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
@Table(name = "recordatorios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDateTime fechaVencimiento;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String prioridad = "PROXIMO";

    @Builder.Default
    @Column(name = "recibir_ventanas", nullable = false)
    private Boolean recibirVentanas = true;

    @Builder.Default
    @Column(name = "recibir_actividades", nullable = false)
    private Boolean recibirActividades = true;

    @Builder.Default
    @Column(name = "recibir_mentoria", nullable = false)
    private Boolean recibirMentoria = true;

    @Builder.Default
    @Column(name = "recibir_rrss", nullable = false)
    private Boolean recibirRrss = true;

    @Column(name = "numero_whatsapp", length = 20)
    private String numeroWhatsapp;

    public void programar() {
    }

    public void cancelar() {
    }

    public boolean disparar() {
        return LocalDateTime.now().isAfter(fechaVencimiento.minusHours(1));
    }
}
