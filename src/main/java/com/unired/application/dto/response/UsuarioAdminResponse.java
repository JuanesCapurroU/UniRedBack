package com.unired.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioAdminResponse {

    private Long id;
    private String primerNombre;
    private String primerApellido;
    private String correo;
    private String rol;
    private Boolean activo;
    private Boolean verificado;
    private String programaAcademico;
    private Integer semestre;
    private String sede;
    private String fechaCreacion;
}
