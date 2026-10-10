package com.smartserve.dto.response;

import com.smartserve.entity.ComplaintStatus;
import com.smartserve.entity.Priority;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComplaintSummaryResponse {
    private Long id;
    private String complaintNumber;
    private String title;
    private String categoryName;
    private String location;
    private Priority priority;
    private ComplaintStatus status;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
