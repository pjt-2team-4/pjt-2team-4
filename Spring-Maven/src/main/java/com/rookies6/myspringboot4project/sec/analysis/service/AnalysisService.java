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

import java.util.List;

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

        // 2. 분석 요청(엔티티) 생성
        AnalysisRequest analysisRequest = AnalysisRequest.create(
                user,
                request.getTitle(),
                request.getLanguage(),
                30 // estimatedDurationSeconds
        );

        // 3. 클라이언트가 보낸 파일 데이터를 AnalysisFile로 변환하여 추가
        List<AnalysisDTO.Request.FileRequest> targetFiles = request.getFiles();

        if (targetFiles == null || targetFiles.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT, "분석할 파일이 존재하지 않습니다."
            );
        }

        for (AnalysisDTO.Request.FileRequest fileReq : targetFiles) {
            int lineCount = fileReq.getContent() != null
                    ? fileReq.getContent().split("\n", -1).length
                    : 0;

            AnalysisFile analysisFile = AnalysisFile.builder()
                    .relativePath(fileReq.getFilePath())
                    .fileName(extractFileName(fileReq.getFilePath()))
                    .language(request.getLanguage())
                    .content(fileReq.getContent())
                    .lineCount(lineCount)
                    .build();

            analysisRequest.addFile(analysisFile);
        }

        // 4. 분석 요청 저장
        AnalysisRequest savedRequest = analysisRequestRepository.save(analysisRequest);

        log.info("분석 요청 생성 완료. analysisId={}", savedRequest.getId());

        // 5. 비동기 분석 시작
        analysisWorker.runAnalysisPipeline(savedRequest.getId());

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

    /**
     * 분석 상태
     */
    public AnalysisDTO.StatusResponse getAnalysisStatus(Long id) {
        AnalysisRequest analysisRequest = analysisRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", id
                ));

        return AnalysisDTO.StatusResponse.fromEntity(analysisRequest);
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