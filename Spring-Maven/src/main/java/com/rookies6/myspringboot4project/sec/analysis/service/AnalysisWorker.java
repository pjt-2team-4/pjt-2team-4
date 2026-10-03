package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.common.enums.Language;
import com.rookies6.myspringboot4project.sec.scanner.SecurityScanner;
import com.rookies6.myspringboot4project.sec.scanner.dto.RawFinding;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;

import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisWorker {

    private final AnalysisStateService stateService;
    private final AnalysisProgressStore progressStore;
    private final SecurityScanner scanner;
    private final LlmAnalysisService llmAnalysisService;

    @Async("analysisTaskExecutor")
    public void runAnalysisPipeline(Long analysisId, Set<VulnerabilityType> enabledTypes) {
        boolean explaining = false;
        try {
            stateService.markScanning(analysisId);

            List<ScanTarget> targets = stateService.loadScanTargets(analysisId);
            progressStore.start(analysisId, targets.size());

            int totalFindings = 0;

            EnumSet<Severity> severities = EnumSet.noneOf(Severity.class);

            for (int i = 0; i < targets.size(); i++) {
                ScanTarget target = targets.get(i);
                progressStore.updateFile(analysisId, target.relativePath(), i);

                Language language = Language.fromFileName(target.fileName());
                List<RawFinding> findings = scanner.scan(
                        target.relativePath(), language, target.content(), enabledTypes);

                stateService.saveFindings(target.fileId(), findings);
                totalFindings += findings.size();
                progressStore.addFindings(analysisId, findings.size());

                for (RawFinding finding : findings) {
                    severities.add(finding.severity());
                    progressStore.log(analysisId,
                            "%s 발견 (%s) %s:%d".formatted(
                                    finding.vulnerabilityType().getDisplayName(),
                                    finding.ruleId(),
                                    target.relativePath(),
                                    finding.startLine()));
                }

                progressStore.updateFile(analysisId, target.relativePath(), i + 1);
            }

            if (totalFindings > 0) {
                stateService.startExplaining(analysisId, totalFindings);
                explaining = true;
                progressStore.log(analysisId, "AI 설명 생성 시작 (%d건)".formatted(totalFindings));
                if (!llmAnalysisService.explainAll(analysisId)) {
                    log.warn("LLM 설명 단계 타임아웃: analysisId={}", analysisId);
                    return;
                }
            }
            stateService.complete(analysisId, totalFindings, Severity.highest(severities));
            log.info("분석 완료: analysisId={}, findings={}", analysisId, totalFindings);

        } catch (Exception e) {
            log.error("분석 실패: analysisId={}", analysisId, e);
            stateService.fail(analysisId, explaining
                    ? "AI 설명 처리 중 오류가 발생했습니다."
                    : "코드 분석 중 오류가 발생했습니다.");
        } finally {
            progressStore.clear(analysisId);
        }
    }
}
