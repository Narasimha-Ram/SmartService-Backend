package com.smartserve.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalComplaints;
    private long pendingComplaints;
    private long assignedComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long closedComplaints;
    private long highPriorityComplaints;
    private long criticalComplaints;
    private Double avgResolutionHours;
    private List<Map<String, Object>> monthlyTrend;
    private List<Map<String, Object>> categoryDistribution;
    private List<Map<String, Object>> statusDistribution;
    private List<Map<String, Object>> priorityDistribution;
}
