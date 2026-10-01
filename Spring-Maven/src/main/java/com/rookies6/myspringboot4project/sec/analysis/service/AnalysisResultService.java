package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisResultDTO;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.repository.AnalysisFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisResultService {

    private final AnalysisRequestRepository requestRepository;
    private final AnalysisFileRepository fileRepository;
    private final AnalysisProgressStore progressStore;

    public AnalysisResultDTO.StatusResponse getStatus(Long analysisId) {
        AnalysisRequest request = requestRepository.findById(analysisId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND, "AnalysisRequest", "id", analysisId));

        AnalysisStatus status = request.getStatus();
        var snapshot = progressStore.get(analysisId).orElse(null);
        int totalFiles = snapshot != null
                ? snapshot.totalFiles()
                : (int) fileRepository.countByAnalysisRequestId(analysisId);
        int processed = status == AnalysisStatus.COMPLETED
                ? totalFiles
                : snapshot != null ? snapshot.processedFiles() : 0;
        int found = status == AnalysisStatus.COMPLETED
                ? request.getTotalFindings()
                : snapshot != null ? snapshot.findingsSoFar() : 0;

        return AnalysisResultDTO.StatusResponse.builder()
                .analysisId(request.getId())
                .status(status.name())
                .stage(stageOf(status))
                .stageLabel(labelOf(status))
                .progress(progressOf(status, processed, totalFiles))
                .currentFile(snapshot != null ? snapshot.currentFile() : null)
                .processedFiles(processed)
                .totalFiles(totalFiles)
                .findingsSoFar(found)
                .totalFindings(request.getTotalFindings())
                .errorMessage(request.getErrorMessage())
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .recentLogs(snapshot != null ? snapshot.recentLogs() : List.of())
                .build();
    }

    private static String stageOf(AnalysisStatus status) {
        return switch (status) {
            case PENDING -> "WAIT";
            case SCANNING -> "SCAN";
            case EXPLAINING -> "EXPLAIN";
            case COMPLETED -> "DONE";
            case FAILED -> "SCAN";
        };
    }

    private static String labelOf(AnalysisStatus status) {
        return switch (status) {
            case PENDING -> "분석 대기 중";
            case SCANNING -> "규칙 기반 탐지 중";
            case EXPLAINING -> "AI 설명 생성 중";
            case COMPLETED -> "분석 완료";
            case FAILED -> "분석 실패";
        };
    }

    private static int progressOf(AnalysisStatus status, int processed, int total) {
        return switch (status) {
            case PENDING, FAILED -> 0;
            case SCANNING -> total == 0 ? 0
                    : (int) Math.round(processed * 40.0 / total);
            case EXPLAINING -> 40; // LLM 연결 시 40~100% 계산으로 확장
            case COMPLETED -> 100;
        };
    }

    // 위의 코드, LLM 연동시 아래 코드로 교체하기
    /**
     * AnalysisProgressStore에 explainedFindings를 추가하고, 
     * LLM 설명 1건이 성공하거나 실패 처리될 때마다 1씩 올려야함. 
     * AnalysisRequest도 EXPLAINING 시작 시 totalFindings를 저장해야 하고, 
     * 실제 응답을 만드는 AnalysisService에서 두 값을 읽어와서 계산해야딤
     */
//    private static int progressOf(
//            AnalysisStatus status,
//            int processedFiles, int totalFiles,
//            int explainedFindings, int totalFindings) {
//        return switch (status) {
//            case PENDING, FAILED -> 0;
//            case SCANNING -> totalFiles == 0 ? 0
//                    : Math.min(40, (int) Math.round(processedFiles * 40.0 / totalFiles));
//            case EXPLAINING -> totalFindings <= 0 ? 40
//                    : Math.min(100, 40 + (int) Math.round(
//                    explainedFindings * 60.0 / totalFindings));
//            case COMPLETED -> 100;
//        };
//    }
}