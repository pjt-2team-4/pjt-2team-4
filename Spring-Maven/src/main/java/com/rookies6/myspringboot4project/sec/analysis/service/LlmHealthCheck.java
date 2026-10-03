package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.dto.LlmExplainDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "llm.health-check.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class LlmHealthCheck {
    private final LlmClient client;

    @Async("analysisTaskExecutor")
    @EventListener(ApplicationReadyEvent.class)
    public void checkOnce() {
        try {
            LlmExplainDTO.HealthResponse health = client.health();
            log.info("LLM service health: status={}, model={}, promptVersion={}",
                    health.status(), health.model(), health.promptVersion());
        } catch (Exception e) {
            log.warn("LLM service health check failed; Scanner remains available: {}",
                    e.getClass().getSimpleName());
        }
    }
}
