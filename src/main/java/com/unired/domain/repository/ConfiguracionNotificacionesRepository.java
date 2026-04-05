package com.unired.domain.repository;

import com.unired.domain.model.ConfiguracionNotificaciones;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracionNotificacionesRepository extends JpaRepository<ConfiguracionNotificaciones, Long> {

    Optional<ConfiguracionNotificaciones> findByUsuarioId(Long usuarioId);

    List<ConfiguracionNotificaciones> findByRecibirRrssTrueAndFcmTokenIsNotNull();
}
