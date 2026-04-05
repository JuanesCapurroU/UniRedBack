package com.unired.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EnviarNotificacionRequest {

    @NotNull
    private Long usuarioId;

    @NotBlank
    @Size(max = 200)
    private String titulo;

    @NotBlank
    @Size(max = 5000)
    private String mensaje;

    @Size(max = 50)
    private String tipo = "BOT";

    @Size(max = 20)
    private String prioridad = "MEDIA";

    @Size(max = 500)
    private String urlAccion;
}
