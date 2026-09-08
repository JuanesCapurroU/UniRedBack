package com.unired.application.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class PostulacionMentorDTO {
    @NotEmpty
    private List<@NotBlank String> materias;

    @NotBlank
    private String disponibilidad;

    @Size(max = 1000)
    private String bio;

    // Requisitos para ser mentor (el administrador los verifica)
    @NotNull(message = "El promedio académico es obligatorio")
    @DecimalMin(value = "3.8", message = "Se requiere un promedio de 3.8 o superior")
    @DecimalMax(value = "5.0", message = "El promedio máximo es 5.0")
    private Double promedioAcademico;

    @AssertTrue(message = "No puedes tener materias perdidas para ser mentor")
    private Boolean sinMateriasPerdidas;

    @AssertTrue(message = "No puedes tener procesos disciplinarios para ser mentor")
    private Boolean sinProcesosDisciplinarios;

    @NotNull(message = "Indica tus horas disponibles por semana")
    @Min(value = 3, message = "Se requiere disponibilidad de 3 a 5 horas semanales")
    @Max(value = 5, message = "Se requiere disponibilidad de 3 a 5 horas semanales")
    private Integer horasSemanales;
}
