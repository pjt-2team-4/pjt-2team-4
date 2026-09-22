package com.rookies6.myspringboot4project.sec.analysis.controller;

import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisDTO;
import com.rookies6.myspringboot4project.sec.analysis.service.AnalysisService;
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
    public ResponseEntity<AnalysisDTO.Response> getAnalysisById(@PathVariable Long id) {
        AnalysisDTO.Response response = analysisService.getAnalysisById(id);
        return ResponseEntity.ok(response);
    }
}