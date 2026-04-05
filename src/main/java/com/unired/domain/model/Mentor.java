package com.unired.domain.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "mentores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mentor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false, unique = true)
    private Estudiante estudiante;

    @Builder.Default
    @Column(name = "calificacion_promedio", nullable = false)
    private Double calificacionPromedio = 0.0;

    @Builder.Default
    @Column(name = "sesiones_completadas", nullable = false)
    private Integer sesionesCompletadas = 0;

    @Builder.Default
    @Column(name = "sesiones_activas", nullable = false)
    private Integer sesionesActivas = 0;

    @Column(name = "disponibilidad", length = 500)
    private String disponibilidad;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = false;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "mentor_materias", joinColumns = @JoinColumn(name = "mentor_id"))
    @Column(name = "materia", length = 120)
    private List<String> materias = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "mentor", fetch = FetchType.LAZY)
    private List<SolicitudMentoria> solicitudes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaSolicitud = LocalDateTime.now();
    }

    public boolean tieneCapacidad() {
        return sesionesActivas < 5;
    }

    public void calcularCompatibilidad(Estudiante e, Long idEstudiante) {
    }
}
