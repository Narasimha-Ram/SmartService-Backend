package com.smartserve.service;

import com.smartserve.dto.response.DashboardStatsResponse;
import com.smartserve.entity.ComplaintStatus;
import com.smartserve.entity.Priority;
import com.smartserve.repository.ComplaintRepository;
import com.smartserve.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private UserRepository userRepository;

    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        stats.setTotalComplaints(complaintRepository.count());
        stats.setPendingComplaints(complaintRepository.countByStatus(ComplaintStatus.PENDING));
        stats.setAssignedComplaints(complaintRepository.countByStatus(ComplaintStatus.ASSIGNED));
        stats.setInProgressComplaints(complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS));
        stats.setResolvedComplaints(complaintRepository.countByStatus(ComplaintStatus.RESOLVED));
        stats.setClosedComplaints(complaintRepository.countByStatus(ComplaintStatus.CLOSED));
        stats.setHighPriorityComplaints(complaintRepository.countByPriority(Priority.HIGH));
        stats.setCriticalComplaints(complaintRepository.countByPriority(Priority.CRITICAL));

        // Monthly trend - last 6 months
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Object[]> monthlyRaw = complaintRepository.countByMonth(sixMonthsAgo);
        List<Map<String, Object>> monthlyTrend = new ArrayList<>();
        String[] monthNames = {"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        for (Object[] row : monthlyRaw) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", monthNames[month] + " " + year);
            m.put("count", count);
            monthlyTrend.add(m);
        }
        stats.setMonthlyTrend(monthlyTrend);

        // Category distribution
        List<Object[]> catRaw = complaintRepository.countByCategoryGrouped();
        List<Map<String, Object>> catDist = new ArrayList<>();
        for (Object[] row : catRaw) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("category", row[0]);
            m.put("count", row[1]);
            catDist.add(m);
        }
        stats.setCategoryDistribution(catDist);

        // Status distribution
        List<Object[]> statusRaw = complaintRepository.countByStatusGrouped();
        List<Map<String, Object>> statusDist = new ArrayList<>();
        for (Object[] row : statusRaw) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("status", row[0]);
            m.put("count", row[1]);
            statusDist.add(m);
        }
        stats.setStatusDistribution(statusDist);

        // Priority distribution
        List<Object[]> priorityRaw = complaintRepository.countByPriorityGrouped();
        List<Map<String, Object>> priorityDist = new ArrayList<>();
        for (Object[] row : priorityRaw) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("priority", row[0]);
            m.put("count", row[1]);
            priorityDist.add(m);
        }
        stats.setPriorityDistribution(priorityDist);

        return stats;
    }

    public List<Map<String, Object>> getResolutionTimeByCategory() {
        List<Object[]> raw = complaintRepository.avgResolutionTimeByCategory();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            if (row[1] != null) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("category", row[0]);
                double avgHours = ((Number) row[1]).doubleValue();
                m.put("avgHours", Math.round(avgHours * 10.0) / 10.0);
                m.put("avgDays", Math.round((avgHours / 24.0) * 10.0) / 10.0);
                result.add(m);
            }
        }
        return result;
    }

    public List<Map<String, Object>> getLocationAnalysis() {
        List<Object[]> raw = complaintRepository.countByLocation();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("location", row[0]);
            m.put("count", row[1]);
            result.add(m);
        }
        return result;
    }
}
