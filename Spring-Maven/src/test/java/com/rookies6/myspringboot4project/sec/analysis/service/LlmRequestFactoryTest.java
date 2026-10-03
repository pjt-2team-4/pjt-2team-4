package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LlmRequestFactoryTest {
    @Test
    void includesTwentyLinesAroundFindingWithLineNumbers() {
        String code = IntStream.rangeClosed(1, 100)
                .mapToObj(line -> "line " + line)
                .collect(Collectors.joining("\n"));

        var context = LlmRequestFactory.context(code, 50, 52);

        assertThat(context.startLine()).isEqualTo(30);
        assertThat(context.endLine()).isEqualTo(72);
        assertThat(context.lines()).hasSize(43);
        assertThat(context.lines().get(20)).isEqualTo("50: line 50");
    }

    @Test
    void usesSavedSeverityAndLanguageDisplayName() {
        FindingVulnerability finding = mock(FindingVulnerability.class);
        AnalysisFile file = mock(AnalysisFile.class);
        when(finding.getId()).thenReturn(9L);
        when(finding.getAnalysisFile()).thenReturn(file);
        when(finding.getRuleId()).thenReturn("SQLI-003");
        when(finding.getVulnerabilityType()).thenReturn(VulnerabilityType.SQL_INJECTION);
        when(finding.getCweId()).thenReturn("CWE-89");
        when(finding.getSeverity()).thenReturn(Severity.HIGH);
        when(finding.getStartLine()).thenReturn(1);
        when(finding.getEndLine()).thenReturn(1);
        when(file.getRelativePath()).thenReturn("src/demo.spec.ts");
        when(file.getFileName()).thenReturn("demo.spec.ts");
        when(file.getContent()).thenReturn("const query = `SELECT * FROM users WHERE id = ${id}`;");

        var request = LlmRequestFactory.from(finding, "v1.0");

        assertThat(request.rule().severity()).isEqualTo("HIGH");
        assertThat(request.location().language()).isEqualTo("TypeScript");
        assertThat(request.rule().title()).isNotBlank();
    }
}
