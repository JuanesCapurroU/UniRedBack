package com.unired.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioBasicoResponse {

    private Long id;
    private String primerNombre;
    private String primerApellido;
    private String correo;
    private String rol;
    private String programaAcademico;
    private Integer semestre;
}
