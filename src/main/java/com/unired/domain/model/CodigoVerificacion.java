package com.unired.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "codigo_verificacion", indexes = {
        @Index(name = "idx_codigo_verificacion_correo", columnList = "correo"),
        @Index(name = "idx_codigo_verificacion_expires", columnList = "expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String correo;

    @Column(nullable = false, length = 6)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCodigo tipo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean usado = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer intentos = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isValido() {
        return !usado && expiresAt.isAfter(LocalDateTime.now());
    }

    public void incrementarIntento() {
        this.intentos = this.intentos + 1;
    }

    public enum TipoCodigo {
        REGISTRO,
        RECUPERAR_PASSWORD
    }
}