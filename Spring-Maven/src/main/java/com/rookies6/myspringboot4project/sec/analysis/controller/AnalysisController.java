package com.rookies6.myspringboot4project.sec.analysis.controller;

import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisDTO;
import com.rookies6.myspringboot4project.sec.analysis.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * 분석 요청 생성
     * POST /api/v1/analyses
     */
    @PostMapping
    public ResponseEntity<AnalysisDTO.Response> createAnalysis(
            @Valid @RequestBody AnalysisDTO.Request request) {

        AnalysisDTO.Response response =
                analysisService.analyzeCode(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 분석 목록 조회
     * GET /api/v1/analyses
     */
    @GetMapping
    public ResponseEntity<List<AnalysisDTO.Response>> getAnalyses() {

        List<AnalysisDTO.Response> response =
                analysisService.getAnalyses();

        return ResponseEntity.ok(response);
    }

    /**
     * 분석 상세 조회
     * GET /api/v1/analyses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisDTO.Response> getAnalysisById(
            @PathVariable Long id) {

        AnalysisDTO.Response response =
                analysisService.getAnalysisById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * 분석 상태 조회
     * GET /api/v1/analyses/{id}/status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<AnalysisDTO.StatusResponse> getAnalysisStatus(
            @PathVariable Long id) {

        AnalysisDTO.StatusResponse response =
                analysisService.getAnalysisStatus(id);

        return ResponseEntity.ok(response);
    }

    /**
     * 분석 삭제
     * DELETE /api/v1/analyses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(
            @PathVariable Long id) {

        analysisService.deleteAnalysis(id);

        return ResponseEntity.noContent().build();
    }
}