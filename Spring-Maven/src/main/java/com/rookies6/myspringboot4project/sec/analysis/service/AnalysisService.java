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
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

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
    public AnalysisDTO.Response analyzeCode(AnalysisDTO.Request request) {

        // 1. 사용자 조회
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "User", "id", 1L
                ));

        // 2. 파일별 언어 중 가장 많은 언어를 대표 언어로 사용
        List<AnalysisDTO.Request.FileRequest> targetFiles = request.getFiles();

        if (targetFiles == null || targetFiles.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT, "분석할 파일이 존재하지 않습니다."
            );
        }

        Map<Language, Integer> languageCounts = new LinkedHashMap<>();
        for (AnalysisDTO.Request.FileRequest fileReq : targetFiles) {
            Language language = Language.fromFileName(fileReq.getRelativePath());
            if (!language.isSupported()) {
                throw new BusinessException(
                        ErrorCode.INVALID_INPUT,
                        "지원하지 않는 파일 형식입니다: " + fileReq.getRelativePath()
                );
            }
            languageCounts.merge(language, 1, Integer::sum);
        }

        Language primaryLanguage = null;
        int maxCount = 0;
        for (var entry : languageCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                primaryLanguage = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        AnalysisRequest analysisRequest = AnalysisRequest.create(
                user,
                request.getTitle(),
                primaryLanguage.name(),
                30 // estimatedDurationSeconds
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
//        analysisWorker.runAnalysisPipeline(savedRequest.getId());
        Long analysisId = savedRequest.getId();
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        analysisWorker.runAnalysisPipeline(analysisId);
                    }
                });

        // 6. 즉시 응답
        return AnalysisDTO.Response.fromEntity(savedRequest);
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

            case EXPLAINING -> response
                    .stage("EXPLAIN")
                    .stageLabel("AI 설명 생성 중")
                    .progress(40)
                    .totalFindings(request.getTotalFindings())
                    .recentLogs(logs);

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
                    .stage("SCAN")
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
