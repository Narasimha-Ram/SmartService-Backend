package com.smartserve.dto.response;

import com.smartserve.entity.ComplaintStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StatusHistoryResponse {
    private ComplaintStatus oldStatus;
    private ComplaintStatus newStatus;
    private String changedByName;
    private LocalDateTime changedAt;
    private String notes;
}
