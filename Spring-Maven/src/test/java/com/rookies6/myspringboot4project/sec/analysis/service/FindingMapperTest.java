package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import com.rookies6.myspringboot4project.sec.scanner.dto.RawFinding;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FindingMapperTest {

    @Test
    void copiesAllFindingFieldsAndDefaultsStatusToOpen() {
        RawFinding raw = new RawFinding(
                "src/main/java/Login.java",
                "SQLI-001",
                VulnerabilityType.SQL_INJECTION,
                "CWE-89",
                Severity.CRITICAL,
                3,
                5,
                "String sql = \"SELECT * FROM users WHERE id = \" + userId;"
        );

        FindingVulnerability finding = FindingMapper.toEntity(raw);

        assertThat(finding.getRuleId()).isEqualTo(raw.ruleId());
        assertThat(finding.getVulnerabilityType()).isEqualTo(raw.vulnerabilityType());
        assertThat(finding.getCweId()).isEqualTo(raw.cweId());
        assertThat(finding.getSeverity()).isEqualTo(raw.severity());
        assertThat(finding.getStartLine()).isEqualTo(raw.startLine());
        assertThat(finding.getEndLine()).isEqualTo(raw.endLine());
        assertThat(finding.getCodeSnippet()).isEqualTo(raw.codeSnippet());
        assertThat(finding.getStatus()).isEqualTo(FindingStatus.OPEN);
    }

    @Test
    void leavesFileAssociationForTheCaller() {
        RawFinding raw = new RawFinding(
                "src/test/java/LoginTest.java",
                "SECRET-001",
                VulnerabilityType.HARDCODED_SECRET,
                "CWE-798",
                Severity.LOW,
                2,
                2,
                "String password = \"example-secret\";"
        );

        FindingVulnerability finding = FindingMapper.toEntity(raw);

        assertThat(finding.getSeverity()).isEqualTo(Severity.LOW);
        assertThat(finding.getAnalysisFile()).isNull();
    }
}