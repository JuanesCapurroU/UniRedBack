package com.unired.domain.repository;

import com.unired.domain.model.SolicitudMentoria;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolicitudMentoriaRepository extends JpaRepository<SolicitudMentoria, Long> {

    List<SolicitudMentoria> findByEstudianteIdOrderByFechaSolicitudDesc(Long estudianteId);

    Optional<SolicitudMentoria> findByIdAndEstudianteId(Long id, Long estudianteId);

    Optional<SolicitudMentoria> findByIdAndMentorEstudianteId(Long id, Long mentorEstudianteId);

    List<SolicitudMentoria> findByMentorEstudianteIdOrderByFechaSolicitudDesc(Long mentorEstudianteId);

    @Query("SELECT s FROM SolicitudMentoria s WHERE s.estudiante.id = :userId OR s.mentor.estudiante.id = :userId ORDER BY s.fechaSolicitud DESC")
    List<SolicitudMentoria> findAllByParticipanteId(@Param("userId") Long userId);

    Optional<SolicitudMentoria> findFirstByEstudianteIdAndMentorIdAndEstadoInOrderByFechaSolicitudDesc(
            Long estudianteId,
            Long mentorId,
            Collection<String> estados
    );

    List<SolicitudMentoria> findByEstudianteIdAndMentorIdAndEstadoAndIdNotOrderByFechaSolicitudDesc(
            Long estudianteId,
            Long mentorId,
            String estado,
            Long id
    );
}
