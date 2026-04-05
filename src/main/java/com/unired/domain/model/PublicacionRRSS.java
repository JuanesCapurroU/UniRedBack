package com.unired.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "publicaciones_rrss")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicacionRRSS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "red_social", nullable = false, length = 30)
    private String redSocial;

    @Column(name = "perfil_nombre", length = 120)
    private String perfilNombre;

    @Column(name = "contenido_texto", columnDefinition = "TEXT")
    private String contenidoTexto;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "url_publicacion", length = 500)
    private String urlPublicacion;

    @Column(length = 500)
    private String hashtags;

    @Builder.Default
    @Column(nullable = false)
    private Integer likes = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer comentarios = 0;

    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_cache", nullable = false)
    private LocalDateTime fechaCache;

    @Builder.Default
    @Column(nullable = false)
    private Boolean sincronizada = true;

    @PrePersist
    protected void onCreate() {
        fechaCache = LocalDateTime.now();
    }

    public void obtenerNuevasPublicaciones() {
    }

    public void filtrarPorHashtag(String tag) {
    }

    public List<PublicacionRRSS> obtenerPorHashtag(String tag) {
        return new ArrayList<>();
    }
}
