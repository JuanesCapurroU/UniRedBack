package com.unired.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InscripcionResponse {

    private Long id;
    private Long actividadId;
    private String actividadNombre;
    private LocalDateTime fechaInscripcion;
    private LocalDateTime fechaCancelacion;
    private Boolean recordatorioActivo;
    private Boolean asistio;
    private String estado;
}
