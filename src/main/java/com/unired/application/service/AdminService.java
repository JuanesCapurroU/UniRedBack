package com.unired.application.service;

import com.unired.application.dto.request.ActualizarEstudianteRequest;
import com.unired.application.dto.request.CrearEstudianteRequest;
import com.unired.application.dto.request.EnviarNotificacionRequest;
import com.unired.application.dto.response.EstudianteResponse;
import com.unired.application.mapper.UsuarioMapper;
import com.unired.domain.model.Estudiante;
import com.unired.domain.model.Usuario;
import com.unired.domain.repository.UsuarioRepository;
import com.unired.exception.custom.RecursoNoEncontradoException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final ActividadService actividadService;
    private final NotificacionService notificacionService;

    @Transactional
    public EstudianteResponse crearEstudiante(CrearEstudianteRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalStateException("El correo ya está registrado");
        }
        if (usuarioRepository.existsByNumeroDocumento(request.getNumeroDocumento())) {
            throw new IllegalStateException("El documento ya está registrado");
        }

        Estudiante estudiante = Estudiante.builder()
                .tipoDocumento(request.getTipoDocumento())
                .numeroDocumento(request.getNumeroDocumento())
                .primerNombre(request.getPrimerNombre())
                .primerApellido(request.getPrimerApellido())
                .correo(request.getCorreo())
                .correoInstitucional(request.getCorreo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .programaAcademico(request.getProgramaAcademico())
                .semestre(request.getSemestre())
                .sede(request.getSede())
                .activo(true)
                .build();

        Estudiante saved = (Estudiante) usuarioRepository.save(estudiante);
        return usuarioMapper.toEstudianteResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<EstudianteResponse> listarEstudiantes(Integer page, Integer size) {
        int currentPage = page == null ? 0 : Math.max(page, 0);
        int pageSize = size == null ? 20 : Math.max(size, 1);

        List<EstudianteResponse> estudiantes = usuarioRepository.findAll().stream()
                .filter(usuario -> usuario instanceof Estudiante)
                .map(usuario -> usuarioMapper.toEstudianteResponse((Estudiante) usuario))
                .toList();

        int start = Math.min(currentPage * pageSize, estudiantes.size());
        int end = Math.min(start + pageSize, estudiantes.size());

        return new PageImpl<>(estudiantes.subList(start, end), PageRequest.of(currentPage, pageSize), estudiantes.size());
    }

    @Transactional
    public EstudianteResponse actualizarEstudiante(Long estudianteId, ActualizarEstudianteRequest request) {
        Usuario usuario = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));

        if (!(usuario instanceof Estudiante estudiante)) {
            throw new RecursoNoEncontradoException("Usuario no es estudiante");
        }

        if (request.getPrimerNombre() != null) {
            estudiante.setPrimerNombre(request.getPrimerNombre());
        }
        if (request.getPrimerApellido() != null) {
            estudiante.setPrimerApellido(request.getPrimerApellido());
        }
        if (request.getTelefono() != null) {
            estudiante.setTelefono(request.getTelefono());
        }
        if (request.getCorreo() != null) {
            estudiante.setCorreo(request.getCorreo());
            estudiante.setCorreoInstitucional(request.getCorreo());
        }
        if (request.getProgramaAcademico() != null) {
            estudiante.setProgramaAcademico(request.getProgramaAcademico());
        }
        if (request.getSemestre() != null) {
            estudiante.setSemestre(request.getSemestre());
        }
        if (request.getSede() != null) {
            estudiante.setSede(request.getSede());
        }
        if (request.getActivo() != null) {
            estudiante.setActivo(request.getActivo());
        }

        Estudiante saved = (Estudiante) usuarioRepository.save(estudiante);
        return usuarioMapper.toEstudianteResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EstudianteResponse> obtenerAsistentesActividad(Long actividadId) {
        return actividadService.obtenerAsistentesIds(actividadId).stream()
                .map(id -> usuarioRepository.findById(id)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado")))
                .filter(u -> u instanceof Estudiante)
                .map(u -> usuarioMapper.toEstudianteResponse((Estudiante) u))
                .toList();
    }

    @Transactional
    public void enviarNotificacion(EnviarNotificacionRequest request) {
        notificacionService.crearNotificacion(
                request.getUsuarioId(),
                request.getTipo(),
                request.getTitulo(),
                request.getMensaje(),
                request.getPrioridad(),
                request.getUrlAccion()
        );
    }
}
