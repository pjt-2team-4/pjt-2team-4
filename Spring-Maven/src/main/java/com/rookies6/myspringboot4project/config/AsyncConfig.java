package com.rookies6.myspringboot4project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

@EnableAsync // 비동기 처리 활성화
@Configuration
public class AsyncConfig {

    @Bean(name = "analysisTaskExecutor")
    public Executor analysisTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);   // 기본 실행 스레드 수
        executor.setMaxPoolSize(4);    // 최대 스레드 수 (명세서 기준 반영)
        executor.setQueueCapacity(50); // 대기열 크기
        executor.setThreadNamePrefix("AnalysisWorker-");
        executor.initialize();
        return executor;
    }
}