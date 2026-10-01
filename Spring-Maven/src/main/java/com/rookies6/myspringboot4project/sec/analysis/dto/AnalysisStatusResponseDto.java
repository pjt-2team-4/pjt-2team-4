package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisStatusResponseDto {

    private Long analysisId;

    private String status;

    private String stage;

    private String stageLabel;

    private int progress;

    private String currentFile;

    private int processedFiles;

    private int totalFiles;

    private int findingsSoFar;

    private int totalFindings;

    private int explainedFindings;

    private List<RecentLogDto> recentLogs;

    private LocalDateTime startedAt;

    private int durationSeconds;

    private String overallSeverity;

    private LocalDateTime completedAt;

    private String errorMessage;

    /**
     * 기존 호출과 호환하기 위한 메서드
     */
    public static AnalysisStatusResponseDto fromEntity(
            AnalysisRequest request
    ) {
        return fromEntity(request, Collections.emptyList());
    }

    /**
     * 분석 상태 + 최근 로그를 함께 반환
     */
    public static AnalysisStatusResponseDto fromEntity(
            AnalysisRequest request,
            List<RecentLogDto> recentLogs
    ) {

        return AnalysisStatusResponseDto.builder()
                .analysisId(request.getId())

                .status(
                        request.getStatus() != null
                                ? request.getStatus().name()
                                : null
                )

                .stage(request.getStage())
                .stageLabel(createStageLabel(request.getStage()))

                .progress(
                        request.getProgress() != null
                                ? request.getProgress()
                                : 0
                )

                .currentFile(request.getCurrentFile())

                .processedFiles(
                        request.getProcessedFiles() != null
                                ? request.getProcessedFiles()
                                : 0
                )

                .totalFiles(
                        request.getTotalFiles() != null
                                ? request.getTotalFiles()
                                : 0
                )

                .findingsSoFar(
                        request.getFindingsSoFar() != null
                                ? request.getFindingsSoFar()
                                : 0
                )

                .totalFindings(
                        request.getTotalFindings() != null
                                ? request.getTotalFindings()
                                : 0
                )

                .explainedFindings(
                        request.getExplainedFindings() != null
                                ? request.getExplainedFindings()
                                : 0
                )

                .recentLogs(
                        recentLogs != null
                                ? recentLogs
                                : Collections.emptyList()
                )

                .startedAt(request.getStartedAt())

                .durationSeconds(
                        calculateDurationSeconds(request)
                )

                .overallSeverity(
                        calculateOverallSeverity(request)
                )

                .completedAt(request.getCompletedAt())

                .errorMessage(request.getErrorMessage())

                .build();
    }

    /**
     * stage 값을 화면 표시용 문구로 변환
     */
    private static String createStageLabel(String stage) {

        if (stage == null) {
            return null;
        }

        return switch (stage) {
            case "SCANNING" -> "보안 취약점 검사 중";
            case "EXPLAINING" -> "취약점 설명 생성 중";
            case "COMPLETED" -> "분석 완료";
            case "FAILED" -> "분석 실패";
            case "PENDING" -> "분석 대기 중";
            default -> stage;
        };
    }

    /**
     * 분석 소요 시간 계산
     */
    private static int calculateDurationSeconds(
            AnalysisRequest request
    ) {

        if (request.getStartedAt() == null) {
            return 0;
        }

        LocalDateTime endTime =
                request.getCompletedAt() != null
                        ? request.getCompletedAt()
                        : LocalDateTime.now();

        return (int) java.time.Duration
                .between(request.getStartedAt(), endTime)
                .getSeconds();
    }

    /**
     * 현재는 Entity에 severity 집계값이 없으므로 null 반환.
     *
     * 추후 FindingVulnerability의 severity를 기준으로
     * 계산하도록 확장 가능.
     */
    private static String calculateOverallSeverity(
            AnalysisRequest request
    ) {
        return null;
    }
}
