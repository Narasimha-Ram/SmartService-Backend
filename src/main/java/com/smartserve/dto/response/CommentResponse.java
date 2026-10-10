package com.smartserve.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private String authorName;
    private String authorRole;
    private String message;
    private Boolean isInternal;
    private LocalDateTime createdAt;
}
