package com.unired.application.dto.response;

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
    private String fechaUltimoMensaje;
    private boolean soySolicitante;
}
