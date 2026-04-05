package com.unired.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstudianteResponse {

    private Long id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String primerNombre;
    private String primerApellido;
    private String correo;
    private String telefono;
    private String programaAcademico;
    private Integer semestre;
    private String sede;
    private Boolean activo;
}
