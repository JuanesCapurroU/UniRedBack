package com.unired.application.service;

import com.unired.application.dto.request.ActividadRequest;
import com.unired.application.dto.request.FiltroActividadDTO;
import com.unired.application.dto.response.ActividadResponse;
import com.unired.application.dto.response.InscripcionResponse;
import com.unired.application.mapper.ActividadMapper;
import com.unired.domain.model.Actividad;
import com.unired.domain.model.Estudiante;
import com.unired.domain.model.Inscripcion;
import com.unired.domain.model.Usuario;
import com.unired.domain.repository.ActividadRepository;
import com.unired.domain.repository.ConfiguracionNotificacionesRepository;
import com.unired.domain.repository.InscripcionRepository;
import com.unired.domain.repository.UsuarioRepository;
import com.unired.exception.custom.CancelacionFueraDeplazoException;
import com.unired.exception.custom.RecursoNoEncontradoException;
import com.unired.exception.custom.SinCuposException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;
    private final ConfiguracionNotificacionesRepository configuracionNotificacionesRepository;
    private final ActividadMapper actividadMapper;
    private final NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public Page<ActividadResponse> listarActividades(FiltroActividadDTO filtro) {
        return listarActividades(filtro, null);
    }

    @Transactional(readOnly = true)
    public Page<ActividadResponse> listarActividades(FiltroActividadDTO filtro, Long estudianteId) {
        Page<Actividad> page = actividadRepository.findFiltered(
                filtro.getCategoria(),
                filtro.getFecha(),
                filtro.getRecordatorioWa(),
                PageRequest.of(filtro.getPage(), filtro.getSize())
        );

        List<ActividadResponse> mapped = page.getContent().stream()
                .map(actividad -> mapActividadResponse(actividad, estudianteId))
                .toList();

        return new PageImpl<>(mapped, page.getPageable(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ActividadResponse obtenerActividad(Long actividadId, Long estudianteId) {
        Actividad actividad = actividadRepository.findByIdAndActivaTrue(actividadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));
        return mapActividadResponse(actividad, estudianteId);
    }

    @Transactional
    public ActividadResponse crearActividad(ActividadRequest request, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Actividad actividad = actividadMapper.toEntity(request);
        actividad.setCreadoPor(usuario);
        Actividad saved = actividadRepository.save(actividad);
        return actividadMapper.toResponse(saved);
    }

    @Transactional
    public ActividadResponse actualizarActividad(Long id, ActividadRequest request) {
        Actividad actividad = actividadRepository.findByIdAndActivaTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));

        actividad.setNombre(request.getNombre());
        actividad.setLugar(request.getLugar());
        actividad.setDescripcion(request.getDescripcion());
        actividad.setFechaHora(request.getFechaHora());
        actividad.setDuracionMinutos(request.getDuracionMinutos());
        actividad.setCategoria(request.getCategoria());

        int usedSlots = actividad.getCupoTotal() - actividad.getCupoDisponible();
        actividad.setCupoTotal(request.getCupoTotal());
        actividad.setCupoDisponible(Math.max(request.getCupoTotal() - usedSlots, 0));

        Actividad updated = actividadRepository.save(actividad);
        return actividadMapper.toResponse(updated);
    }

    @Transactional
    public void eliminarActividad(Long id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));
        actividad.setActiva(false);
        actividadRepository.save(actividad);
    }

    @Transactional
    public InscripcionResponse inscribirse(Long estudianteId, Long actividadId) {
        Usuario usuario = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));

        if (!(usuario instanceof Estudiante estudiante)) {
            throw new RecursoNoEncontradoException("Usuario no es estudiante");
        }

        Actividad actividad = actividadRepository.findByIdForUpdate(actividadId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Actividad no encontrada"));

        if (!actividad.tieneCupo()) {
            throw new SinCuposException("No hay cupos disponibles");
        }

        if (inscripcionRepository.existsByEstudianteIdAndActividadId(estudianteId, actividadId)) {
            throw new IllegalStateException("El estudiante ya está inscrito");
        }

        actividad.decrementarCupo();
        actividadRepository.save(actividad);

        Inscripcion inscripcion = Inscripcion.builder()
                .estudiante(estudiante)
                .actividad(actividad)
                .estado("ACTIVA")
                .build();

        Inscripcion saved = inscripcionRepository.save(inscripcion);

        boolean recibeActividades = configuracionNotificacionesRepository.findByUsuarioId(estudianteId)
                .map(config -> Boolean.TRUE.equals(config.getRecibirActividades()))
                .orElse(true);

        if (recibeActividades) {
            notificacionService.crearNotificacion(
                    estudianteId,
                    "ACTIVIDAD",
                    "Inscripción confirmada",
                    "Tu cupo en " + actividad.getNombre() + " ha sido reservado exitosamente.",
                    "MEDIA",
                    null
            );
        }

        return actividadMapper.toInscripcionResponse(saved);
    }

    @Transactional
    public void cancelarInscripcion(Long actividadId, Long estudianteId) {
        Inscripcion inscripcion = inscripcionRepository
                .findByEstudianteIdAndActividadIdAndEstado(estudianteId, actividadId, "ACTIVA")
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción activa no encontrada"));

        Actividad actividad = inscripcion.getActividad();
        if (!actividad.esCancelable(LocalDateTime.now())) {
            throw new CancelacionFueraDeplazoException("No se puede cancelar con menos de 2 horas de anticipación");
        }

        inscripcion.cancelar();
        actividad.incrementarCupo();

        inscripcionRepository.save(inscripcion);
        actividadRepository.save(actividad);
    }

    @Transactional
    public void registrarAsistencia(Long actividadId, Long estudianteId) {
        Inscripcion inscripcion = inscripcionRepository
                .findByEstudianteIdAndActividadIdAndEstado(estudianteId, actividadId, "ACTIVA")
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción activa no encontrada"));

        inscripcion.marcarAsistencia();
        inscripcionRepository.save(inscripcion);
    }

    @Transactional
    public void actualizarCupos(Long id, Integer delta) {
        actividadRepository.actualizarCupos(id, delta);
    }

    @Transactional(readOnly = true)
    public List<InscripcionResponse> obtenerActividadesPorEstudiante(Long estudianteId) {
        return inscripcionRepository.findByEstudianteIdOrderByFechaInscripcionDesc(estudianteId)
                .stream()
                .map(actividadMapper::toInscripcionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> obtenerAsistentesIds(Long actividadId) {
        return inscripcionRepository.findByActividadIdAndEstado(actividadId, "ACTIVA")
                .stream()
                .map(i -> i.getEstudiante().getId())
                .toList();
    }

    private ActividadResponse mapActividadResponse(Actividad actividad, Long estudianteId) {
        ActividadResponse response = actividadMapper.toResponse(actividad);
        boolean inscrito = estudianteId != null
                && inscripcionRepository.findByEstudianteIdAndActividadIdAndEstado(estudianteId, actividad.getId(), "ACTIVA")
                .isPresent();
        response.setInscrito(inscrito);
        return response;
    }
}
