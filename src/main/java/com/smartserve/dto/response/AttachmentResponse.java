package com.smartserve.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private LocalDateTime uploadedAt;
}
