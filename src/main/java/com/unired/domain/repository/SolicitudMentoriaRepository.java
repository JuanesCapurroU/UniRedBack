package com.unired.domain.repository;

import com.unired.domain.model.SolicitudMentoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudMentoriaRepository extends JpaRepository<SolicitudMentoria, Long> {

    List<SolicitudMentoria> findByEstudianteIdOrderByFechaSolicitudDesc(Long estudianteId);

    Optional<SolicitudMentoria> findByIdAndMentorEstudianteId(Long id, Long mentorEstudianteId);

    List<SolicitudMentoria> findByMentorEstudianteIdOrderByFechaSolicitudDesc(Long mentorEstudianteId);
}
