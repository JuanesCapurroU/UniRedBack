package com.unired.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FotoRequest {

    @NotBlank
    @Size(max = 500)
    private String fotoUrl;
}
