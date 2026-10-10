package com.smartserve.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;
    private String phone;
}
