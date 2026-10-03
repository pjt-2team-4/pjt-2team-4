package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisDTO;
import com.rookies6.myspringboot4project.sec.analysisfile.dto.AnalysisFileDTO;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisResultDTO;
import com.rookies6.myspringboot4project.sec.common.enums.Language;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final AnalysisRequestRepository analysisRequestRepository;
    private final UserRepository userRepository; 
    private final AnalysisWorker analysisWorker;

    /**
     * 분석 요청
     */
    @Transactional
    public AnalysisResultDTO.AcceptedResponse analyzeCode(AnalysisDTO.Request request) {
        List<AnalysisDTO.Request.FileRequest> targetFiles = request.getFiles();
        if (targetFiles == null || targetFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_FILE_LIST);
        }
        if (targetFiles.size() > 20) {
            throw new BusinessException(ErrorCode.FILE_COUNT_EXCEEDED);
        }

        Map<Language, Integer> languageCounts = new LinkedHashMap<>();
        Set<String> paths = new HashSet<>();
        long totalBytes = 0;
        for (AnalysisDTO.Request.FileRequest fileReq : targetFiles) {
            String path = fileReq.getRelativePath();
            if (!paths.add(path)) {
                throw new BusinessException(ErrorCode.DUPLICATE_FILE_PATH);
            }
            int fileBytes = fileReq.getContent().getBytes(StandardCharsets.UTF_8).length;
            if (fileBytes > 100 * 1024) {
                throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
            }
            totalBytes += fileBytes;
            if (totalBytes > 500 * 1024) {
                throw new BusinessException(ErrorCode.CODE_SIZE_EXCEEDED);
            }
            Language language = Language.fromFileName(path);
            if (!language.isSupported()) {
                int dot = path.lastIndexOf('.');
                String extension = dot < 0 ? path : path.substring(dot);
                throw new BusinessException(ErrorCode.UNSUPPORTED_LANGUAGE,
                        ErrorCode.UNSUPPORTED_LANGUAGE.getMessage(extension));
            }
            languageCounts.merge(language, 1, Integer::sum);
        }

        // 인증 연동 전까지 기존 사용자 선택 방식은 유지한다.
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "User", "id", 1L
                ));

        Language primaryLanguage = null;
        int maxCount = 0;
        for (var entry : languageCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                primaryLanguage = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        int estimatedDurationSeconds = targetFiles.size() * 20;
        AnalysisRequest analysisRequest = AnalysisRequest.create(
                user,
                request.getTitle(),
                primaryLanguage.name(),
                null
        );

        // 3. 클라이언트가 보낸 파일 데이터를 AnalysisFile로 변환하여 추가

        for (AnalysisDTO.Request.FileRequest fileReq : targetFiles) {
            int lineCount = fileReq.getContent() != null
                    ? fileReq.getContent().split("\n", -1).length
                    : 0;

            String relativePath = fileReq.getRelativePath();
            Language fileLanguage = Language.fromFileName(relativePath);

            AnalysisFile analysisFile = AnalysisFile.builder()
                    .relativePath(relativePath)
                    .fileName(extractFileName(relativePath))
                    .language(fileLanguage.name())
                    .content(fileReq.getContent())
                    .lineCount(lineCount)
                    .build();

            analysisRequest.addFile(analysisFile);
        }

        // 4. 분석 요청 저장
        AnalysisRequest savedRequest = analysisRequestRepository.save(analysisRequest);

        log.info("분석 요청 생성 완료. analysisId={}", savedRequest.getId());

        // 5. 비동기 분석 시작
        Long analysisId = savedRequest.getId();
        Set<VulnerabilityType> enabledTypes = enabledTypes(request.getScanOptions());
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        analysisWorker.runAnalysisPipeline(analysisId, enabledTypes);
                    }
                });

        // 6. 즉시 응답
        return new AnalysisResultDTO.AcceptedResponse(
                savedRequest.getId(), savedRequest.getTitle(), savedRequest.getStatus().name(),
                targetFiles.size(), estimatedDurationSeconds, savedRequest.getCreatedAt());
    }

    private static Set<VulnerabilityType> enabledTypes(AnalysisDTO.Request.ScanOptions options) {
        EnumSet<VulnerabilityType> types = EnumSet.allOf(VulnerabilityType.class);
        if (options != null) {
            if (Boolean.FALSE.equals(options.detectSqlInjection())) {
                types.remove(VulnerabilityType.SQL_INJECTION);
            }
            if (Boolean.FALSE.equals(options.detectHardcodedSecret())) {
                types.remove(VulnerabilityType.HARDCODED_SECRET);
            }
            if (Boolean.FALSE.equals(options.detectXss())) {
                types.remove(VulnerabilityType.XSS);
            }
        }
        return Set.copyOf(types);
    }

    /**
     * 분석 목록
     */
    public List<AnalysisDTO.Response> getAnalyses() {
        return analysisRequestRepository.findAll()
                .stream()
                .map(AnalysisDTO.Response::fromEntity)
                .toList();
    }

    /**
     * 분석 상세
     */
    public AnalysisDTO.Response getAnalysisById(Long id) {
        AnalysisRequest analysisRequest = analysisRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", id
                ));

        return AnalysisDTO.Response.fromEntity(analysisRequest);
    }

    private final AnalysisProgressStore progressStore;

    /**
     * 분석 상태
     */
    public AnalysisResultDTO.StatusResponse getAnalysisStatus(Long id) {
        AnalysisRequest request = analysisRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", id));

        var snapshot = progressStore.get(id).orElse(null);
        int totalFiles = snapshot != null
                ? snapshot.totalFiles()
                : request.getAnalysisFiles().size();

        List<AnalysisProgressStore.LogLine> logs = snapshot == null
                ? List.of()
                : snapshot.recentLogs();

        var response = AnalysisResultDTO.StatusResponse.builder()
                .analysisId(request.getId())
                .status(request.getStatus().name());

        switch (request.getStatus()) {
            case PENDING -> response
                    .progress(0)
                    .totalFiles(totalFiles);

            case SCANNING -> {
                int processed = snapshot == null ? 0 : snapshot.processedFiles();
                int progress = totalFiles == 0
                        ? 0
                        : Math.min(40, processed * 40 / totalFiles);

                response.stage("SCAN")
                        .stageLabel("규칙 기반 탐지 중")
                        .progress(progress)
                        .currentFile(snapshot == null ? null : snapshot.currentFile())
                        .processedFiles(processed)
                        .totalFiles(totalFiles)
                        .findingsSoFar(snapshot == null ? 0 : snapshot.findingsSoFar())
                        .recentLogs(logs)
                        .startedAt(request.getStartedAt());
            }

            case EXPLAINING -> {
                int total = request.getTotalFindings();
                int explained = snapshot == null ? 0 : snapshot.explainedFindings();
                response.stage("EXPLAIN")
                        .stageLabel("AI 설명 생성 중")
                        .progress(total == 0 ? 40
                                : Math.min(100, 40 + explained * 60 / total))
                        .totalFindings(total)
                        .explainedFindings(explained)
                        .recentLogs(logs)
                        .startedAt(request.getStartedAt());
            }

            case COMPLETED -> {
//                var highest = Severity.highest(
//                        request.getAnalysisFiles().stream()
//                                .flatMap(file -> file.getFindings().stream())
//                                .map(finding -> finding.getSeverity())
//                                .toList());

                Long duration = request.getStartedAt() != null
                        && request.getCompletedAt() != null
                        ? ChronoUnit.SECONDS.between(
                        request.getStartedAt(), request.getCompletedAt())
                        : null;

                response.stage("DONE")
                        .progress(100)
                        .totalFiles(totalFiles)
                        .totalFindings(request.getTotalFindings())
                        .overallSeverity(request.getOverallSeverity() == null
                                ? null
                                : request.getOverallSeverity().name())
                        .durationSeconds(duration)
                        .completedAt(request.getCompletedAt());
            }

            case FAILED -> response
                    .stage(request.getTotalFindings() != null && request.getTotalFindings() > 0
                            ? "EXPLAIN" : "SCAN")
                    .errorMessage(request.getErrorMessage());
        }

        return response.build();
    }

