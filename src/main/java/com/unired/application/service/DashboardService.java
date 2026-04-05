package com.unired.application.service;

import com.unired.application.dto.response.ActividadResponse;
import com.unired.application.dto.response.DashboardResponse;
import com.unired.application.dto.response.RecordatorioResponse;
import com.unired.application.mapper.ActividadMapper;
import com.unired.application.mapper.NotificacionMapper;
import com.unired.domain.model.Estudiante;
import com.unired.domain.model.Usuario;
import com.unired.domain.repository.InscripcionRepository;
import com.unired.domain.repository.NotificacionRepository;
import com.unired.domain.repository.RecordatorioRepository;
import com.unired.domain.repository.SolicitudMentoriaRepository;
import com.unired.domain.repository.UsuarioRepository;
import com.unired.exception.custom.RecursoNoEncontradoException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final InscripcionRepository inscripcionRepository;
    private final SolicitudMentoriaRepository solicitudMentoriaRepository;
    private final NotificacionRepository notificacionRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final ActividadMapper actividadMapper;
    private final NotificacionMapper notificacionMapper;

    @Transactional(readOnly = true)
    public DashboardResponse obtenerDashboard(Long estudianteId) {
        Estudiante estudiante = getEstudiante(estudianteId);

        int eventosAsistidos = (int) inscripcionRepository.findByEstudianteIdOrderByFechaInscripcionDesc(estudianteId)
                .stream()
                .filter(i -> Boolean.TRUE.equals(i.getAsistio()))
                .count();

        int mentoriasActivas = (int) solicitudMentoriaRepository.findByEstudianteIdOrderByFechaSolicitudDesc(estudianteId)
                .stream()
                .filter(s -> "CONFIRMADA".equals(s.getEstado()))
                .count();

        List<RecordatorioResponse> recordatoriosUrgentes = recordatorioRepository
                .findTop5ByEstudianteIdAndPrioridadOrderByFechaVencimientoAsc(estudianteId, "URGENTE")
                .stream()
                .map(notificacionMapper::toRecordatorioResponse)
                .toList();

        List<ActividadResponse> proximosEventos = inscripcionRepository.findByEstudianteIdOrderByFechaInscripcionDesc(estudianteId)
                .stream()
                .filter(i -> "ACTIVA".equals(i.getEstado()))
                .filter(i -> i.getActividad().getFechaHora().isAfter(LocalDateTime.now()))
                .map(i -> {
                    ActividadResponse response = actividadMapper.toResponse(i.getActividad());
                    response.setInscrito(true);
                    return response;
                })
                .limit(5)
                .toList();

        return DashboardResponse.builder()
                .nombreEstudiante(estudiante.getPrimerNombre() + " " + estudiante.getPrimerApellido())
                .programaAcademico(estudiante.getProgramaAcademico())
                .promedioAcademico(estudiante.getPromedioAcademico())
                .eventosAsistidos(eventosAsistidos)
                .mentoriasActivas(mentoriasActivas)
                .notificacionesSinLeer(notificacionRepository.countNoLeidas(estudianteId).intValue())
                .recordatoriosUrgentes(recordatoriosUrgentes)
                .proximosEventos(proximosEventos)
                .build();
    }

    private Estudiante getEstudiante(Long estudianteId) {
        Usuario usuario = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));

        if (!(usuario instanceof Estudiante estudiante)) {
            throw new RecursoNoEncontradoException("Usuario no es estudiante");
        }

        return estudiante;
    }
}
