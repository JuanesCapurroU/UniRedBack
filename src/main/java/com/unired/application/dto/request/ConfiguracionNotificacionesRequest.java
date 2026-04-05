package com.unired.application.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ConfiguracionNotificacionesRequest {

    private Boolean recibirTramites;
    private Boolean recibirActividades;
    private Boolean recibirMentoria;
    private Boolean recibirRrss;
    private Boolean recibirBot;
    private Boolean modificadoMentoria;

    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$")
    private String numeroWhatsapp;
}
