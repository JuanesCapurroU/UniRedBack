package com.unired.domain.repository;

import com.unired.domain.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    @Override
    Optional<Usuario> findById(Long id);

    boolean existsByCorreo(String correo);

    boolean existsByNumeroDocumento(String doc);
}
