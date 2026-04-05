package com.unired.application.dto.request;

import com.unired.domain.enums.CategoriaActividad;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ActividadRequest {

    @NotBlank
    @Size(max = 200)
    private String nombre;

    @Size(max = 200)
    private String lugar;

    private String descripcion;

    @NotNull
    @Future
    private LocalDateTime fechaHora;

    @Min(15)
    @Max(480)
    private Integer duracionMinutos;

    @NotNull
    private CategoriaActividad categoria;

    @Min(1)
    @Max(500)
    private Integer cupoTotal;
}
