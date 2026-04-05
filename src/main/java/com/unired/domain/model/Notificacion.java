package com.unired.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificaciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "publicacion_id")
    private Long publicacionId;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Builder.Default
    @Column(nullable = false)
    private Boolean leida = false;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String prioridad = "MEDIA";

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "url_accion", length = 500)
    private String urlAccion;

    @Builder.Default
    @Column(name = "es_activa", nullable = false)
    private Boolean esActiva = true;

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }

    public void marcarLeida() {
        this.leida = true;
    }

    public void marcarActiva(Boolean estado) {
        this.esActiva = estado;
    }
}
