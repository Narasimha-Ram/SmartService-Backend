package com.smartserve.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StaffResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String departmentName;
    private Long departmentId;
    private String designation;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
