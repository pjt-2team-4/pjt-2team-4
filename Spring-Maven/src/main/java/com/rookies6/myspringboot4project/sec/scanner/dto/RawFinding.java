package com.rookies6.myspringboot4project.sec.scanner.dto;

import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;

/** Scanner가 생성하는 취약점 탐지 결과. */
public record RawFinding(
        String relativePath,
        String ruleId,
        VulnerabilityType vulnerabilityType,
        String cweId,
        Severity severity,
        int startLine,
        int endLine,
        String codeSnippet
) {
    public RawFinding {
        if (startLine < 1 || endLine < startLine) {
            throw new IllegalArgumentException("유효하지 않은 탐지 라인 범위입니다.");
        }
    }
}

