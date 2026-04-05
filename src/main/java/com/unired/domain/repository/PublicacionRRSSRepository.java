package com.unired.domain.repository;

import com.unired.domain.model.PublicacionRRSS;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublicacionRRSSRepository extends JpaRepository<PublicacionRRSS, Long> {

    List<PublicacionRRSS> findByRedSocialOrderByFechaPublicacionDesc(String redSocial);

    List<PublicacionRRSS> findByHashtagsContaining(String hashtag);

    List<PublicacionRRSS> findByRedSocial(String redSocial);

    @Modifying
    @Query("delete from PublicacionRRSS p where p.fechaPublicacion < :fecha")
    void deleteOlderThan(@Param("fecha") LocalDateTime fecha);
}
