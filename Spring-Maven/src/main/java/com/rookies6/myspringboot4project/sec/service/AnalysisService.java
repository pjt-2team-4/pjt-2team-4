package com.rookies6.myspringboot4project.sec.service;

import com.rookies6.myspringboot4project.sec.client.LlmAnalyzerClient;
import com.rookies6.myspringboot4project.sec.dto.AnalysisDTO;
import com.rookies6.myspringboot4project.sec.entity.Analysis;
import com.rookies6.myspringboot4project.sec.entity.Vulnerability;
import com.rookies6.myspringboot4project.sec.repository.AnalysisRepository;
import com.rookies6.myspringboot4project.exception.BusinessException;
import com.rookies6.myspringboot4project.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final LlmAnalyzerClient llmClient;

    @Transactional
    public AnalysisDTO.Response analyzeCode(AnalysisDTO.Request request) {
        Analysis analysis = Analysis.builder()
                .language(request.getLanguage())
                .originalCode(request.getCode())
                .build();
        analysisRepository.save(analysis);

        List<Vulnerability> vulnerabilities = llmClient.requestAnalysisToAi(request.getLanguage(), request.getCode());
        int highCount = (int) vulnerabilities.stream().filter(v -> "HIGH".equals(v.getSeverity())).count();
        analysis.updateResults(vulnerabilities.size(), highCount, vulnerabilities);

        return AnalysisDTO.Response.fromEntity(analysis);
    }

    @Transactional(readOnly = true)
    public AnalysisDTO.Response getAnalysis(Long id) {
        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        return AnalysisDTO.Response.fromEntity(analysis);
    }
}