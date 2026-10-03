package com.rookies6.myspringboot4project.sec.analysis.dto;

import java.util.List;

public final class LlmExplainDTO {
    private LlmExplainDTO() {
    }

    public record Request(Long findingId, String promptVersion, Rule rule,
                          Location location, CodeContext codeContext) {
    }

    public record Rule(String ruleId, String vulnerabilityType, String cweId,
                       String severity, String title, String description) {
    }

    public record Location(String relativePath, String language, int startLine, int endLine) {
    }

    public record CodeContext(int startLine, int endLine, List<String> lines) {
    }

    public record Response(Long findingId, String modelName, String promptVersion,
                           Integer promptTokens, Integer completionTokens,
                           String verdict, Integer confidence, String explanation,
                           String riskDescription, String attackScenario,
                           String remediation, String fixedCode, String rawResponse) {
    }

    public record ErrorResponse(Long findingId, String errorCode, String message,
                                String rawResponse) {
    }

    public record HealthResponse(String status, String model, String promptVersion) {
    }
}
