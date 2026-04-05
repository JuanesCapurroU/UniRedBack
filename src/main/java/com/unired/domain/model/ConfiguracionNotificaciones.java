package com.unired.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracion_notificaciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionNotificaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Builder.Default
    @Column(name = "recibir_tramites", nullable = false)
    private Boolean recibirTramites = true;

    @Builder.Default
    @Column(name = "recibir_actividades", nullable = false)
    private Boolean recibirActividades = true;

    @Builder.Default
    @Column(name = "recibir_mentoria", nullable = false)
    private Boolean recibirMentoria = true;

    @Builder.Default
    @Column(name = "recibir_rrss", nullable = false)
    private Boolean recibirRrss = true;

    @Builder.Default
    @Column(name = "recibir_bot", nullable = false)
    private Boolean recibirBot = true;

    @Builder.Default
    @Column(name = "modificado_mentoria", nullable = false)
    private Boolean modificadoMentoria = false;

    @Column(name = "numero_whatsapp", length = 20)
    private String numeroWhatsapp;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    public void actualizarPreferencias(Map<String, Boolean> prefs) {
        prefs.forEach(this::establecerTipoNotificacion);
    }

    public void establecerTipoNotificacion(String tipo, Boolean activo) {
        switch (tipo) {
            case "tramites" -> this.recibirTramites = activo;
            case "actividades" -> this.recibirActividades = activo;
            case "mentoria" -> this.recibirMentoria = activo;
            case "rrss" -> this.recibirRrss = activo;
            case "bot" -> this.recibirBot = activo;
            default -> {
            }
        }
    }

    public boolean esEficiente(String tipo) {
        return switch (tipo) {
            case "TRAMITE" -> Boolean.TRUE.equals(recibirTramites);
            case "ACTIVIDAD" -> Boolean.TRUE.equals(recibirActividades);
            case "MENTORIA" -> Boolean.TRUE.equals(recibirMentoria);
            case "RRSS" -> Boolean.TRUE.equals(recibirRrss);
            case "BOT" -> Boolean.TRUE.equals(recibirBot);
            default -> true;
        };
    }
}
