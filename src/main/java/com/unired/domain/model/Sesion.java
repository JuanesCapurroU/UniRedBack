package com.unired.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sesiones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_jwt", nullable = false, unique = true, columnDefinition = "TEXT")
    private String tokenJwt;

    @Column(name = "refresh_token", nullable = false, unique = true, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "dispositivo_info", length = 255)
    private String dispositivoInfo;

    public boolean isValido() {
        return Boolean.TRUE.equals(activo) && fechaExpiracion.isAfter(LocalDateTime.now());
    }

    public void invalidar() {
        this.activo = false;
    }

    public boolean estaExpirado() {
        return fechaExpiracion.isBefore(LocalDateTime.now());
    }
}
