package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.scanner.dto.RawFinding;

public final class FindingMapper {

    private FindingMapper() {
    }

    public static FindingVulnerability toEntity(RawFinding raw) {
        return FindingVulnerability.builder()
                .ruleId(raw.ruleId())
                .vulnerabilityType(raw.vulnerabilityType())
                .cweId(raw.cweId())
                .severity(raw.severity())
                .startLine(raw.startLine())
                .endLine(raw.endLine())
                .codeSnippet(raw.codeSnippet())
                .build();
    }
}