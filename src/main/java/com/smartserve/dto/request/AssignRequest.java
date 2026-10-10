package com.smartserve.dto.request;

import com.smartserve.entity.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRequest {
    @NotNull(message = "Department is required")
    private Long departmentId;
    private Long staffId;
    private String notes;
    private Priority priority;
}
