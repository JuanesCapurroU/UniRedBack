package com.unired.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActividadResponse {

    private Long id;
    private String nombre;
    private String lugar;
    private String descripcion;
    private LocalDateTime fechaHora;
    private Integer duracionMinutos;
    private String categoria;
    private Integer cupoTotal;
    private Integer cupoDisponible;
    private Boolean activa;
    private Boolean inscrito;
    private Boolean recordatorioWa;
}
