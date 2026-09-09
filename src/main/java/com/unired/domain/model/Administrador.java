package com.unired.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("ADMINISTRADOR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
// Un administrador de UniRed siempre nace de promover a un estudiante (ver
// UsuarioRepository.promoteToAdmin), asi que hereda de Estudiante. De lo contrario, al
// cambiar el dtype se rompen las mentorias, inscripciones y demas datos que lo referencian
// como Estudiante. El rol se resuelve por "instanceof Administrador", que se evalua primero.
public class Administrador extends Estudiante {

    @Builder.Default
    @Column(name = "nivel_acceso", length = 30)
    private String nivelAcceso = "ADMIN";
}
