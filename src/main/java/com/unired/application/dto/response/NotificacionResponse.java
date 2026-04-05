package com.unired.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificacionResponse {

    private Long id;
    private Long publicacionId;
    private String tipo;
    private String titulo;
    private String mensaje;
    private Boolean leida;
    private String prioridad;
    private LocalDateTime fecha;
    private String urlAccion;
    private Boolean esActiva;
}
