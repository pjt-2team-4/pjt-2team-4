package com.rookies6.myspringboot4project.sec.analysisfile.controller;

import com.rookies6.myspringboot4project.sec.analysisfile.dto.AnalysisFileDetailResponseDto;
import com.rookies6.myspringboot4project.sec.analysisfile.dto.AnalysisFileListResponseDto;
import com.rookies6.myspringboot4project.sec.analysisfile.service.AnalysisFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analyses/{analysisId}/files")
@RequiredArgsConstructor
public class AnalysisFileController {

    private final AnalysisFileService analysisFileService;

    /**
     * 특정 분석에 포함된 파일 목록 조회
     *
     * GET /api/v1/analyses/{analysisId}/files
     *
     * 예:
     * GET /api/v1/analyses/1/files
     */
    @GetMapping
    public ResponseEntity<List<AnalysisFileListResponseDto>> getFiles(
            @PathVariable Long analysisId
    ) {
        return ResponseEntity.ok(
                analysisFileService.getFilesByAnalysisRequestId(analysisId)
        );
    }

    /**
     * 특정 분석의 특정 파일 상세 조회
     *
     * GET /api/v1/analyses/{analysisId}/files/{fileId}
     *
     * 예:
     * GET /api/v1/analyses/1/files/3
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<AnalysisFileDetailResponseDto> getFile(
            @PathVariable Long analysisId,
            @PathVariable Long fileId
    ) {
        return ResponseEntity.ok(
                analysisFileService.getFileById(
                        analysisId,
                        fileId
                )
        );
    }
}
