package com.rookies6.myspringboot4project.sec.dashboard.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import com.rookies6.myspringboot4project.sec.dashboard.dto.DashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int SEVERITY_TREND_DAYS = 7;

    // TODO: AnalysisRequest에 riskScore 컬럼이 아직 없어서, overallSeverity를 숫자로 임시 환산함.
    // 실제 점수 산정 로직이 생기면 이 매핑은 지우고 엔티티 값을 그대로 쓸 것.
    private static final Map<Severity, Integer> RISK_SCORE_BY_SEVERITY = Map.of(
            Severity.CRITICAL, 90,
            Severity.HIGH, 70,
            Severity.MEDIUM, 40,
            Severity.LOW, 15
    );

    private final AnalysisRequestRepository analysisRequestRepository;
    private final FindingVulnerabilityRepository findingVulnerabilityRepository;

    public DashboardDTO.Response getDashboardSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisWeek = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime startOfLastWeek = startOfThisWeek.minusWeeks(1);

        // 이번주 분석한 수
        long weeklyAnalysisCount = analysisRequestRepository.countByStatusAndCreatedAtBetween(
                AnalysisStatus.COMPLETED, startOfThisWeek, now);
        // 지난주 대비 몇 % 증가한건지
        long lastWeekAnalysisCount = analysisRequestRepository.countByStatusAndCreatedAtBetween(
                AnalysisStatus.COMPLETED, startOfLastWeek, startOfThisWeek);
        double weeklyAnalysisDelta = rateOfChange(lastWeekAnalysisCount, weeklyAnalysisCount);
        // 지금까지 발견한 취약점 수
        long totalFindings = findingVulnerabilityRepository.count();
        // '치명적'으로 나온 분석 수
        long criticalCount = analysisRequestRepository.countByOverallSeverity(Severity.CRITICAL);

        // 해결률
        double resolutionRate = currentResolutionRate();
        // TODO: "지난 기간 대비" 해결률 변화는 과거 시점의 상태 이력(언제 RESOLVED로 바뀌었는지)이
        // 없으면 정확히 계산할 수 없음. 상태 변경 이력 테이블이 생기기 전까지는 0으로 둠.
        double resolutionRateDelta = 0.0;

        // 평균 분석 시간
        long averageDurationSeconds = averageDurationSeconds();

        List<DashboardDTO.SeverityTrend> severityTrend = buildSeverityTrend(now);
        List<DashboardDTO.TypeDistribution> typeDistribution = buildTypeDistribution(totalFindings);
        List<DashboardDTO.RecentAnalysis> recentAnalyses = buildRecentAnalyses();

        return DashboardDTO.Response.builder()
                .weeklyAnalysisCount(weeklyAnalysisCount)
                .weeklyAnalysisDelta(weeklyAnalysisDelta)
                .totalFindings(totalFindings)
                .criticalCount(criticalCount)
                .resolutionRate(resolutionRate)
                .resolutionRateDelta(resolutionRateDelta)
                .averageDurationSeconds(averageDurationSeconds)
                .severityTrend(severityTrend)
                .typeDistribution(typeDistribution)
                .recentAnalyses(recentAnalyses)
                .build();
    }

    // 1. 분석 수가 지난주 대비 몇 % 증가했는지 구하는 메서드
    private double rateOfChange(long previous, long current) {
        return previous == 0 ? 0.0 : (double) (current - previous) / previous;
    }

    // 2. 해결률 구하는 메서드
    private double currentResolutionRate() {
        long resolved = findingVulnerabilityRepository.countByStatusIn(List.of(FindingStatus.RESOLVED));
        long countable = findingVulnerabilityRepository.countByStatusIn(
                List.of(FindingStatus.OPEN, FindingStatus.RESOLVED));
        return countable == 0 ? 0.0 : (double) resolved / countable;
    }

    // 3. 평균 분석 시간
    private long averageDurationSeconds() {
        List<AnalysisRequest> completed = analysisRequestRepository
                .findByStatusAndStartedAtIsNotNullAndCompletedAtIsNotNull(AnalysisStatus.COMPLETED);

        if (completed.isEmpty()) {
            return 0L;
        }

        long totalSeconds = completed.stream()
                .mapToLong(a -> Duration.between(a.getStartedAt(), a.getCompletedAt()).getSeconds())
                .sum();

        return totalSeconds / completed.size();
    }

    // 4. 심각도
    private List<DashboardDTO.SeverityTrend> buildSeverityTrend(LocalDateTime now) {
        LocalDateTime since = now.toLocalDate().minusDays(SEVERITY_TREND_DAYS - 1L).atStartOfDay();
        List<FindingVulnerability> recentFindings = findingVulnerabilityRepository.findByCreatedAtAfter(since);

        Map<LocalDate, List<FindingVulnerability>> byDate = recentFindings.stream()
                .collect(Collectors.groupingBy(f -> f.getCreatedAt().toLocalDate()));

        return java.util.stream.IntStream.range(0, SEVERITY_TREND_DAYS)
                .mapToObj(i -> since.toLocalDate().plusDays(i))
                .map(date -> {
                    List<FindingVulnerability> findingsOfDay = byDate.getOrDefault(date, List.of());
                    return DashboardDTO.SeverityTrend.builder()
                            .date(date)
                            .critical(countBySeverity(findingsOfDay, Severity.CRITICAL))
                            .high(countBySeverity(findingsOfDay, Severity.HIGH))
                            .medium(countBySeverity(findingsOfDay, Severity.MEDIUM))
                            .low(countBySeverity(findingsOfDay, Severity.LOW))
                            .build();
                })
                .toList();
    }

    private int countBySeverity(List<FindingVulnerability> findings, Severity severity) {
        return (int) findings.stream().filter(f -> f.getSeverity() == severity).count();
    }

    // 5. 취약점 유형별 분포
    private List<DashboardDTO.TypeDistribution> buildTypeDistribution(long totalFindings) {
        if (totalFindings == 0) {
            return List.of();
        }

        List<FindingVulnerability> all = findingVulnerabilityRepository.findAll();
        Map<VulnerabilityType, Long> countByType = all.stream()
                .collect(Collectors.groupingBy(FindingVulnerability::getVulnerabilityType, Collectors.counting()));

        return countByType.entrySet().stream()
                .map(entry -> DashboardDTO.TypeDistribution.builder()
                        .type(entry.getKey().name())
                        .displayName(entry.getKey().getDisplayName())
                        .count(entry.getValue())
                        .ratio((double) entry.getValue() / totalFindings)
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .toList();
    }

    /** 가장 최근 분석 5건 */
    private List<DashboardDTO.RecentAnalysis> buildRecentAnalyses() {
        return analysisRequestRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(a -> DashboardDTO.RecentAnalysis.builder()
                        .analysisId(a.getId())
                        .title(a.getTitle())
                        .language(a.getLanguage())
                        .riskScore(toRiskScore(a.getOverallSeverity()))
                        .createdAt(a.getCreatedAt())
                        .build())
                .toList();
    }

    private Integer toRiskScore(Severity severity) {
        return severity == null ? null : RISK_SCORE_BY_SEVERITY.get(severity);
    }
}
