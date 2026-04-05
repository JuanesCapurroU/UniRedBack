package com.unired.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FcmTokenRequest {

    @NotBlank
    @Size(max = 500)
    private String fcmToken;
}
