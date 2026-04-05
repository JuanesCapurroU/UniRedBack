package com.unired.domain.repository;

import com.unired.domain.model.Sesion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionRepository extends JpaRepository<Sesion, Long> {

    Optional<Sesion> findByTokenJwtAndActivoTrue(String tokenJwt);

    Optional<Sesion> findByRefreshTokenAndActivoTrue(String refreshToken);

    Optional<Sesion> findByTokenJwt(String tokenJwt);
}
