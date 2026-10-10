package com.smartserve.service;

import com.smartserve.entity.Priority;
import com.smartserve.entity.Complaint;
import com.smartserve.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Heuristic intelligent features for SmartServe.
 * Note: These are rule/data-based heuristics, NOT machine learning models.
 * Results should be treated as suggestions only.
 */
@Service
public class IntelligentService {

    @Autowired private ComplaintRepository complaintRepository;

    private static final List<String> CRITICAL_KEYWORDS =
            Arrays.asList("fire", "flood", "unsafe", "dangerous", "emergency", "critical", "urgent", "hazard", "electric shock", "gas leak");
    private static final List<String> HIGH_KEYWORDS =
            Arrays.asList("not working", "down", "broken", "outage", "failure", "leak", "burst", "offline");
    private static final List<String> LOW_KEYWORDS =
            Arrays.asList("minor", "slight", "small", "cosmetic", "paint", "aesthetic");

    /**
     * Recommend a priority based on keywords in title + description, and category.
     */
    public String recommendPriority(String title, String description, String categoryName) {
        String combined = (title + " " + description).toLowerCase();

        for (String keyword : CRITICAL_KEYWORDS) {
            if (combined.contains(keyword)) return "CRITICAL";
        }

        if (categoryName != null) {
            String cat = categoryName.toLowerCase();
            if (cat.contains("electrical") || cat.contains("security") || cat.contains("plumbing")) {
                for (String keyword : HIGH_KEYWORDS) {
                    if (combined.contains(keyword)) return "HIGH";
                }
            }
        }

        for (String keyword : HIGH_KEYWORDS) {
            if (combined.contains(keyword)) return "HIGH";
        }

        for (String keyword : LOW_KEYWORDS) {
            if (combined.contains(keyword)) return "LOW";
        }

        return "MEDIUM";
    }

    /**
     * Estimate resolution time based on historical data for same category.
     * Returns a human-readable estimate string.
     */
    public String estimateResolutionTime(Long categoryId, Priority priority) {
        Double avgHours = complaintRepository.avgResolutionHoursByCategory(categoryId);
        if (avgHours == null) {
            // Fallback to priority-based estimate
            return getFallbackEstimate(priority);
        }
        double days = avgHours / 24.0;
        if (days < 1) return String.format("Approximately %.0f hours (based on historical data)", avgHours);
        return String.format("Approximately %.1f days (based on historical data)", days);
    }

    private String getFallbackEstimate(Priority priority) {
        return switch (priority) {
            case CRITICAL -> "Expected within 24 hours (insufficient historical data)";
            case HIGH     -> "Expected within 2-3 days (insufficient historical data)";
            case MEDIUM   -> "Expected within 3-5 days (insufficient historical data)";
            case LOW      -> "Expected within 5-7 days (insufficient historical data)";
        };
    }

    /**
     * Simple text overlap similarity (Jaccard-like) for duplicate detection.
     */
    public double textSimilarity(String a, String b) {
        if (a == null || b == null) return 0.0;
        String[] wordsA = a.toLowerCase().split("\\s+");
        String[] wordsB = b.toLowerCase().split("\\s+");

        java.util.Set<String> setA = new java.util.HashSet<>(Arrays.asList(wordsA));
        java.util.Set<String> setB = new java.util.HashSet<>(Arrays.asList(wordsB));

        java.util.Set<String> intersection = new java.util.HashSet<>(setA);
        intersection.retainAll(setB);

        java.util.Set<String> union = new java.util.HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }
}
