package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisResultDTO;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.repository.AnalysisFileRepository;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisResultService {

    private final AnalysisRequestRepository requestRepository;
    private final AnalysisFileRepository fileRepository;
    private final FindingVulnerabilityRepository findingRepository;
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

    /** 7번 — 집계는 GROUP BY 쿼리로 처리, 취약점 전체를 메모리에 올리지 않는다 */
    public AnalysisResultDTO.SummaryResponse getSummary(Long analysisId) {
        AnalysisRequest request = requestRepository.findById(analysisId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "분석 요청을 찾을 수 없습니다: " + analysisId));

        // severityCount (IGNORED 제외)
        Map<Severity, Long> severityMap = toEnumMap(
                findingRepository.countBySeverity(analysisId, FindingStatus.IGNORED), Severity.class);

        // typeCount (IGNORED 제외, 3개 유형 항상 포함 — 결과 화면 탭용)
        Map<VulnerabilityType, Long> typeMap = toEnumMap(
                findingRepository.countByType(analysisId, FindingStatus.IGNORED), VulnerabilityType.class);
        List<AnalysisResultDTO.TypeCount> typeCount = Arrays.stream(VulnerabilityType.values())
                .map(type -> AnalysisResultDTO.TypeCount.of(type, typeMap.getOrDefault(type, 0L)))
                .toList();

        // ruleCount (IGNORED 제외, 건수 많은 순)
        List<AnalysisResultDTO.RuleCount> ruleCount =
                findingRepository.countByRule(analysisId, FindingStatus.IGNORED).stream()
                        .map(row -> new AnalysisResultDTO.RuleCount((String) row[0], (Long) row[1]))
                        .toList();

        // resolutionRate = RESOLVED / (전체 - IGNORED)
        Map<FindingStatus, Long> statusMap = toEnumMap(findingRepository.countByStatus(analysisId), FindingStatus.class);
        long resolved = statusMap.getOrDefault(FindingStatus.RESOLVED, 0L);
        long countable = statusMap.values().stream().mapToLong(Long::longValue).sum()
                - statusMap.getOrDefault(FindingStatus.IGNORED, 0L);
        double resolutionRate = countable == 0 ? 0.0 : (double) resolved / countable;

        Long durationSeconds = (request.getStartedAt() == null || request.getCompletedAt() == null)
                ? null
                : Duration.between(request.getStartedAt(), request.getCompletedAt()).toSeconds();

        return AnalysisResultDTO.SummaryResponse.builder()
                .analysisId(request.getId())
                .title(request.getTitle())
                .status(request.getStatus().name())
                .overallSeverity(highestOf(severityMap))
                .totalFiles((int) fileRepository.countByAnalysisRequestId(analysisId))
                .totalFindings(request.getTotalFindings() == null ? 0 : request.getTotalFindings())
                .severityCount(AnalysisResultDTO.severityCountOf(severityMap))
                .typeCount(typeCount)
                .ruleCount(ruleCount)
                .resolutionRate(resolutionRate)
                .durationSeconds(durationSeconds)
                .startedAt(request.getStartedAt())
                .completedAt(request.getCompletedAt())
                .build();
    }

    /** 0건이 아닌 최고 심각도. 없으면 null */
    private static String highestOf(Map<Severity, Long> severityMap) {
        for (Severity severity : List.of(Severity.CRITICAL, Severity.HIGH, Severity.MEDIUM, Severity.LOW)) {
            if (severityMap.getOrDefault(severity, 0L) > 0) {
                return severity.name();
            }
        }
        return null;
    }

    /** GROUP BY 결과(Object[] {enum, count})를 EnumMap 으로 변환 */
    private static <E extends Enum<E>> Map<E, Long> toEnumMap(List<Object[]> rows, Class<E> type) {
        Map<E, Long> map = new EnumMap<>(type);
        for (Object[] row : rows) {
            map.put(type.cast(row[0]), (Long) row[1]);
        }
        return map;
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