package com.unired.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolicitudMentoriaDTO {

    @NotNull
    private Long mentorId;

    @Size(max = 1000)
    private String motivacion;

    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$")
    private String numeroWhatsapp;
}
