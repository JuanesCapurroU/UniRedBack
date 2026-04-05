package com.unired.domain.repository;

import com.unired.domain.model.Inscripcion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Optional<Inscripcion> findByEstudianteIdAndActividadIdAndEstado(Long estudianteId, Long actividadId, String estado);

    List<Inscripcion> findByEstudianteIdOrderByFechaInscripcionDesc(Long estudianteId);

    List<Inscripcion> findByActividadIdAndEstado(Long actividadId, String estado);

    boolean existsByEstudianteIdAndActividadId(Long estudianteId, Long actividadId);

    Optional<Inscripcion> findByIdAndEstudianteId(Long id, Long estudianteId);
}
