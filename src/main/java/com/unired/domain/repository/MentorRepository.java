package com.unired.domain.repository;

import com.unired.domain.enums.Delta;
import com.unired.domain.model.Mentor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MentorRepository extends JpaRepository<Mentor, Long> {

    List<Mentor> findByActivoTrue();

    Optional<Mentor> findByEstudianteId(Long id);

    @Override
    Optional<Mentor> findById(Long id);

    @Modifying
    @Query(value = """
            UPDATE mentores
            SET sesiones_activas = CASE
                WHEN :#{#delta.name()} = 'INCREMENT' THEN sesiones_activas + 1
                WHEN :#{#delta.name()} = 'DECREMENT' THEN GREATEST(sesiones_activas - 1, 0)
                ELSE sesiones_activas
            END
            WHERE id = :id
            """, nativeQuery = true)
    void updateSesionesActivasWhenId(@Param("id") Long id, @Param("delta") Delta delta);
}
