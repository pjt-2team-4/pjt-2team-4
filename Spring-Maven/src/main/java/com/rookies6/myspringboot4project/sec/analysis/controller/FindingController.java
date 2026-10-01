package com.rookies6.myspringboot4project.sec.analysis.controller;

import com.rookies6.myspringboot4project.common.dto.ApiResponse;
import com.rookies6.myspringboot4project.sec.analysis.dto.FindingDTO;
import com.rookies6.myspringboot4project.sec.analysis.service.FindingService;
import com.rookies6.myspringboot4project.sec.common.enums.FindingStatus;
import com.rookies6.myspringboot4project.sec.common.enums.VulnerabilityType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FindingController {

    private final FindingService findingService;

    /** 12. GET /api/v1/analyses/{analysisId}/findings?type=&ruleId=&status= */
    @GetMapping("/analyses/{analysisId}/findings")
    public ResponseEntity<ApiResponse<List<FindingDTO.ListItem>>> getFindings(
            @PathVariable Long analysisId,
            @RequestParam(required = false) VulnerabilityType type,
            @RequestParam(required = false) String ruleId,
            @RequestParam(required = false) FindingStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                findingService.getFindings(analysisId, type, ruleId, status),
                "취약점 목록 조회가 완료되었습니다"));
    }

    /** 13. GET /api/v1/findings/{findingId} */
    @GetMapping("/findings/{findingId}")
    public ResponseEntity<ApiResponse<FindingDTO.Detail>> getFinding(@PathVariable Long findingId) {
        return ResponseEntity.ok(ApiResponse.success(
                findingService.getFinding(findingId),
                "취약점 상세 조회가 완료되었습니다"));
    }
}