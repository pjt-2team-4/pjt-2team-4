package com.rookies6.myspringboot4project.sec.analysisfile.controller;

import com.rookies6.myspringboot4project.sec.analysisfile.dto.AnalysisFileDTO;
import com.rookies6.myspringboot4project.sec.analysisfile.service.AnalysisFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analyses/{analysisId}/files") // 💡 /v1 추가 완료
@RequiredArgsConstructor
public class AnalysisFileController {

    private final AnalysisFileService analysisFileService;

    /**
     * 특정 분석(프로젝트)에 속한 파일 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<AnalysisFileDTO.Response>> getFilesByProject(
            @PathVariable("analysisId") Long analysisId) {
        List<AnalysisFileDTO.Response> files = analysisFileService.getFilesByProjectId(analysisId);
        return ResponseEntity.ok(files);
    }

    /**
     * 특정 파일 상세 조회 (코드 본문 및 취약점 포함)
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<AnalysisFileDTO.DetailResponse> getFileDetail(
            @PathVariable("analysisId") Long analysisId,
            @PathVariable("fileId") Long fileId) {
        AnalysisFileDTO.DetailResponse fileDetail = analysisFileService.getFileDetail(analysisId, fileId);
        return ResponseEntity.ok(fileDetail);
    }
}