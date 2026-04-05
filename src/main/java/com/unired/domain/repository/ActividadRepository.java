package com.unired.domain.repository;

import com.unired.domain.enums.CategoriaActividad;
import com.unired.domain.model.Actividad;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    @Override
    List<Actividad> findAll();

    @Query("""
            select a from Actividad a
            where (:date is null or FUNCTION('date', a.fechaHora) = FUNCTION('date', :date))
            and (:actividadId is null or a.id = :actividadId)
            """)
    List<Actividad> findByFechaData(@Param("date") LocalDateTime date, @Param("actividadId") Long actividadId);

    Optional<Actividad> findByIdAndActivaTrue(Long id);

    @Modifying
    @Query("update Actividad a set a.cupoDisponible = a.cupoDisponible + :delta where a.id = :id")
    void actualizarCupos(@Param("id") Long id, @Param("delta") Integer delta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Actividad a where a.id = :id and a.activa = true")
    Optional<Actividad> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select a from Actividad a
            where a.activa = true
              and (:categoria is null or a.categoria = :categoria)
              and (:fecha is null or FUNCTION('date', a.fechaHora) = :fecha)
              and (:recordatorioWa is null or a.recordatorioWa = :recordatorioWa)
            """)
    Page<Actividad> findFiltered(
            @Param("categoria") CategoriaActividad categoria,
            @Param("fecha") LocalDate fecha,
            @Param("recordatorioWa") Boolean recordatorioWa,
            Pageable pageable
    );
}
