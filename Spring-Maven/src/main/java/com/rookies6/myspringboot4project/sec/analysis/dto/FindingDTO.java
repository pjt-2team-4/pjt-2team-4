package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysis.entity.LlmAnalysis;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import com.rookies6.myspringboot4project.sec.scanner.rule.RuleCatalog;
import com.rookies6.myspringboot4project.sec.scanner.rule.SecurityRule;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * 취약점 응답 DTO.
 * Scanner 가 만든 값(detection)과 LLM 이 만든 값(llmAnalysis)을 구분해 내려준다.
 */
public class FindingDTO {

    private static final int SUMMARY_LENGTH = 120;

    // ───────────── 12번 목록 ─────────────
    @Getter
    @Builder
    public static class ListItem {
        // Scanner 출처
        private final Long findingId;
        private final Long fileId;
        private final String relativePath;
        private final String ruleId;
        private final String ruleTitle;
        private final String vulnerabilityType;
        private final String displayName;
        private final String cweId;
        private final String severity;
        private final String status;
        private final Integer startLine;
        private final Integer endLine;
        private final String codeSnippet;
        // LLM 출처 (요약만)
        private final String llmStatus;
        private final String verdict;
        private final Integer confidence;
        private final String summary;

        public static ListItem from(FindingVulnerability f) {
            AnalysisFile file = f.getAnalysisFile();
            SecurityRule rule = findRule(f.getRuleId());
            LlmAnalysis llm = f.getLlmAnalysis();

            return ListItem.builder()
                    .findingId(f.getId())
                    .fileId(file.getId())
                    .relativePath(file.getRelativePath())
                    .ruleId(f.getRuleId())
                    .ruleTitle(rule == null ? null : rule.getTitle())
                    .vulnerabilityType(f.getVulnerabilityType().name())
                    .displayName(f.getVulnerabilityType().getDisplayName())
                    .cweId(f.getCweId())
                    .severity(f.getSeverity().name())
                    .status(f.getStatus().name())
                    .startLine(f.getStartLine())
                    .endLine(f.getEndLine())
                    .codeSnippet(f.getCodeSnippet())
                    .llmStatus(llm == null ? "PENDING" : llm.getStatus().name())
                    .verdict(llm == null || llm.getVerdict() == null ? null : llm.getVerdict().name())
                    .confidence(llm == null ? null : llm.getConfidence())
                    .summary(summarize(llm))
                    .build();
        }
    }

    // ───────────── 13번 상세 ─────────────
    @Getter
    @Builder
    public static class Detail {
        private final Long findingId;
        private final Long analysisId;
        private final FileInfo file;
        private final Detection detection;
        private final String vulnerabilityType;
        private final String displayName;
        private final String cweId;
        private final String severity;
        private final String status;
        private final Integer startLine;
        private final Integer endLine;
        private final LlmAnalysisDto llmAnalysis;
        private final CodeBlock beforeCode;
        private final CodeBlock afterCode;

        public static Detail from(FindingVulnerability f) {
            AnalysisFile file = f.getAnalysisFile();
            LlmAnalysis llm = f.getLlmAnalysis();

            return Detail.builder()
                    .findingId(f.getId())
                    .analysisId(file.getAnalysisRequest().getId())
                    .file(new FileInfo(file.getId(), file.getRelativePath(), file.getLanguage()))
                    .detection(Detection.of(findRule(f.getRuleId()), f.getRuleId(), f.getCodeSnippet()))
                    .vulnerabilityType(f.getVulnerabilityType().name())
                    .displayName(f.getVulnerabilityType().getDisplayName())
                    .cweId(f.getCweId())
                    .severity(f.getSeverity().name())
                    .status(f.getStatus().name())
                    .startLine(f.getStartLine())
                    .endLine(f.getEndLine())
                    .llmAnalysis(LlmAnalysisDto.from(llm))
                    .beforeCode(CodeBlock.of(f.getStartLine(), f.getCodeSnippet()))
                    .afterCode(llm == null ? null : CodeBlock.of(f.getStartLine(), llm.getFixedCode()))
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class FileInfo {
        private final Long fileId;
        private final String relativePath;
        private final String language;
    }

    /** Scanner 가 어떤 근거로 탐지했는지 */
    @Getter
    @AllArgsConstructor
    public static class Detection {
        private final String ruleId;
        private final String ruleTitle;
        private final String ruleDescription;
        private final String matchedText;

        public static Detection of(SecurityRule rule, String ruleId, String matchedText) {
            if (rule == null) {
                return new Detection(ruleId, null, null, matchedText);
            }
            return new Detection(rule.getRuleId(), rule.getTitle(), rule.getDescription(), matchedText);
        }
    }

    /** LLM 이 만든 값. 아직 없으면 status 만 PENDING */
    @Getter
    @Builder
    public static class LlmAnalysisDto {
        private final String status;
        private final String verdict;
        private final Integer confidence;
        private final String explanation;
        private final String riskDescription;
        private final String attackScenario;
        private final String remediation;
        private final String modelName;
        private final String promptVersion;

        public static LlmAnalysisDto from(LlmAnalysis llm) {
            if (llm == null) {
                return LlmAnalysisDto.builder().status("PENDING").build();
            }
            return LlmAnalysisDto.builder()
                    .status(llm.getStatus().name())
                    .verdict(llm.getVerdict() == null ? null : llm.getVerdict().name())
                    .confidence(llm.getConfidence())
                    .explanation(llm.getExplanation())
                    .riskDescription(llm.getRiskDescription())
                    .attackScenario(llm.getAttackScenario())
                    .remediation(llm.getRemediation())
                    .modelName(llm.getModelName())
                    .promptVersion(llm.getPromptVersion())
                    .build();
        }
    }

    /** BEFORE / AFTER 비교 영역 */
    @Getter
    @AllArgsConstructor
    public static class CodeBlock {
        private final Integer startLine;
        private final List<String> lines;

        public static CodeBlock of(Integer startLine, String code) {
            if (code == null) {
                return null;
            }
            return new CodeBlock(startLine, Arrays.asList(code.split("\n", -1)));
        }
    }

    // ───────────── 14번 상태 변경 ─────────────
    @Getter
    @NoArgsConstructor
    public static class StatusUpdateRequest {
        @NotNull(message = "변경할 상태는 필수입니다.")
        private FindingStatus status;   // OPEN / RESOLVED / IGNORED
    }

    @Getter
    @AllArgsConstructor
    public static class StatusUpdateResponse {
        private final Long findingId;
        private final String status;

        public static StatusUpdateResponse from(FindingVulnerability f) {
            return new StatusUpdateResponse(f.getId(), f.getStatus().name());
        }
    }

    // ───────────── 헬퍼 ─────────────
    private static SecurityRule findRule(String ruleId) {
        try {
            return RuleCatalog.byId(ruleId);
        } catch (IllegalArgumentException e) {
            return null;   // 폐기된 룰이나 더미 데이터 방어
        }
    }

    private static String summarize(LlmAnalysis llm) {
        if (llm == null || llm.getExplanation() == null) {
            return null;
        }
        String text = llm.getExplanation();
        return text.length() <= SUMMARY_LENGTH ? text : text.substring(0, SUMMARY_LENGTH) + "...";
    }
}