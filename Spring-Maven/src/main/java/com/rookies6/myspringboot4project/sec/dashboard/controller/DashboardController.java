package com.rookies6.myspringboot4project.sec.dashboard.controller;

import com.rookies6.myspringboot4project.common.dto.ApiResponse;
import com.rookies6.myspringboot4project.sec.dashboard.dto.DashboardDTO;
import com.rookies6.myspringboot4project.sec.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard/summary")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ApiResponse<DashboardDTO.Response> getDashboard() {
        DashboardDTO.Response response = dashboardService.getDashboardSummary();
        return ApiResponse.success(response);
    }
}
