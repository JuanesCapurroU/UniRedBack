package com.unired.application.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MentorResponse {

    private Long id;
    private String nombre;
    private List<String> materias;
    private Double calificacionPromedio;
    private Integer sesionesCompletadas;
    private String disponibilidad;
    private Double porcentajeCompatibilidad;
    private String numeroWhatsapp;
}
