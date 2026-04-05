package com.unired.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Email
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+\\-]+@uniminuto\\.edu\\.co$",
            message = "Solo se permiten correos @uniminuto.edu.co"
    )
    private String correo;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;
}
