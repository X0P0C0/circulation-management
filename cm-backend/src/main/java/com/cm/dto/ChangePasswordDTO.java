package com.cm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDTO {
    @NotBlank(message = "Old password required")
    private String oldPassword;

    @NotBlank(message = "New password required")
    @Size(min = 6, max = 50, message = "Password must be 6-50 chars")
    private String newPassword;
}