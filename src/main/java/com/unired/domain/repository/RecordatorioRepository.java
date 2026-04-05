package com.unired.domain.repository;

import com.unired.domain.model.Recordatorio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {

    List<Recordatorio> findTop5ByEstudianteIdAndPrioridadOrderByFechaVencimientoAsc(Long estudianteId, String prioridad);
}
