package com.unired.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfiguracionNotificacionesResponse {

    private Boolean recibirTramites;
    private Boolean recibirActividades;
    private Boolean recibirMentoria;
    private Boolean recibirRrss;
    private Boolean recibirBot;
    private Boolean modificadoMentoria;
    private String numeroWhatsapp;
}
