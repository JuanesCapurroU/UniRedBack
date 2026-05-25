package com.unired.domain.repository;

import com.unired.domain.model.CodigoVerificacion;
import com.unired.domain.model.CodigoVerificacion.TipoCodigo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {

    Optional<CodigoVerificacion> findTopByCorreoAndTipoAndUsadoFalseOrderByCreatedAtDesc(String correo, TipoCodigo tipo);

    Optional<CodigoVerificacion> findTopByCorreoAndTipoOrderByCreatedAtDesc(String correo, TipoCodigo tipo);
}
