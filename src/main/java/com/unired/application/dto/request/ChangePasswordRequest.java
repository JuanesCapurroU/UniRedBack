package com.unired.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank
    private String passwordActual;

    @NotBlank
    @Size(min = 8, max = 100)
    private String passwordNueva;

    @NotBlank
    private String confirmacionPassword;
}
