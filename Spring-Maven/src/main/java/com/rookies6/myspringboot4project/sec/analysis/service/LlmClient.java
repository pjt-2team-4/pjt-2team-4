package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.dto.LlmExplainDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

@Component
public class LlmClient {
    private final RestClient restClient;
    private final String internalToken;

    public LlmClient(RestClient.Builder builder,
                     @Value("${llm.base-url}") String baseUrl,
                     @Value("${llm.internal-token:}") String internalToken,
                     @Value("${llm.connect-timeout}") Duration connectTimeout,
                     @Value("${llm.read-timeout}") Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.internalToken = internalToken;
    }

    public LlmExplainDTO.Response explain(LlmExplainDTO.Request request) {
        if (internalToken.isBlank()) {
            throw new LlmCallException("내부 LLM 토큰이 설정되지 않았습니다.", false, null);
        }
        try {
            LlmExplainDTO.Response response = restClient.post()
                    .uri("/internal/llm/explain")
                    .header("X-Internal-Token", internalToken)
                    .body(request)
                    .retrieve()
                    .body(LlmExplainDTO.Response.class);
            if (response == null || !request.findingId().equals(response.findingId())
                    || response.explanation() == null || response.explanation().isBlank()
                    || response.remediation() == null || response.remediation().isBlank()) {
                throw new LlmCallException("LLM 응답이 요청과 일치하지 않거나 필수 설명이 없습니다.", false,
                        response == null ? null : response.rawResponse());
            }
            return response;
        } catch (RestClientResponseException e) {
            int status = e.getStatusCode().value();
            throw new LlmCallException("LLM 서비스 오류 (HTTP " + status + ")",
                    status == 502 || status == 503 || status == 504,
                    rawResponseOf(e));
        } catch (ResourceAccessException e) {
            throw new LlmCallException("LLM 서비스에 연결할 수 없거나 응답 시간이 초과되었습니다.", true, null);
        } catch (RestClientException e) {
            throw new LlmCallException("LLM 응답을 처리할 수 없습니다.", false, null);
        }
    }

    public LlmExplainDTO.HealthResponse health() {
        return restClient.get().uri("/internal/llm/health")
                .retrieve().body(LlmExplainDTO.HealthResponse.class);
    }

    private String rawResponseOf(RestClientResponseException error) {
        try {
            LlmExplainDTO.ErrorResponse response =
                    error.getResponseBodyAs(LlmExplainDTO.ErrorResponse.class);
            return response == null ? null : response.rawResponse();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static class LlmCallException extends RuntimeException {
        private final boolean retryable;
        private final String rawResponse;

        public LlmCallException(String message, boolean retryable, String rawResponse) {
            super(message);
            this.retryable = retryable;
            this.rawResponse = rawResponse;
        }

        public boolean isRetryable() {
            return retryable;
        }

        public String getRawResponse() {
            return rawResponse;
        }
    }
}
