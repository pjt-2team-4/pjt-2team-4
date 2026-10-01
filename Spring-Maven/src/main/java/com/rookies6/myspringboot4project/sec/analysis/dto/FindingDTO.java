//package com.rookies6.myspringboot4project.sec.analysis.dto;
//
////import com.rookies6.myspringboot4project.sec.analysis.entity.LlmAnalysis;
//import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
//import com.rookies6.myspringboot4project.sec.scanner.rule.RuleCatalog;
//import com.rookies6.myspringboot4project.sec.scanner.rule.SecurityRule;
//import jakarta.validation.constraints.NotBlank;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.util.Arrays;
//import java.util.List;
//
///**
// * 취약점 관련 DTO.
// *
// * 응답에서 "Scanner 가 만든 값(detection)"과 "LLM 이 만든 값(llmAnalysis)"을 구분해 노출한다.
// * LLM 생성이 실패해도 detection 은 항상 정상적으로 내려간다.
// */
//public class FindingDTO {
//
//    private static final int SUMMARY_LENGTH = 120;
//
//    // ────────────────────────── 12번 API: 취약점 목록 ──────────────────────────
//
//    /** GET /api/v1/analyses/{analysisId}/findings */
//    @Getter
//    @Builder
//    public static class ListItem {
//
//        // Scanner 출처
//        private final Long findingId;
//        private final Long fileId;
//        private final String filePath;
//        private final String ruleId;
//        private final String ruleTitle;
//        private final String vulnerabilityType;
//        private final String displayName;
//        private final String cweId;
//        private final String severity;
//        private final String status;
//        private final Integer startLine;
//        private final Integer endLine;
//        private final String codeSnippet;
//
//        // LLM 출처 (배지 표시용 요약만)
//        private final String llmStatus;
//        private final String verdict;
//        private final Integer confidence;
//        private final String summary;
//
//        public static ListItem from(Finding finding) {
//            SecurityRule rule = findRule(finding.getRuleId());
//            LlmAnalysis llm = finding.getLlmAnalysis();
//
//            return ListItem.builder()
//                    .findingId(finding.getId())
//                    .fileId(finding.getAnalysisFile().getId())
//                    .filePath(finding.getAnalysisFile().getFilePath())
//                    .ruleId(finding.getRuleId())
//                    .ruleTitle(rule == null ? null : rule.getTitle())
//                    .vulnerabilityType(finding.getVulnerabilityType())
//                    .displayName(displayNameOf(finding.getVulnerabilityType()))
//                    .cweId(finding.getCweId())
//                    .severity(finding.getSeverity())
//                    .status(finding.getStatus())
//                    .startLine(finding.getStartLine())
//                    .endLine(finding.getEndLine())
//                    .codeSnippet(finding.getCodeSnippet())
//                    .llmStatus(llm == null ? "PENDING" : llm.getStatus())
//                    .verdict(llm == null ? null : llm.getVerdict())
//                    .confidence(llm == null ? null : llm.getConfidence())
//                    .summary(summarize(llm))
//                    .build();
//        }
//    }
//
//    // ────────────────────────── 13번 API: 취약점 상세 ──────────────────────────
//
//    /** GET /api/v1/findings/{findingId} */
//    @Getter
//    @Builder
//    public static class Detail {
//
//        private final Long findingId;
//        private final Long analysisId;
//        private final FileInfo file;
//        private final Detection detection;
//        private final String vulnerabilityType;
//        private final String displayName;
//        private final String cweId;
//        private final String severity;
//        private final String status;
//        private final Integer startLine;
//        private final Integer endLine;
//        private final LlmAnalysisDto llmAnalysis;
//        private final CodeBlock beforeCode;
//        private final CodeBlock afterCode;
//
//        public static Detail from(FindingVulnerability finding) {
//            var file = finding.getAnalysisFile();
//            SecurityRule rule = findRule(finding.getRuleId());
//            LlmAnalysis llm = finding.getLlmAnalysis();
//
//            return Detail.builder()
//                    .findingId(finding.getId())
//                    .analysisId(file.getAnalysisRequest().getId())
//                    .file(new FileInfo(file.getId(), file.getFilePath(), file.getLanguage()))
//                    .detection(Detection.of(rule, finding.getRuleId(), finding.getCodeSnippet()))
//                    .vulnerabilityType(finding.getVulnerabilityType())
//                    .displayName(displayNameOf(finding.getVulnerabilityType()))
//                    .cweId(finding.getCweId())
//                    .severity(finding.getSeverity())
//                    .status(finding.getStatus())
//                    .startLine(finding.getStartLine())
//                    .endLine(finding.getEndLine())
//                    .llmAnalysis(LlmAnalysisDto.from(llm))
//                    .beforeCode(CodeBlock.of(finding.getStartLine(), finding.getCodeSnippet()))
//                    .afterCode(llm == null || llm.getFixedCode() == null
//                            ? null
//                            : CodeBlock.of(finding.getStartLine(), llm.getFixedCode()))
//                    .build();
//        }
//    }
//
//    @Getter
//    @AllArgsConstructor
//    public static class FileInfo {
//        private final Long fileId;
//        private final String filePath;
//        private final String language;
//    }
//
//    /** Scanner 가 어떤 근거로 탐지했는지 — 화면에서 AI 설명과 구분해 표시한다 */
//    @Getter
//    @AllArgsConstructor
//    public static class Detection {
//
//        private final String ruleId;
//        private final String ruleTitle;
//        private final String ruleDescription;
//        private final String matchedText;
//
//        public static Detection of(SecurityRule rule, String ruleId, String matchedText) {
//            if (rule == null) {
//                return new Detection(ruleId, null, null, matchedText);
//            }
//            return new Detection(rule.getRuleId(), rule.getTitle(), rule.getDescription(), matchedText);
//        }
//    }
//
//    /** LLM 이 만든 값. 생성 실패 시 status 만 FAILED 로 내려간다 */
//    @Getter
//    @Builder
//    public static class LlmAnalysisDto {
//
//        private final String status;           // PENDING / SUCCESS / FAILED
//        private final String verdict;          // TRUE_POSITIVE / FALSE_POSITIVE / UNCERTAIN
//        private final Integer confidence;      // 화면의 "AI 신뢰도 97%"
//        private final String explanation;      // ※ 엔티티 컬럼명은 cause
//        private final String riskDescription;
//        private final String attackScenario;
//        private final String remediation;
//        private final String modelName;
//        private final String promptVersion;
//        private final String errorMessage;
//
//        public static LlmAnalysisDto from(LlmAnalysis llm) {
//            if (llm == null) {
//                return LlmAnalysisDto.builder().status("PENDING").build();
//            }
//            return LlmAnalysisDto.builder()
//                    .status(llm.getStatus())
//                    .verdict(llm.getVerdict())
//                    .confidence(llm.getConfidence())
//                    .explanation(llm.getCause())
//                    .riskDescription(llm.getRiskDescription())
//                    .attackScenario(llm.getAttackScenario())
//                    .remediation(llm.getRemediation())
//                    .modelName(llm.getModelName())
//                    .promptVersion(llm.getPromptVersion())
//                    .build();
//        }
//
//        /** 화면에 "오탐 가능성 있음" 배지를 띄울지 */
//        public boolean isLikelyFalsePositive() {
//            return "FALSE_POSITIVE".equals(verdict) || (confidence != null && confidence < 50);
//        }
//    }
//
//    /** BEFORE / AFTER 비교 영역 */
//    @Getter
//    @AllArgsConstructor
//    public static class CodeBlock {
//
//        private final Integer startLine;
//        private final List<String> lines;
//
//        public static CodeBlock of(Integer startLine, String code) {
//            if (code == null) {
//                return null;
//            }
//            return new CodeBlock(startLine, Arrays.asList(code.split("\n", -1)));
//        }
//    }
//
//    // ────────────────────────── 14번 API: 상태 변경 ──────────────────────────
//
//    /** PATCH /api/v1/findings/{findingId}/status */
//    @Getter
//    @NoArgsConstructor
//    public static class StatusUpdateRequest {
//
//        @NotBlank(message = "변경할 상태는 필수입니다.")
//        private String status;   // OPEN / RESOLVED / IGNORED
//    }
//
//    @Getter
//    @AllArgsConstructor
//    public static class StatusUpdateResponse {
//
//        private final Long findingId;
//        private final String status;
//
//        public static StatusUpdateResponse from(FindingVulnerability finding) {
//            return new StatusUpdateResponse(finding.getId(), finding.getStatus());
//        }
//    }
//
//    // ────────────────────────── 공통 헬퍼 ──────────────────────────
//
//    private static SecurityRule findRule(String ruleId) {
//        try {
//            return RuleCatalog.byId(ruleId);
//        } catch (IllegalArgumentException e) {
//            return null;   // 폐기된 룰로 만들어진 과거 데이터 방어
//        }
//    }
//
//    private static String displayNameOf(String vulnerabilityType) {
//        try {
//            return com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType
//                    .valueOf(vulnerabilityType).getDisplayName();
//        } catch (IllegalArgumentException | NullPointerException e) {
//            return vulnerabilityType;
//        }
//    }
//
//    private static String summarize(LlmAnalysis llm) {
//        if (llm == null || llm.getCause() == null) {
//            return null;
//        }
//        String cause = llm.getCause();
//        return cause.length() <= SUMMARY_LENGTH ? cause : cause.substring(0, SUMMARY_LENGTH) + "...";
//    }
//}