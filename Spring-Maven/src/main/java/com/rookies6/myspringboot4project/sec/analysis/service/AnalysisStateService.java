package com.rookies6.myspringboot4project.sec.analysis.service;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import com.rookies6.myspringboot4project.sec.analysisfile.entity.AnalysisFile;
import com.rookies6.myspringboot4project.sec.analysisfile.repository.AnalysisFileRepository;
import com.rookies6.myspringboot4project.sec.scanner.dto.RawFinding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisStateService {

    private final AnalysisRequestRepository requestRepository;
    private final AnalysisFileRepository fileRepository;

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
    public void complete(Long analysisId, int totalFindings) {
        findRequest(analysisId).complete(totalFindings);
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