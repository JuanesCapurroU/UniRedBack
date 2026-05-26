package com.unired.domain.repository;

import com.unired.domain.model.MensajeMentoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeMentoriaRepository extends JpaRepository<MensajeMentoria, Long> {

    List<MensajeMentoria> findBySolicitudIdOrderByFechaEnvioAsc(Long solicitudId);

    Optional<MensajeMentoria> findTopBySolicitudIdOrderByFechaEnvioDesc(Long solicitudId);
}
