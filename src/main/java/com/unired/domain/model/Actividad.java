package com.unired.domain.model;

import com.unired.domain.enums.CategoriaActividad;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "actividades")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 200)
    private String lugar;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Builder.Default
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos = 60;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaActividad categoria;

    @Column(name = "cupo_total", nullable = false)
    private Integer cupoTotal;

    @Column(name = "cupo_disponible", nullable = false)
    private Integer cupoDisponible;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activa = true;

    @Builder.Default
    @Column(name = "recordatorio_wa", nullable = false)
    private Boolean recordatorioWa = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrador_id")
    private Administrador administrador;

    @Builder.Default
    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    public boolean tieneCupo() {
        return cupoDisponible > 0;
    }

    public void decrementarCupo() {
        if (!tieneCupo()) {
            throw new IllegalStateException("Sin cupos disponibles");
        }
        cupoDisponible--;
    }

    public void incrementarCupo() {
        cupoDisponible = Math.min(cupoDisponible + 1, cupoTotal);
    }

    public boolean esCancelable(LocalDateTime ahora) {
        return fechaHora.minusHours(2).isAfter(ahora);
    }
}
