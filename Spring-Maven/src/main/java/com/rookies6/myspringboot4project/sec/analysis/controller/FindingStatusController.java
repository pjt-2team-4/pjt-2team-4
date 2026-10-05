package com.rookies6.myspringboot4project.sec.analysis.controller;

import com.rookies6.myspringboot4project.sec.analysis.dto.FindingStatusDTO;
import com.rookies6.myspringboot4project.sec.analysis.service.FindingStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FindingStatusController {

    private final FindingStatusService findingStatusService;

    @PatchMapping("findings/{findingId}/status")
    public ResponseEntity<FindingStatusDTO.Response> changeFindingStatus(
            @PathVariable Long findingId, @Valid @RequestBody FindingStatusDTO.Request request) {

        FindingStatusDTO.Response response = findingStatusService.changeStatus(findingId, request);
        return ResponseEntity.ok(response);
    }
}
