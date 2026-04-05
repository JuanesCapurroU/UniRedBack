package com.unired.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearEstudianteRequest {

    @NotBlank
    @Size(max = 20)
    private String tipoDocumento;

    @NotBlank
    @Size(max = 20)
    private String numeroDocumento;

    @NotBlank
    @Size(max = 80)
    private String primerNombre;

    @NotBlank
    @Size(max = 80)
    private String primerApellido;

    @Email
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+\\-]+@uniminuto\\.edu\\.co$",
            message = "Solo se permiten correos @uniminuto.edu.co"
    )
    private String correo;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @Size(max = 120)
    private String programaAcademico;

    @Min(1)
    @Max(20)
    private Integer semestre;

    @Size(max = 80)
    private String sede;
}
