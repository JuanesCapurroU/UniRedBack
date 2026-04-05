package com.unired.application.dto.request;

import com.unired.domain.enums.CategoriaActividad;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.Data;

@Data
public class FiltroActividadDTO {

    private CategoriaActividad categoria;
    private LocalDate fecha;
    private Boolean recordatorioWa;

    @Min(0)
    private Integer page = 0;

    @Min(1)
    @Max(100)
    private Integer size = 20;
}
