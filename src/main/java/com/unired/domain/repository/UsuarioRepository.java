package com.unired.domain.repository;

import com.unired.domain.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    @Override
    Optional<Usuario> findById(Long id);

    boolean existsByCorreo(String correo);

    boolean existsByNumeroDocumento(String doc);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE usuarios SET activo = :activo WHERE id = :id", nativeQuery = true)
    int updateActivo(@Param("id") Long id, @Param("activo") boolean activo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE usuarios SET dtype = 'ADMINISTRADOR', nivel_acceso = 'ADMIN' WHERE id = :id", nativeQuery = true)
    int promoteToAdmin(@Param("id") Long id);
}
