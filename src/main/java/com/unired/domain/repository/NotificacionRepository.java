package com.unired.domain.repository;

import com.unired.domain.model.Notificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    @Query(value = """
            SELECT *
            FROM notificaciones
            WHERE usuario_id = :userId
            ORDER BY fecha DESC
            LIMIT :keep
            """, nativeQuery = true)
    List<Notificacion> findByReceivedUserId(@Param("userId") Long userId, @Param("keep") Long keep);

    @Query("""
            select n from Notificacion n
            where n.usuario.id = :userId
            and n.esActiva = true
            order by n.fecha desc
            """)
    List<Notificacion> findReceivedNotificaciones(@Param("userId") Long userId);

    @Modifying
    @Query(value = """
            DELETE FROM notificaciones
            WHERE id IN (
              SELECT id
              FROM notificaciones
              WHERE usuario_id = :usuarioId
              AND leida = true
              ORDER BY fecha DESC
              OFFSET :keep
            )
            """, nativeQuery = true)
    void deleteLeidasByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("keep") Long keep);

    @Modifying
    @Query("update Notificacion n set n.leida = true where n.usuario.id = :usuarioId")
    void marcarTodasLeidas(@Param("usuarioId") Long usuarioId);

    @Query("select count(n) from Notificacion n where n.usuario.id = :usuarioId and n.leida = false")
    Long countNoLeidas(@Param("usuarioId") Long usuarioId);
}
