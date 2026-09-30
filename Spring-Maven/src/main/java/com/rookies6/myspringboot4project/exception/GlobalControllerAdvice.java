package com.rookies6.myspringboot4project.common.advice;

import com.rookies6.myspringboot4project.sec.analysis.entity.AnalysisRequest;
import com.rookies6.myspringboot4project.sec.analysis.repository.AnalysisRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final AnalysisRequestRepository analysisRequestRepository;

    @ModelAttribute("analyses")
    public List<AnalysisRequest> populateAnalyses() {
        return analysisRequestRepository.findAll();
    }
}