package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AnalysisStatusResponseTest {
    private final AnalysisRequestRepository repository =
            mock(AnalysisRequestRepository.class);
    private final AnalysisProgressStore store = new AnalysisProgressStore();
    private final AnalysisService service = new AnalysisService(
            repository, mock(UserRepository.class), mock(AnalysisWorker.class), store);

    private AnalysisRequest request(AnalysisStatus status) {
        AnalysisRequest request = mock(AnalysisRequest.class);
        when(repository.findById(10L)).thenReturn(Optional.of(request));
        when(request.getId()).thenReturn(10L);
        when(request.getStatus()).thenReturn(status);
        when(request.getAnalysisFiles()).thenReturn(List.of());
        return request;
    }

    @Test
    void pendingStartsAtZero() {
        request(AnalysisStatus.PENDING);

        var result = service.getAnalysisStatus(10L);

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getStage()).isNull();
        assertThat(result.getProgress()).isZero();
        assertThat(result.getProgress()).isZero();
    }

    @Test
    void scanningUsesSnapshotAndKeepsFiveLogs() {
        AnalysisRequest request = request(AnalysisStatus.SCANNING);
        when(request.getAnalysisFiles()).thenReturn(
                List.of(mock(AnalysisFile.class), mock(AnalysisFile.class)));
        store.start(10L, 2);
        store.updateFile(10L, "src/A.java", 1);
        store.addFindings(10L, 3);
        for (int i = 1; i <= 6; i++) {
            store.log(10L, "log-" + i);
        }

        var result = service.getAnalysisStatus(10L);

        assertThat(result.getStage()).isEqualTo("SCAN");
        assertThat(result.getProgress()).isEqualTo(20);
        assertThat(result.getCurrentFile()).isEqualTo("src/A.java");
        assertThat(result.getFindingsSoFar()).isEqualTo(3);
        assertThat(result.getRecentLogs()).hasSize(5);
        assertThat(result.getRecentLogs().get(0).message()).isEqualTo("log-2");
    }

    @Test
    void completedRemainsAtHundredAfterSnapshotIsCleared() {
        AnalysisRequest request = request(AnalysisStatus.COMPLETED);
        when(request.getTotalFindings()).thenReturn(0);

        var result = service.getAnalysisStatus(10L);

        assertThat(result.getStage()).isEqualTo("DONE");
        assertThat(result.getProgress()).isEqualTo(100);
        assertThat(result.getTotalFindings()).isZero();
    }

    @Test
    void explainingProgressIncludesSuccessfulAndFailedFindings() {
        AnalysisRequest request = request(AnalysisStatus.EXPLAINING);
        when(request.getTotalFindings()).thenReturn(4);
        store.start(10L, 1);
        store.addExplained(10L);
        store.addExplained(10L);

        var result = service.getAnalysisStatus(10L);

        assertThat(result.getStage()).isEqualTo("EXPLAIN");
        assertThat(result.getExplainedFindings()).isEqualTo(2);
        assertThat(result.getProgress()).isEqualTo(70);
    }

    @Test
    void failedReturnsReason() {
        AnalysisRequest request = request(AnalysisStatus.FAILED);
        when(request.getErrorMessage()).thenReturn("분석 실패");

        var result = service.getAnalysisStatus(10L);

        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getErrorMessage()).isEqualTo("분석 실패");
    }
}
