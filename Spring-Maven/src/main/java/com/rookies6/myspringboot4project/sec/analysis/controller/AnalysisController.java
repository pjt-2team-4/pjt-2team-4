package com.rookies6.myspringboot4project.sec.analysis.controller;

import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisRequestDto;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisResponseDto;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisStatusResponseDto;
import com.rookies6.myspringboot4project.sec.analysis.service.AnalysisService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    /**
     * 1. 전체 분석 히스토리 목록 조회
     * GET /api/v1/analyses
     */
    @GetMapping
    public ResponseEntity<List<AnalysisResponseDto>> getAllAnalyses() {
        List<AnalysisResponseDto> analyses = analysisService.getAllAnalyses();
        return ResponseEntity.ok(analyses);
    }

    /**
     * 2. ID로 분석 결과 상세 조회
     * GET /api/v1/analyses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResponseDto> getAnalysisById(@PathVariable Long id) {
        AnalysisResponseDto analysis = analysisService.getAnalysisById(id);
        return ResponseEntity.ok(analysis);
    }

    /**
     * 3. 비동기 스캔 진행 상태 조회 (Polling)
     * GET /api/v1/analyses/{id}/status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<AnalysisStatusResponseDto> getStatus(@PathVariable Long id) {
        AnalysisStatusResponseDto status = analysisService.getAnalysisStatus(id);
        return ResponseEntity.ok(status);
    }

    /**
     * 4. 새 분석 요청 생성 (202 ACCEPTED 비동기 처리)
     * POST /api/v1/analyses
     */
    @PostMapping
    public ResponseEntity<AnalysisResponseDto> createAnalysis(@Valid @RequestBody AnalysisRequestDto request) {
        AnalysisResponseDto response = analysisService.createAnalysis(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * 5. 분석 히스토리 삭제
     * DELETE /api/v1/analyses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysis(@PathVariable Long id) {
        analysisService.deleteAnalysis(id);
        return ResponseEntity.ok().build();
    }
}