package com.rookies6.myspringboot4project.sec.analysisfile.controller;

import com.rookies6.myspringboot4project.sec.analysisfile.dto.AnalysisFileDetailResponseDto;
import com.rookies6.myspringboot4project.sec.analysisfile.dto.AnalysisFileListResponseDto;
import com.rookies6.myspringboot4project.sec.analysisfile.service.AnalysisFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis-files")
@RequiredArgsConstructor
public class AnalysisFileController {

    private final AnalysisFileService analysisFileService;

    /**
     * 1. 특정 분석 요청 ID에 속한 전체 파일 목록 조회 (content 제외)
     * GET /api/v1/analysis-files/request/{requestId}
     */
    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<AnalysisFileListResponseDto>> getFilesByRequestId(@PathVariable Long requestId) {
        List<AnalysisFileListResponseDto> files = analysisFileService.getFilesByAnalysisRequestId(requestId);
        return ResponseEntity.ok(files);
    }

    /**
     * 2. 단일 분석 파일 상세 조회 (소스 코드 원문 및 취약점 정보 포함)
     * GET /api/v1/analysis-files/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisFileDetailResponseDto> getFileById(@PathVariable Long id) {
        AnalysisFileDetailResponseDto fileDetail = analysisFileService.getFileById(id);
        return ResponseEntity.ok(fileDetail);
    }
}