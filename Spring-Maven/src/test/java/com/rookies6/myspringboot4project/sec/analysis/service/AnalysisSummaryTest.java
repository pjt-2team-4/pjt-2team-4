package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.repository.AnalysisFileRepository;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AnalysisSummaryTest {
    private final AnalysisRequestRepository requestRepository = mock(AnalysisRequestRepository.class);
    private final AnalysisFileRepository fileRepository = mock(AnalysisFileRepository.class);
    private final FindingVulnerabilityRepository findingRepository = mock(FindingVulnerabilityRepository.class);
    private final AnalysisResultService service = new AnalysisResultService(
            requestRepository, fileRepository, findingRepository, new AnalysisProgressStore());

    @Test
    void ignoredFindingsAreExcludedFromSummaryAndResolutionRate() {
        AnalysisRequest request = mock(AnalysisRequest.class);
        when(requestRepository.findById(7L)).thenReturn(Optional.of(request));
        when(request.getId()).thenReturn(7L);
        when(request.getTitle()).thenReturn("sample");
        when(request.getStatus()).thenReturn(AnalysisStatus.COMPLETED);
        when(request.getTotalFindings()).thenReturn(3);
        when(fileRepository.countByAnalysisRequestId(7L)).thenReturn(2L);
        when(findingRepository.countBySeverity(7L, FindingStatus.IGNORED))
                .thenReturn(List.<Object[]>of(new Object[]{Severity.HIGH, 2L}));
        when(findingRepository.countByType(7L, FindingStatus.IGNORED))
                .thenReturn(List.<Object[]>of(new Object[]{VulnerabilityType.HARDCODED_SECRET, 2L}));
        when(findingRepository.countByRule(7L, FindingStatus.IGNORED))
                .thenReturn(List.<Object[]>of(new Object[]{"SECRET-001", 2L}));
        when(findingRepository.countByStatus(7L))
                .thenReturn(List.of(new Object[]{FindingStatus.OPEN, 1L},
                        new Object[]{FindingStatus.RESOLVED, 1L},
                        new Object[]{FindingStatus.IGNORED, 1L}));

        var response = service.getSummary(7L);

        assertThat(response.getTotalFindings()).isEqualTo(2);
        assertThat(response.getSeverityCount()).containsEntry("HIGH", 2L);
        assertThat(response.getResolutionRate()).isEqualTo(0.5);
        assertThat(response.getOverallSeverity()).isEqualTo("HIGH");
    }
}
