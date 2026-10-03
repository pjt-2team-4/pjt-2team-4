package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.dto.LlmExplainDTO;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.common.enums.Language;
import com.rookies6.myspringboot4project.sec.scanner.rule.RuleCatalog;
import com.rookies6.myspringboot4project.sec.scanner.rule.SecurityRule;

import java.util.ArrayList;
import java.util.List;

final class LlmRequestFactory {
    private static final int CONTEXT_PADDING = 20;

    private LlmRequestFactory() {
    }

    static LlmExplainDTO.Request from(FindingVulnerability finding, String promptVersion) {
        AnalysisFile file = finding.getAnalysisFile();
        SecurityRule rule = RuleCatalog.RULES.stream()
                .filter(candidate -> candidate.getRuleId().equals(finding.getRuleId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("탐지 룰을 찾을 수 없습니다: " + finding.getRuleId()));

        return new LlmExplainDTO.Request(
                finding.getId(), promptVersion,
                new LlmExplainDTO.Rule(finding.getRuleId(),
                        finding.getVulnerabilityType().name(), finding.getCweId(),
                        finding.getSeverity().name(), rule.getTitle(), rule.getDescription()),
                new LlmExplainDTO.Location(file.getRelativePath(),
                        Language.fromFileName(file.getFileName()).getDisplayName(),
                        finding.getStartLine(), finding.getEndLine()),
                context(file.getContent(), finding.getStartLine(), finding.getEndLine()));
    }

    static LlmExplainDTO.CodeContext context(String content, int startLine, int endLine) {
        String[] source = content.split("\\R", -1);
        int from = Math.max(1, startLine - CONTEXT_PADDING);
        int to = Math.min(source.length, endLine + CONTEXT_PADDING);
        List<String> numbered = new ArrayList<>(to - from + 1);
        for (int line = from; line <= to; line++) {
            numbered.add(line + ": " + source[line - 1]);
        }
        return new LlmExplainDTO.CodeContext(from, to, List.copyOf(numbered));
    }
}
