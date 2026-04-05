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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inscripciones", uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id", "actividad_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad;

    @Column(name = "fecha_inscripcion", nullable = false)
    private LocalDateTime fechaInscripcion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Builder.Default
    @Column(name = "recordatorio_activo", nullable = false)
    private Boolean recordatorioActivo = false;

    @Column
    private Boolean asistio;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String estado = "ACTIVA";

    @PrePersist
    protected void onCreate() {
        fechaInscripcion = LocalDateTime.now();
    }

    public void confirmar() {
        this.estado = "ACTIVA";
    }

    public void cancelar() {
        this.estado = "CANCELADA";
        this.fechaCancelacion = LocalDateTime.now();
    }

    public void marcarAsistencia() {
        this.asistio = true;
        this.estado = "ASISTIDA";
    }
}
