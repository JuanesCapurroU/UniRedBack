package com.unired.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolicitudResponse {

    private Long id;
    private Long estudianteId;
    private String estudianteNombre;
    private Long mentorId;
    private String mentorNombre;
    private Double porcentajeCompatibilidad;
    private String estado;
    private String motivacion;
    private String numeroWhatsapp;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaRespuesta;
    private LocalDateTime fechaFinalizacion;
    private Integer calificacionFinal;
    private Boolean puedeCalificar;
    private Long horasRestantesCalificacion;
    private String comentarioCalificacion;
}
