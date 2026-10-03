package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.dto.LlmExplainDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class LlmAnalysisService {
    private final AnalysisStateService stateService;
    private final AnalysisProgressStore progressStore;
    private final LlmClient client;
    private final ExecutorService executor;
    private final Duration phaseTimeout;

    public LlmAnalysisService(AnalysisStateService stateService, AnalysisProgressStore progressStore,
                              LlmClient client, @Qualifier("llmTaskExecutor") ExecutorService executor,
                              @Value("${llm.phase-timeout}") Duration phaseTimeout) {
        this.stateService = stateService;
        this.progressStore = progressStore;
        this.client = client;
        this.executor = executor;
        this.phaseTimeout = phaseTimeout;
    }

    public boolean explainAll(Long analysisId) {
        long deadline = System.nanoTime() + phaseTimeout.toNanos();
        List<LlmExplainDTO.Request> requests = stateService.prepareExplainRequests(analysisId);
        List<CompletableFuture<Void>> tasks = requests.stream()
                .map(request -> CompletableFuture.runAsync(
                        () -> explainOne(analysisId, request, deadline), executor))
                .toList();

        try {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                throw new TimeoutException("LLM 설명 단계 타임아웃");
            }
            CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new))
                    .get(remaining, TimeUnit.NANOSECONDS);
            return true;
        } catch (TimeoutException e) {
            stateService.failExplanationTimeout(analysisId);
            tasks.forEach(task -> task.cancel(true));
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LLM 설명 단계가 중단되었습니다.", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("LLM 결과 저장 중 오류가 발생했습니다.", e.getCause());
        }
    }

    private void explainOne(Long analysisId, LlmExplainDTO.Request request, long deadline) {
        for (int attempt = 0; attempt < 2; attempt++) {
            if (System.nanoTime() >= deadline) {
                return;
            }
            try {
                LlmExplainDTO.Response response = client.explain(request);
                if (stateService.saveLlmSuccess(request.findingId(), response)) {
                    progressStore.addExplained(analysisId);
                    progressStore.log(analysisId, "AI 설명 생성 완료 (%d)".formatted(request.findingId()));
                }
                return;
            } catch (LlmClient.LlmCallException e) {
                if (e.isRetryable() && attempt == 0
                        && deadline - System.nanoTime() > TimeUnit.SECONDS.toNanos(2)) {
                    continue;
                }
                if (stateService.saveLlmFailure(request.findingId(), e.getMessage(), e.getRawResponse())) {
                    progressStore.addExplained(analysisId);
                    progressStore.log(analysisId, "AI 설명 생성 실패 (%d)".formatted(request.findingId()));
                }
                return;
            }
        }
    }
}
