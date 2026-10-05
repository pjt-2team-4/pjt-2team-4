package com.rookies6.myspringboot4project.sec.analysis.controller;

import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisDTO;
import com.rookies6.myspringboot4project.sec.analysis.service.AnalysisService;
import com.rookies6.myspringboot4project.sec.analysis.service.AnalysisResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.rookies6.myspringboot4project.common.dto.ApiResponse;
import com.rookies6.myspringboot4project.sec.analysis.dto.AnalysisResultDTO;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AnalysisResultService analysisResultService;

    /**
     * 분석 요청 생성
     * POST /api/v1/analyses
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AnalysisResultDTO.AcceptedResponse>> createAnalysis(
            @Valid @RequestBody AnalysisDTO.Request request) {

        AnalysisResultDTO.AcceptedResponse response =
                analysisService.analyzeCode(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response, "분석 요청이 접수되었습니다"));
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
    public ResponseEntity<ApiResponse<AnalysisResultDTO.SummaryResponse>> getAnalysisById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                analysisResultService.getSummary(id),
                "분석 결과 요약 조회가 완료되었습니다"));
    }

    /**
     * 분석 상태 조회
     * GET /api/v1/analyses/{id}/status
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AnalysisResultDTO.StatusResponse>>
    getAnalysisStatus(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(analysisService.getAnalysisStatus(id)));
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
