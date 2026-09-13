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
    /** ADMIN o SUPER_ADMIN; null para estudiantes. */
    private String nivelAcceso;
    private String programaAcademico;
    private Integer semestre;
}
