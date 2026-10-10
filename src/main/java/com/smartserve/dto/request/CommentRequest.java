package com.smartserve.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank(message = "Message is required")
    private String message;
    private Boolean isInternal = false;
}