/**
     * 특정 분석 ID에 속한 파일 목록 조회
     */
    public List<AnalysisFileDTO.Response> getFilesByAnalysisId(Long analysisId) {
        AnalysisRequest analysisRequest = analysisRequestRepository.findById(analysisId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", analysisId
                ));

        return analysisRequest.getAnalysisFiles().stream()
                .map(AnalysisFileDTO.Response::new) // 생성자를 통해 바로 변환
                .toList();
    }

    /**
     * 특정 분석의 특정 파일 상세 내용 조회
     */
    public AnalysisFileDTO.DetailResponse getFileDetail(Long analysisId, Long fileId) {
        AnalysisRequest analysisRequest = analysisRequestRepository.findById(analysisId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", analysisId
                ));

        AnalysisFile targetFile = analysisRequest.getAnalysisFiles().stream()
                .filter(file -> file.getId().equals(fileId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisFile", "id", fileId
                ));

        return new AnalysisFileDTO.DetailResponse(targetFile); // 생성자를 통해 바로 변환
    }

    /**
     * 분석 삭제
     */
    @Transactional
    public void deleteAnalysis(Long id) {
        AnalysisRequest analysisRequest = analysisRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", id
                ));

        analysisRequestRepository.delete(analysisRequest);
    }

    // 유틸리티 메서드: 경로에서 파일명만 추출
    private String extractFileName(String filePath) {
        if (filePath == null) return "unknown";
        int lastSlash = filePath.lastIndexOf('/');
        int lastBackslash = filePath.lastIndexOf('\\');
        int maxIndex = Math.max(lastSlash, lastBackslash);
        return maxIndex >= 0 ? filePath.substring(maxIndex + 1) : filePath;
    }
}
