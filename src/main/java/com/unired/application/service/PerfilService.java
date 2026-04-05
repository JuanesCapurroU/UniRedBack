package com.unired.application.service;

import com.unired.application.dto.request.ChangePasswordRequest;
import com.unired.application.dto.response.EstadisticasResponse;
import com.unired.application.dto.response.InscripcionResponse;
import com.unired.application.dto.response.PerfilResponse;
import com.unired.application.mapper.ActividadMapper;
import com.unired.application.mapper.UsuarioMapper;
import com.unired.domain.model.Estudiante;
import com.unired.domain.model.Usuario;
import com.unired.domain.repository.InscripcionRepository;
import com.unired.domain.repository.SolicitudMentoriaRepository;
import com.unired.domain.repository.UsuarioRepository;
import com.unired.exception.custom.RecursoNoEncontradoException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final InscripcionRepository inscripcionRepository;
    private final SolicitudMentoriaRepository solicitudMentoriaRepository;
    private final ActividadMapper actividadMapper;

    @Transactional(readOnly = true)
    public PerfilResponse obtenerPerfil(Long usuarioId) {
        Usuario usuario = getUsuario(usuarioId);
        if (usuario instanceof Estudiante estudiante) {
            return usuarioMapper.toPerfilResponse(estudiante);
        }

        return PerfilResponse.builder()
                .id(usuario.getId())
                .primerNombre(usuario.getPrimerNombre())
                .primerApellido(usuario.getPrimerApellido())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .fotoUrl(usuario.getFotoUrl())
                .rol("ADMINISTRADOR")
                .build();
    }

    @Transactional
    public void actualizarTelefono(Long usuarioId, String telefono) {
        Usuario usuario = getUsuario(usuarioId);
        usuario.setTelefono(telefono);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarPassword(Long usuarioId, ChangePasswordRequest request) {
        Usuario usuario = getUsuario(usuarioId);

        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (!request.getPasswordNueva().equals(request.getConfirmacionPassword())) {
            throw new IllegalArgumentException("La confirmación de contraseña no coincide");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public String actualizarFoto(Long usuarioId, String fotoUrl) {
        Usuario usuario = getUsuario(usuarioId);
        usuario.setFotoUrl(fotoUrl);
        usuarioRepository.save(usuario);
        return fotoUrl;
    }

    @Transactional(readOnly = true)
    public EstadisticasResponse obtenerEstadisticas(Long usuarioId) {
        int eventosInscritos = inscripcionRepository.findByEstudianteIdOrderByFechaInscripcionDesc(usuarioId).size();
        int eventosAsistidos = (int) inscripcionRepository.findByEstudianteIdOrderByFechaInscripcionDesc(usuarioId)
                .stream()
                .filter(i -> Boolean.TRUE.equals(i.getAsistio()))
                .count();
        int mentoriasSolicitadas = solicitudMentoriaRepository.findByEstudianteIdOrderByFechaSolicitudDesc(usuarioId).size();
        int mentoriasActivas = (int) solicitudMentoriaRepository.findByEstudianteIdOrderByFechaSolicitudDesc(usuarioId)
                .stream()
                .filter(s -> "CONFIRMADA".equals(s.getEstado()))
                .count();

        return EstadisticasResponse.builder()
                .eventosInscritos(eventosInscritos)
                .eventosAsistidos(eventosAsistidos)
                .mentoriasSolicitadas(mentoriasSolicitadas)
                .mentoriasActivas(mentoriasActivas)
                .build();
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> obtenerHistorialAsistencias(Long usuarioId) {
        return inscripcionRepository.findByEstudianteIdOrderByFechaInscripcionDesc(usuarioId)
                .stream()
                .map(actividadMapper::toInscripcionResponse)
                .toList();
    }

    private Usuario getUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
