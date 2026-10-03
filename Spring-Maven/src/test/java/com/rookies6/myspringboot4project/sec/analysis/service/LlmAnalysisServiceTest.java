package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.dto.LlmExplainDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LlmAnalysisServiceTest {
    private final AnalysisStateService stateService = mock(AnalysisStateService.class);
    private final LlmClient client = mock(LlmClient.class);
    private final AnalysisProgressStore progressStore = new AnalysisProgressStore();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final LlmExplainDTO.Request request = new LlmExplainDTO.Request(
            9L, "v1.0", null, null, null);

    @AfterEach
    void shutdown() {
        executor.shutdownNow();
    }

    private LlmAnalysisService service(Duration timeout) {
        when(stateService.prepareExplainRequests(5L)).thenReturn(List.of(request));
        progressStore.start(5L, 1);
        return new LlmAnalysisService(stateService, progressStore, client, executor, timeout);
    }

    @Test
    void schemaErrorIsNotRetriedAndOnlyFindingFails() {
        when(client.explain(request)).thenThrow(
                new LlmClient.LlmCallException("LLM 서비스 오류 (HTTP 422)", false, "invalid JSON"));
        when(stateService.saveLlmFailure(9L, "LLM 서비스 오류 (HTTP 422)", "invalid JSON"))
                .thenReturn(true);

        assertThat(service(Duration.ofSeconds(5)).explainAll(5L)).isTrue();

        verify(client, times(1)).explain(request);
        verify(stateService).saveLlmFailure(9L, "LLM 서비스 오류 (HTTP 422)", "invalid JSON");
        assertThat(progressStore.get(5L).orElseThrow().explainedFindings()).isEqualTo(1);
    }

    @Test
    void unavailableServiceIsRetriedOnceAndCanSucceed() {
        LlmExplainDTO.Response response = new LlmExplainDTO.Response(
                9L, "mock", "v1.0", 0, 0, "TRUE_POSITIVE", 90,
                "설명", null, null, "개선", "fixed", "{}");
        when(client.explain(request))
                .thenThrow(new LlmClient.LlmCallException("HTTP 503", true, null))
                .thenReturn(response);
        when(stateService.saveLlmSuccess(9L, response)).thenReturn(true);

        assertThat(service(Duration.ofSeconds(5)).explainAll(5L)).isTrue();

        verify(client, times(2)).explain(request);
        verify(stateService).saveLlmSuccess(9L, response);
        assertThat(progressStore.get(5L).orElseThrow().explainedFindings()).isEqualTo(1);
    }

    @Test
    void wholePhaseTimeoutFailsAnalysis() {
        when(client.explain(request)).thenAnswer(invocation -> {
            Thread.sleep(300);
            return null;
        });

        assertThat(service(Duration.ofMillis(30)).explainAll(5L)).isFalse();

        verify(stateService).failExplanationTimeout(5L);
        verify(stateService, never()).saveLlmSuccess(any(), any());
    }
}
