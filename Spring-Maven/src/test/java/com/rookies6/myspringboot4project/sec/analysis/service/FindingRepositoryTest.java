package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysis.entity.LlmAnalysis;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import com.rookies6.myspringboot4project.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FindingRepositoryTest {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FindingVulnerabilityRepository repository;

    @Test
    void listFiltersByFileAndSeverityAndLoadsOptionalLlm() {
        User user = User.builder().email("finding-test@example.com").password("hash").build();
        entityManager.persist(user);
        AnalysisRequest request = AnalysisRequest.create(user, "sample", "JAVA", 30);
        AnalysisFile first = file("src/A.java", "String password = \"abc\";");
        AnalysisFile second = file("src/B.java", "String password = \"def\";");
        request.addFile(first);
        request.addFile(second);
        FindingVulnerability firstFinding = finding(Severity.HIGH);
        first.addFinding(firstFinding);
        LlmAnalysis llm = LlmAnalysis.pending(firstFinding);
        second.addFinding(finding(Severity.LOW));
        entityManager.persist(request);
        entityManager.flush();
        entityManager.clear();

        var page = repository.findAllByAnalysisId(request.getId(), null, null, null,
                first.getId(), List.of(Severity.HIGH), PageRequest.of(0, 1));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getAnalysisFile().getId()).isEqualTo(first.getId());
        assertThat(page.getContent().get(0).getLlmAnalysis()).isNotNull();

        var secondPage = repository.findAllByAnalysisId(request.getId(), null, null, null,
                null, List.of(Severity.HIGH, Severity.LOW), PageRequest.of(1, 1));
        assertThat(secondPage.getTotalElements()).isEqualTo(2);
        assertThat(secondPage.getTotalPages()).isEqualTo(2);
        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.getContent().get(0).getAnalysisFile().getId()).isEqualTo(second.getId());
        assertThat(secondPage.getContent().get(0).getLlmAnalysis()).isNull();

        repository.delete(repository.findDetailById(firstFinding.getId()).orElseThrow());
        entityManager.flush();
        assertThat(entityManager.find(LlmAnalysis.class, llm.getId())).isNull();
    }

    private static AnalysisFile file(String path, String content) {
        return AnalysisFile.builder()
                .relativePath(path)
                .fileName(path.substring(path.lastIndexOf('/') + 1))
                .language("JAVA")
                .content(content)
                .lineCount(1)
                .build();
    }

    private static FindingVulnerability finding(Severity severity) {
        return FindingVulnerability.builder()
                .ruleId("SECRET-001")
                .vulnerabilityType(VulnerabilityType.HARDCODED_SECRET)
                .cweId("CWE-798")
                .severity(severity)
                .startLine(1)
                .endLine(1)
                .codeSnippet("password")
                .build();
    }
}
