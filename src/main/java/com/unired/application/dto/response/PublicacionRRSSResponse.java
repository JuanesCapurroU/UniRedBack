package com.unired.application.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicacionRRSSResponse {

    private Long id;
    private String redSocial;
    private String perfilNombre;
    private String contenidoTexto;
    private String imagenUrl;
    private String urlPublicacion;
    private String hashtags;
    private Integer likes;
    private Integer comentarios;
    private LocalDateTime fechaPublicacion;
    private LocalDateTime fechaCache;
}
