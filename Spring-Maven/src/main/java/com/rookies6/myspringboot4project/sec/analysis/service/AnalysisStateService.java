package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisStatus;
import com.rookies6.myspringboot4project.sec.analysis.entity.FindingVulnerability;
import com.rookies6.myspringboot4project.sec.analysis.entity.LlmAnalysis;
import com.rookies6.myspringboot4project.sec.analysis.dto.LlmExplainDTO;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysis.repository.FindingVulnerabilityRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.analysisfile.repository.AnalysisFileRepository;
import com.rookies6.myspringboot4project.sec.scanner.dto.RawFinding;
import com.rookies6.myspringboot4project.sec.common.enums.Severity;
import com.rookies6.myspringboot4project.sec.common.enums.LlmAnalysisStatus;
import com.rookies6.myspringboot4project.sec.common.enums.LlmVerdict;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalysisStateService {

    private final AnalysisRequestRepository requestRepository;
    private final AnalysisFileRepository fileRepository;
    private final FindingVulnerabilityRepository findingRepository;
    private final String promptVersion;

    public AnalysisStateService(AnalysisRequestRepository requestRepository,
                                AnalysisFileRepository fileRepository,
                                FindingVulnerabilityRepository findingRepository,
                                @Value("${llm.prompt-version}") String promptVersion) {
        this.requestRepository = requestRepository;
        this.fileRepository = fileRepository;
        this.findingRepository = findingRepository;
        this.promptVersion = promptVersion;
    }

    @Transactional
    public void markScanning(Long analysisId) {
        findRequest(analysisId).startScanning();
    }

    @Transactional(readOnly = true)
    public List<ScanTarget> loadScanTargets(Long analysisId) {
        return fileRepository.findByAnalysisRequestId(analysisId).stream()
                .map(file -> new ScanTarget(
                        file.getId(),
                        file.getRelativePath(),
                        file.getFileName(),
                        file.getContent()))
                .toList();
    }

    @Transactional
    public void saveFindings(Long fileId, List<RawFinding> findings) {
        if (findings.isEmpty()) {
            return;
        }

        AnalysisFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalStateException("파일을 찾을 수 없습니다: " + fileId));

        for (RawFinding raw : findings) {
            file.addFinding(FindingMapper.toEntity(raw));
        }
    }

    @Transactional
    public void startExplaining(Long analysisId, int totalFindings) {
        findRequest(analysisId).startExplaining(totalFindings);
    }

    @Transactional
    public List<LlmExplainDTO.Request> prepareExplainRequests(Long analysisId) {
        List<FindingVulnerability> findings = findingRepository.findForExplanation(analysisId);
        for (FindingVulnerability finding : findings) {
            if (finding.getLlmAnalysis() == null) {
                LlmAnalysis.pending(finding);
            }
        }
        return findings.stream()
                .map(finding -> LlmRequestFactory.from(finding, promptVersion))
                .toList();
    }

    @Transactional
    public boolean saveLlmSuccess(Long findingId, LlmExplainDTO.Response response) {
        FindingVulnerability finding = findFinding(findingId);
        AnalysisRequest request = findLockedRequest(finding.getAnalysisFile().getAnalysisRequest().getId());
        if (!isPendingExplanation(finding, request)) {
            return false;
        }
        finding.getLlmAnalysis().succeed(response.explanation(), response.riskDescription(),
                response.attackScenario(), response.remediation(), response.fixedCode(),
                LlmVerdict.from(response.verdict()), response.confidence(), response.modelName(),
                response.promptVersion(), response.rawResponse(), response.promptTokens(),
                response.completionTokens());
        return true;
    }

    @Transactional
    public boolean saveLlmFailure(Long findingId, String message, String rawResponse) {
        FindingVulnerability finding = findFinding(findingId);
        AnalysisRequest request = findLockedRequest(finding.getAnalysisFile().getAnalysisRequest().getId());
        if (!isPendingExplanation(finding, request)) {
            return false;
        }
        finding.getLlmAnalysis().fail(message, rawResponse, promptVersion);
        return true;
    }

    @Transactional
    public void failExplanationTimeout(Long analysisId) {
        AnalysisRequest request = findLockedRequest(analysisId);
        if (request.getStatus() != AnalysisStatus.EXPLAINING) {
            return;
        }
        request.fail("LLM 설명 단계가 180초를 초과했습니다.");
        for (FindingVulnerability finding : findingRepository.findForExplanation(analysisId)) {
            LlmAnalysis llm = finding.getLlmAnalysis();
            if (llm != null && llm.getStatus() == LlmAnalysisStatus.PENDING) {
                llm.fail("LLM 설명 단계 타임아웃", null, promptVersion);
            }
        }
    }

    private FindingVulnerability findFinding(Long findingId) {
        return findingRepository.findById(findingId)
                .orElseThrow(() -> new IllegalStateException("취약점을 찾을 수 없습니다: " + findingId));
    }

    private boolean isPendingExplanation(FindingVulnerability finding, AnalysisRequest request) {
        return request.getStatus() == AnalysisStatus.EXPLAINING
                && finding.getLlmAnalysis() != null
                && finding.getLlmAnalysis().getStatus() == LlmAnalysisStatus.PENDING;
    }

    private AnalysisRequest findLockedRequest(Long analysisId) {
        return requestRepository.findLockedById(analysisId)
                .orElseThrow(() -> new IllegalStateException("분석 요청을 찾을 수 없습니다: " + analysisId));
    }

    @Transactional
    public void complete(Long analysisId, int totalFindings, Severity overallSeverity) {
        findRequest(analysisId).complete(totalFindings, overallSeverity);
    }

    @Transactional
    public void fail(Long analysisId, String message) {
        findRequest(analysisId).fail(message);
    }

    private AnalysisRequest findRequest(Long analysisId) {
        return requestRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalStateException(
                        "분석 요청을 찾을 수 없습니다: " + analysisId));
    }
}
