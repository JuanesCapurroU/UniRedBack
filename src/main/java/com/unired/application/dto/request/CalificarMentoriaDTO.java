package com.unired.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CalificarMentoriaDTO {

    @Min(1)
    @Max(5)
    private Integer estrellas;

    @Size(max = 1000)
    private String comentario;
}
