package com.smartserve.service;

import com.smartserve.entity.Priority;
import com.smartserve.repository.ComplaintRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IntelligentServiceTest {

    @Mock private ComplaintRepository complaintRepository;
    @InjectMocks private IntelligentService intelligentService;

    @Test
    void recommendPriority_CriticalKeywords() {
        String priority = intelligentService.recommendPriority("Fire in electrical room", "There is a fire emergency", "Electrical");
        assertEquals("CRITICAL", priority);
    }

    @Test
    void recommendPriority_HighKeywords() {
        String priority = intelligentService.recommendPriority("WiFi router broken", "The network is down completely", "Internet / Network");
        assertEquals("HIGH", priority);
    }

    @Test
    void recommendPriority_DefaultMedium() {
        String priority = intelligentService.recommendPriority("Need new chair", "Regular request for seminar room", "Furniture");
        assertEquals("MEDIUM", priority);
    }

    @Test
    void estimateResolutionTime_WithHistoricalData() {
        when(complaintRepository.avgResolutionHoursByCategory(1L)).thenReturn(48.0);
        String est = intelligentService.estimateResolutionTime(1L, Priority.HIGH);
        assertTrue(est.contains("2.0 days"));
    }

    @Test
    void textSimilarity_IdenticalText() {
        double sim = intelligentService.textSimilarity("WiFi network down in block A", "WiFi network down in block A");
        assertEquals(1.0, sim, 0.001);
    }

    @Test
    void textSimilarity_DifferentText() {
        double sim = intelligentService.textSimilarity("Plumbing leak in bathroom", "Computer screen broken");
        assertTrue(sim < 0.1);
    }
}
