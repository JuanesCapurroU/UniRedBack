package com.unired.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {

    private Long solicitudId;
    private String estado;
    private Long otroUsuarioId;
    private String otroUsuarioNombre;
    private String ultimoMensaje;
    private LocalDateTime fechaUltimoMensaje;
    private boolean soySolicitante;
}
