package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.dto.LlmExplainDTO;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class LlmClientTest {
    private HttpServer server;
    private LlmClient client;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        client = new LlmClient(RestClient.builder(),
                "http://127.0.0.1:" + server.getAddress().getPort(),
                "shared-test-token", Duration.ofSeconds(1), Duration.ofSeconds(2));
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private LlmExplainDTO.Request request() {
        return new LlmExplainDTO.Request(9L, "v1.0",
                new LlmExplainDTO.Rule("SQLI-003", "SQL_INJECTION", "CWE-89",
                        "CRITICAL", "SQL 삽입", "동적 SQL"),
                new LlmExplainDTO.Location("src/users.ts", "TypeScript", 2, 2),
                new LlmExplainDTO.CodeContext(1, 2, List.of("1: const id = input;", "2: query(id);")));
    }

    @Test
    void sendsTokenAndParsesFastApiResponse() {
        AtomicReference<String> token = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        server.createContext("/internal/llm/explain", exchange -> {
            token.set(exchange.getRequestHeaders().getFirst("X-Internal-Token"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200, """
                    {"findingId":9,"modelName":"mock","promptVersion":"v1.0",
                     "promptTokens":0,"completionTokens":0,"verdict":"TRUE_POSITIVE",
                     "confidence":90,"explanation":"설명","remediation":"개선",
                     "fixedCode":"fixed","rawResponse":"{}"}
                    """);
        });

        LlmExplainDTO.Response response = client.explain(request());

        assertThat(token.get()).isEqualTo("shared-test-token");
        assertThat(body.get()).contains("\"findingId\":9", "\"language\":\"TypeScript\"");
        assertThat(response.explanation()).isEqualTo("설명");
    }

    @Test
    void schemaErrorKeepsRawResponseAndIsNotRetryable() {
        server.createContext("/internal/llm/explain", exchange -> respond(exchange, 422,
                "{\"findingId\":9,\"errorCode\":\"LLM_SCHEMA_INVALID\",\"rawResponse\":\"invalid\"}"));

        LlmClient.LlmCallException error = catchThrowableOfType(
                () -> client.explain(request()), LlmClient.LlmCallException.class);

        assertThat(error.isRetryable()).isFalse();
        assertThat(error.getRawResponse()).isEqualTo("invalid");
    }

    @Test
    void unavailableErrorIsRetryable() {
        server.createContext("/internal/llm/explain", exchange -> respond(exchange, 503,
                "{\"detail\":\"LLM client is not configured\"}"));

        LlmClient.LlmCallException error = catchThrowableOfType(
                () -> client.explain(request()), LlmClient.LlmCallException.class);

        assertThat(error.isRetryable()).isTrue();
    }

    private void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String json)
            throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
