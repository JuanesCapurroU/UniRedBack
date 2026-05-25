package com.unired.infrastructure.security;

import com.unired.domain.model.Administrador;
import com.unired.domain.model.Usuario;
import com.unired.domain.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return buildUserDetails(usuario);
    }

    public AppUserDetails buildUserDetails(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new DisabledException("Cuenta inactiva");
        }

        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new LockedException("Cuenta bloqueada");
        }

        String role = resolveRole(usuario);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return AppUserDetails.builder()
                .id(usuario.getId())
                .username(usuario.getCorreo())
                .password(usuario.getPasswordHash())
                .role(role)
                .enabled(Boolean.TRUE.equals(usuario.getActivo()))
                .blockedUntil(usuario.getBloqueadoHasta())
                .authorities(authorities)
                .build();
    }

    private String resolveRole(Usuario usuario) {
        if (usuario instanceof Administrador) {
            return "ADMINISTRADOR";
        }

        String className = usuario.getClass().getSimpleName().toUpperCase();
        return className.contains("ADMINISTRADOR") ? "ADMINISTRADOR" : "ESTUDIANTE";
    }
}
