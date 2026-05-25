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
@Table(name = "solicitudes_mentoria")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudMentoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Builder.Default
    @Column(name = "porcentaje_compatibilidad", nullable = false)
    private Double porcentajeCompatibilidad = 0.0;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @Column(columnDefinition = "TEXT")
    private String motivacion;

    @Column(name = "numero_whatsapp", length = 20)
    private String numeroWhatsapp;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    @Column(name = "calificacion_final")
    private Integer calificacionFinal;

    @Column(name = "comentario_calificacion", columnDefinition = "TEXT")
    private String comentarioCalificacion;

    @PrePersist
    protected void onCreate() {
        fechaSolicitud = LocalDateTime.now();
    }

    public void confirmar() {
        this.estado = "CONFIRMADA";
        this.fechaRespuesta = LocalDateTime.now();
        this.fechaInicio = LocalDateTime.now();
    }

    public void finalizar() {
        this.estado = "FINALIZADA";
        this.fechaFinalizacion = LocalDateTime.now();
        this.fechaRespuesta = LocalDateTime.now();
    }

    public void rechazar(String motivo) {
        this.estado = "RECHAZADA";
        this.fechaRespuesta = LocalDateTime.now();
    }

    public boolean puedeCalificar() {
        return "FINALIZADA".equals(estado) && calificacionFinal == null;
    }

    public void calificar(Integer estrellas, String comentario) {
        this.calificacionFinal = estrellas;
        this.comentarioCalificacion = comentario;
        this.fechaRespuesta = LocalDateTime.now();
        this.estado = "CALIFICADA";
    }
}
