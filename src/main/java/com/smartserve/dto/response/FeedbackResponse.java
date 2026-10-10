package com.smartserve.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FeedbackResponse {
    private Integer rating;
    private String comment;
    private LocalDateTime submittedAt;
}
