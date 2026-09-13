package com.unired.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Publicacion institucional creada desde el panel administrativo. */
@Data
public class PublicacionRequest {

    @NotBlank(message = "El contenido es obligatorio")
    @Size(max = 5000)
    private String contenidoTexto;

    @Size(max = 120)
    private String perfilNombre;

    @Size(max = 500)
    private String imagenUrl;

    @Size(max = 500)
    private String urlPublicacion;

    @Size(max = 500)
    private String hashtags;
}
