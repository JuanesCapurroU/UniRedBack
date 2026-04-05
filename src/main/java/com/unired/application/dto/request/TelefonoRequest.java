package com.unired.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TelefonoRequest {

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    private String telefono;
}
