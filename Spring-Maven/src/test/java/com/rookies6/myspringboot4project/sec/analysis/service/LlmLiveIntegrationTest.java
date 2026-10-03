package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.common.enums.LlmAnalysisStatus;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "CODEGUARD_LLM_LIVE_TEST", matches = "true")
class LlmLiveIntegrationTest {
    @Autowired private AnalysisWorker worker;
    @Autowired private AnalysisRequestRepository requests;
    @Autowired private FindingVulnerabilityRepository findings;
    @Autowired private UserRepository users;

    @Test
    void scannerFindingIsExplainedByRunningFastApiMock() throws InterruptedException {
        User user = users.save(User.builder()
                .email(UUID.randomUUID() + "@example.com").password("hash").build());
        AnalysisRequest request = AnalysisRequest.create(user, "Live LLM test", "JAVA", 30);
        request.addFile(AnalysisFile.builder()
                .relativePath("src/Live.java").fileName("Live.java").language("JAVA")
                .content("String password = \"abc123\";").lineCount(1).build());
        Long id = requests.save(request).getId();

        worker.runAnalysisPipeline(id, Set.of(VulnerabilityType.values()));

        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (Instant.now().isBefore(deadline)) {
            AnalysisRequest current = requests.findById(id).orElseThrow();
            if (current.getStatus() == AnalysisStatus.COMPLETED) {
                break;
            }
            if (current.getStatus() == AnalysisStatus.FAILED) {
                throw new AssertionError(current.getErrorMessage());
            }
            Thread.sleep(50);
        }
        assertThat(requests.findById(id).orElseThrow().getStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        var saved = findings.findForExplanation(id);
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getLlmAnalysis().getStatus()).isEqualTo(LlmAnalysisStatus.SUCCESS);
        assertThat(saved.get(0).getLlmAnalysis().getModelName()).isEqualTo("mock");
        assertThat(saved.get(0).getLlmAnalysis().getExplanation()).isNotBlank();
    }
}
