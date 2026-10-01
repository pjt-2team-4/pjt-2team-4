package com.rookies6.myspringboot4project.sec.analysis.dto;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysis.entity.LlmAnalysis;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FindingDTOTest {

    @Test
    void detailUsesFileContentAndReportsPendingLlm() {
        FindingVulnerability finding = finding();

        FindingDTO.Detail detail = FindingDTO.Detail.from(finding);

        assertThat(detail.getBeforeCode().getStartLine()).isEqualTo(2);
        assertThat(detail.getBeforeCode().getLines()).containsExactly("String password = \"abc123\";");
        assertThat(detail.getDetection().getMatchedText()).isEqualTo("password = \"abc123\"");
        assertThat(detail.getLlmAnalysis().getStatus()).isEqualTo("PENDING");
        assertThat(detail.getAfterCode()).isNull();
    }

    @Test
    void listSummaryHasAtMost120CharactersAndPendingLinksBothSides() {
        FindingVulnerability finding = finding();
        LlmAnalysis llm = LlmAnalysis.pending(finding);
        ReflectionTestUtils.setField(llm, "explanation", "a".repeat(130));

        FindingDTO.ListItem item = FindingDTO.ListItem.from(finding);

        assertThat(finding.getLlmAnalysis()).isSameAs(llm);
        assertThat(llm.getFinding()).isSameAs(finding);
        assertThat(item.getSummary()).hasSize(120);
    }

    private static FindingVulnerability finding() {
        AnalysisFile file = AnalysisFile.builder()
                .relativePath("src/Secrets.java")
                .fileName("Secrets.java")
                .language("JAVA")
                .content("class Secrets {\nString password = \"abc123\";\n}")
                .lineCount(3)
                .build();
        file.assignTo(mock(AnalysisRequest.class));

        FindingVulnerability finding = FindingVulnerability.builder()
                .ruleId("SECRET-001")
                .vulnerabilityType(VulnerabilityType.HARDCODED_SECRET)
                .cweId("CWE-798")
                .severity(Severity.HIGH)
                .startLine(2)
                .endLine(2)
                .codeSnippet("String password = \"abc123\";")
                .build();
        file.addFinding(finding);
        return finding;
    }
}
