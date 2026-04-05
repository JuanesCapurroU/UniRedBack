package com.unired.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("Estudiante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Estudiante extends Usuario {

    @Column(name = "correo_institucional", length = 120)
    private String correoInstitucional;

    @Column(name = "programa_academico", length = 120)
    private String programaAcademico;

    @Column
    private Integer semestre;

    @Column(length = 80)
    private String sede;

    @Column(name = "promedio_academico")
    private Double promedioAcademico;

    @Builder.Default
    @Column(name = "estado_cuenta", length = 30)
    private String estadoCuenta = "ACTIVA";

    @Builder.Default
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @OneToOne(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Mentor perfilMentor;

    @Builder.Default
    @OneToMany(mappedBy = "estudiante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Recordatorio> recordatorios = new ArrayList<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ConfiguracionNotificaciones configuracionNotificaciones;
}
