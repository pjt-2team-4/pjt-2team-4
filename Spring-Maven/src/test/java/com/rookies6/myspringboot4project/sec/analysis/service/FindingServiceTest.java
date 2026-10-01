package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FindingServiceTest {
    private final FindingVulnerabilityRepository findingRepository = mock(FindingVulnerabilityRepository.class);
    private final AnalysisRequestRepository requestRepository = mock(AnalysisRequestRepository.class);
    private final FindingService service = new FindingService(findingRepository, requestRepository);

    @Test
    void listPassesAllFiltersAndReturnsPageMetadata() {
        var pageable = PageRequest.of(1, 2);
        when(requestRepository.existsById(7L)).thenReturn(true);
        when(findingRepository.findAllByAnalysisId(7L, VulnerabilityType.SQL_INJECTION,
                "SQLI-001", FindingStatus.OPEN, 12L,
                List.of(Severity.CRITICAL, Severity.HIGH), pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 3));

        var response = service.getFindings(7L, VulnerabilityType.SQL_INJECTION,
                "SQLI-001", FindingStatus.OPEN,
                List.of(Severity.CRITICAL, Severity.HIGH), 12L, 1, 2);

        assertThat(response.content()).isEmpty();
        assertThat(response.page().number()).isEqualTo(1);
        assertThat(response.page().size()).isEqualTo(2);
        assertThat(response.page().totalElements()).isEqualTo(3);
        assertThat(response.page().totalPages()).isEqualTo(2);
    }

    @Test
    void missingFindingUsesDocumentedErrorCode() {
        when(findingRepository.findDetailById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFinding(99L))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.FINDING_NOT_FOUND));
    }

    @Test
    void pageSizeOverLimitIsRejected() {
        assertThatThrownBy(() -> service.getFindings(7L, null, null, null,
                null, null, 0, 51))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
        verifyNoInteractions(findingRepository);
    }
}
