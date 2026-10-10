package com.smartserve.controller;

import com.smartserve.dto.response.ApiResponse;
import com.smartserve.dto.response.DashboardStatsResponse;
import com.smartserve.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    @Autowired private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboardStats()));
    }

    @GetMapping("/resolution-time")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getResolutionTime() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getResolutionTimeByCategory()));
    }

    @GetMapping("/location-analysis")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getLocationAnalysis() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getLocationAnalysis()));
    }
}
