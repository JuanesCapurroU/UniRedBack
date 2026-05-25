package com.unired.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MensajeMentoriaResponse {

    private Long id;
    private Long emisorId;
    private String emisorNombre;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private Boolean esMio;
}
