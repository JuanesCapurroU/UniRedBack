package com.unired.domain.repository;

import com.unired.domain.model.MensajeMentoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeMentoriaRepository extends JpaRepository<MensajeMentoria, Long> {

    List<MensajeMentoria> findBySolicitudIdOrderByFechaEnvioAsc(Long solicitudId);
}
