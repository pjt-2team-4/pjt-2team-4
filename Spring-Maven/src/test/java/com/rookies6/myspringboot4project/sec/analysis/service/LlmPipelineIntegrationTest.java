package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.dto.LlmExplainDTO;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.common.enums.LlmAnalysisStatus;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import com.rookies6.myspringboot4project.user.entity.User;
import com.rookies6.myspringboot4project.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class LlmPipelineIntegrationTest {
    @Autowired private AnalysisWorker worker;
    @Autowired private AnalysisRequestRepository requests;
    @Autowired private FindingVulnerabilityRepository findings;
    @Autowired private UserRepository users;
    @Autowired private AnalysisService analysisService;
    @Autowired private FindingService findingService;
    @Autowired private AnalysisStateService stateService;
    @Autowired private AnalysisProgressStore progressStore;
    @Autowired @Qualifier("llmTaskExecutor") private ExecutorService llmExecutor;
    @MockitoBean private LlmClient client;

    @Test
    void findingMovesThroughExplainingAndPersistsSuccess() throws InterruptedException {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(client.explain(any())).thenAnswer(invocation -> {
            LlmExplainDTO.Request request = invocation.getArgument(0);
            entered.countDown();
            if (!release.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("LLM test response was not released");
            }
            return response(request.findingId());
        });
        Long id = analysis("String password = \"abc123\";");

        worker.runAnalysisPipeline(id, Set.of(VulnerabilityType.values()));
        assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
        awaitStatus(id, AnalysisStatus.EXPLAINING);
        assertThat(analysisService.getAnalysisStatus(id).getTotalFindings()).isEqualTo(1);
        release.countDown();

        awaitStatus(id, AnalysisStatus.COMPLETED);
        var saved = findings.findForExplanation(id);
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getLlmAnalysis().getStatus()).isEqualTo(LlmAnalysisStatus.SUCCESS);
        assertThat(saved.get(0).getLlmAnalysis().getExplanation()).isEqualTo("설명");
        assertThat(saved.get(0).getLlmAnalysis().getFixedCode()).isEqualTo("fixed");
        var detail = findingService.getFinding(saved.get(0).getId());
        assertThat(detail.getLlmAnalysis().getStatus()).isEqualTo("SUCCESS");
        assertThat(detail.getLlmAnalysis().getExplanation()).isEqualTo("설명");
        assertThat(detail.getAfterCode().getLines()).containsExactly("fixed");
    }

    @Test
    void noFindingsSkipsExplaining() {
        Long id = analysis("int safe = 1;");

        worker.runAnalysisPipeline(id, Set.of(VulnerabilityType.values()));

        awaitStatus(id, AnalysisStatus.COMPLETED);
        assertThat(requests.findById(id).orElseThrow().getTotalFindings()).isZero();
        verifyNoInteractions(client);
    }

    @Test
    void unavailableLlmFailsOnlyItsFinding() {
        when(client.explain(any())).thenThrow(new LlmClient.LlmCallException("HTTP 503", true, null));
        Long id = analysis("String password = \"abc123\";");

        worker.runAnalysisPipeline(id, Set.of(VulnerabilityType.values()));

        awaitStatus(id, AnalysisStatus.COMPLETED);
        var saved = findings.findForExplanation(id);
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getLlmAnalysis().getStatus()).isEqualTo(LlmAnalysisStatus.FAILED);
        verify(client, times(2)).explain(any());
    }

    @Test
    void wholePhaseTimeoutFailsAnalysisAndPendingLlmRow() throws InterruptedException {
        User user = users.save(User.builder()
                .email(UUID.randomUUID() + "@example.com").password("hash").build());
        AnalysisRequest request = AnalysisRequest.create(user, "Timeout test", "JAVA", 30);
        AnalysisFile file = AnalysisFile.builder()
                .relativePath("src/Timeout.java").fileName("Timeout.java")
                .language("JAVA").content("String password = \"abc123\";").lineCount(1).build();
        request.addFile(file);
        file.addFinding(FindingVulnerability.builder()
                .ruleId("SECRET-001").vulnerabilityType(VulnerabilityType.HARDCODED_SECRET)
                .cweId("CWE-798").severity(com.rookies6.myspringboot4project.sec.common.enums.Severity.HIGH)
                .startLine(1).endLine(1).codeSnippet("String password = \"abc123\";").build());
        Long id = requests.save(request).getId();
        stateService.markScanning(id);
        stateService.startExplaining(id, 1);
        progressStore.start(id, 1);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(client.explain(any())).thenAnswer(invocation -> {
            entered.countDown();
            release.await(2, TimeUnit.SECONDS);
            LlmExplainDTO.Request call = invocation.getArgument(0);
            return response(call.findingId());
        });
        LlmAnalysisService shortPhase = new LlmAnalysisService(
                stateService, progressStore, client, llmExecutor, Duration.ofSeconds(1));

        assertThat(shortPhase.explainAll(id)).isFalse();
        assertThat(entered.getCount()).isZero();
        release.countDown();

        assertThat(requests.findById(id).orElseThrow().getStatus()).isEqualTo(AnalysisStatus.FAILED);
        assertThat(findings.findForExplanation(id).get(0).getLlmAnalysis().getStatus())
                .isEqualTo(LlmAnalysisStatus.FAILED);
    }

    private Long analysis(String content) {
        User user = users.save(User.builder()
                .email(UUID.randomUUID() + "@example.com").password("hash").build());
        AnalysisRequest request = AnalysisRequest.create(user, "LLM integration test", "JAVA", 30);
        request.addFile(AnalysisFile.builder()
                .relativePath("src/Demo.java").fileName("Demo.java")
                .language("JAVA").content(content).lineCount(1).build());
        return requests.save(request).getId();
    }

    private void awaitStatus(Long id, AnalysisStatus expected) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(5));
        while (Instant.now().isBefore(deadline)) {
            AnalysisStatus current = requests.findById(id).orElseThrow().getStatus();
            if (current == expected) {
                return;
            }
            if (current == AnalysisStatus.FAILED && expected != AnalysisStatus.FAILED) {
                throw new AssertionError("Unexpected analysis failure: "
                        + requests.findById(id).orElseThrow().getErrorMessage());
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }
        throw new AssertionError("Analysis did not reach " + expected);
    }

    private LlmExplainDTO.Response response(Long findingId) {
        return new LlmExplainDTO.Response(findingId, "mock", "v1.0", 0, 0,
                "TRUE_POSITIVE", 90, "설명", null, null, "개선", "fixed", "{}");
    }
}
