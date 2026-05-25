package com.unired.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+\\-]+@uniminuto\\.edu\\.co$",
            message = "Solo se permiten correos @uniminuto.edu.co"
    )
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String password;

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    private String numeroDocumento;

    @NotBlank(message = "El primer nombre es obligatorio")
    private String primerNombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    private String primerApellido;

    @NotBlank(message = "El programa academico es obligatorio")
    private String programaAcademico;

    @NotNull(message = "El semestre es obligatorio")
    @Min(value = 1, message = "El semestre debe ser valido")
    @Max(value = 20, message = "El semestre debe ser valido")
    private Integer semestre;

    @NotBlank(message = "La sede es obligatoria")
    @Pattern(regexp = "^Zipaquir[aá]$", message = "La sede debe ser Zipaquira")
    private String sede;

    private String segundoNombre;
    private String segundoApellido;
    private String telefono;
}
