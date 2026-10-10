package com.smartserve.dto.request;

import com.smartserve.entity.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull(message = "Status is required")
    private ComplaintStatus status;
    private String notes;
}
