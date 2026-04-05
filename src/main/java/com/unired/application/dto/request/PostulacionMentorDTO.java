package com.unired.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
}
