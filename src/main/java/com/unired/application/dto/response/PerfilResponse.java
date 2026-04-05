package com.unired.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerfilResponse {

    private Long id;
    private String primerNombre;
    private String primerApellido;
    private String correo;
    private String telefono;
    private String fotoUrl;
    private String programaAcademico;
    private Integer semestre;
    private String sede;
    private String rol;
}
