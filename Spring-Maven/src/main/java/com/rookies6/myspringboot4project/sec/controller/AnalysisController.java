package com.rookies6.myspringboot4project.sec.controller;

import com.rookies6.myspringboot4project.sec.dto.AnalysisDTO;
import com.rookies6.myspringboot4project.sec.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<AnalysisDTO.Response> analyzeCode(@Valid @RequestBody AnalysisDTO.Request request) {
        AnalysisDTO.Response response = analysisService.analyzeCode(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisDTO.Response> getAnalysis(@PathVariable Long id) {
        AnalysisDTO.Response response = analysisService.getAnalysis(id);
        return ResponseEntity.ok(response);
    }
}