package com.unired.application.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MentorDetalleResponse {

    private Long id;
    private String nombre;
    private String correo;
    private List<String> materias;
    private Double calificacionPromedio;
    private Integer sesionesCompletadas;
    private Integer sesionesActivas;
    private String disponibilidad;
    private String bio;
    private Boolean activo;
    private Double porcentajeCompatibilidad;
}
