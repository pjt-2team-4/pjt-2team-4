package com.rookies6.myspringboot4project.sec.analysisfile.dto;

import com.rookies6.myspringboot4project.sec.common.enums.FindingVulnerability;
import lombok.Getter;

@Getter
public class FindingMarkerDto {

    private final Long findingId;
    private final int startLine;
    private final int endLine;
    private final String severity;
    private final String label;

    public FindingMarkerDto(FindingVulnerability finding) {

        this.findingId = finding.getId();
        this.startLine = finding.getStartLine();
        this.endLine = finding.getEndLine();

        this.severity = finding.getSeverity() != null
                ? finding.getSeverity().name()
                : null;

        this.label = finding.getVulnerabilityType() != null
                ? finding.getVulnerabilityType().name()
                : null;
    }
}
