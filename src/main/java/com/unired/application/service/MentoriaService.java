package com.unired.application.service;

import com.unired.application.dto.request.PostulacionMentorDTO;
import com.unired.application.dto.request.CalificarMentoriaDTO;
import com.unired.application.dto.request.EnviarMensajeMentoriaDTO;
import com.unired.application.dto.request.SolicitudMentoriaDTO;
import com.unired.application.dto.response.ChatResponse;
import com.unired.application.dto.response.MensajeMentoriaResponse;
import com.unired.application.dto.response.MentorDetalleResponse;
import com.unired.application.dto.response.MentorResponse;
import com.unired.application.dto.response.SolicitudResponse;
import com.unired.application.mapper.MentoriaMapper;
import com.unired.domain.enums.Delta;
import com.unired.domain.model.MensajeMentoria;
import com.unired.domain.model.Estudiante;
import com.unired.domain.model.Mentor;
import com.unired.domain.model.SolicitudMentoria;
import com.unired.domain.model.Usuario;
import com.unired.domain.repository.MentorRepository;
import com.unired.domain.repository.MensajeMentoriaRepository;
import com.unired.domain.repository.SolicitudMentoriaRepository;
import com.unired.domain.repository.UsuarioRepository;
import com.unired.exception.custom.MentorSinCapacidadException;
import com.unired.exception.custom.RecursoNoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MentoriaService {

    private static final double PESO_MATERIA = 40.0;
    private static final double PESO_CALIFICACION = 30.0;
    private static final double PESO_SESIONES = 20.0;
    private static final double PESO_DISPONIBILIDAD = 10.0;
    private static final List<String> ESTADOS_CONVERSACION_ABIERTA = List.of("PENDIENTE", "CONFIRMADA", "FINALIZADA");

    private final MentorRepository mentorRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudMentoriaRepository solicitudMentoriaRepository;
    private final MensajeMentoriaRepository mensajeMentoriaRepository;
    private final MentoriaMapper mentoriaMapper;
    private final NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public List<MentorResponse> recomendarMentores(Long estudianteId) {
        Estudiante estudiante = getEstudiante(estudianteId);

        return mentorRepository.findByActivoTrue().stream()
                .flatMap(mentor -> {
                    try {
                        MentorResponse response = mentoriaMapper.toMentorResponse(mentor);
                        response.setPorcentajeCompatibilidad(calcularCompatibilidad(estudiante, mentor));
                        return Stream.of(response);
                    } catch (Exception e) {
                        return Stream.empty();
                    }
                })
                .sorted(Comparator.comparing(MentorResponse::getPorcentajeCompatibilidad).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public Mentor findMentorById(Long id) {
        return mentorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mentor no encontrado"));
    }

    @Transactional(readOnly = true)
    public MentorDetalleResponse obtenerMentorDetalle(Long estudianteId, Long mentorId) {
        Estudiante estudiante = getEstudiante(estudianteId);
        Mentor mentor = findMentorById(mentorId);
        MentorDetalleResponse response = mentoriaMapper.toMentorDetalleResponse(mentor);
        response.setPorcentajeCompatibilidad(calcularCompatibilidad(estudiante, mentor));
        return response;
    }

    @Transactional(readOnly = true)
    public Double calcularCompatibilidad(Long estudianteId, Long mentorId) {
        Estudiante estudiante = getEstudiante(estudianteId);
        Mentor mentor = findMentorById(mentorId);
        return calcularCompatibilidad(estudiante, mentor);
    }

    @Transactional
    public MentorResponse postularComoMentor(Long estudianteId, PostulacionMentorDTO dto) {
        Estudiante estudiante = getEstudiante(estudianteId);

        Mentor mentor = mentorRepository.findByEstudianteId(estudianteId)
                .orElseGet(() -> Mentor.builder().estudiante(estudiante).build());

        mentor.setMaterias(dto.getMaterias());
        mentor.setDisponibilidad(dto.getDisponibilidad());
        mentor.setBio(dto.getBio());
        mentor.setActivo(false);

        Mentor saved = mentorRepository.save(mentor);
        MentorResponse response = mentoriaMapper.toMentorResponse(saved);
        response.setPorcentajeCompatibilidad(0.0);
        return response;
    }

    @Transactional
    public void aprobarMentor(Long mentorId) {
        Mentor mentor = findMentorById(mentorId);
        mentor.setActivo(true);
        mentorRepository.save(mentor);
    }

    @Transactional
    public void rechazarMentor(Long mentorId) {
        Mentor mentor = findMentorById(mentorId);
        mentor.setActivo(false);
        mentorRepository.save(mentor);
    }

    @Transactional
    public SolicitudResponse solicitarMentoria(Long estudianteId, SolicitudMentoriaDTO dto) {
        Estudiante estudiante = getEstudiante(estudianteId);
        Mentor mentor = findMentorById(dto.getMentorId());

        var solicitudExistente = solicitudMentoriaRepository
                .findFirstByEstudianteIdAndMentorIdAndEstadoInOrderByFechaSolicitudDesc(
                        estudianteId, dto.getMentorId(), ESTADOS_CONVERSACION_ABIERTA);
        if (solicitudExistente.isPresent()) {
            return toSolicitudResponse(solicitudExistente.get());
        }

        if (!Boolean.TRUE.equals(mentor.getActivo())) {
            throw new IllegalStateException("El mentor no está activo");
        }

        if (!mentor.tieneCapacidad()) {
            throw new MentorSinCapacidadException("El mentor no tiene capacidad disponible");
        }

        SolicitudMentoria solicitud = SolicitudMentoria.builder()
                .estudiante(estudiante)
                .mentor(mentor)
                .porcentajeCompatibilidad(calcularCompatibilidad(estudiante, mentor))
                .motivacion(dto.getMotivacion())
                .numeroWhatsapp(dto.getNumeroWhatsapp())
                .estado("PENDIENTE")
                .build();

        SolicitudMentoria saved = solicitudMentoriaRepository.save(solicitud);

        try {
            notificacionService.crearNotificacion(
                    mentor.getEstudiante().getId(),
                    "MENTORIA",
                    "Nueva solicitud de mentoría",
                    estudiante.getPrimerNombre() + " " + estudiante.getPrimerApellido() + " te ha enviado una solicitud de mentoría.",
                    "ALTA",
                    null
            );
        } catch (Exception e) {
            log.warn("No se pudo crear notificación de solicitud de mentoría: {}", e.getMessage());
        }

        return toSolicitudResponse(saved);
    }

    @Transactional
    public void confirmarMentoria(Long solicitudId, Long mentorEstudianteId) {
        SolicitudMentoria solicitud = solicitudMentoriaRepository.findByIdAndMentorEstudianteId(solicitudId, mentorEstudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new IllegalStateException("Solo puedes aceptar solicitudes pendientes");
        }

        if (!solicitud.getMentor().tieneCapacidad()) {
            throw new MentorSinCapacidadException("El mentor no tiene capacidad disponible");
        }

        solicitud.confirmar();
        solicitudMentoriaRepository.save(solicitud);
        cerrarSolicitudesDuplicadasPendientes(solicitud, "Duplicada: solicitud ya aceptada");
        actualizarSesionesActivas(solicitud.getMentor().getId(), 1);

        try {
            notificacionService.crearNotificacion(
                    solicitud.getEstudiante().getId(),
                    "MENTORIA",
                    "Mentoría confirmada",
                    "Tu solicitud de mentoría fue aceptada. Ya puedes iniciar la sesión.",
                    "ALTA",
                    null
            );
        } catch (Exception e) {
            log.warn("No se pudo crear notificación de mentoría confirmada: {}", e.getMessage());
        }
    }

    @Transactional
    public void rechazarMentoria(Long solicitudId, Long mentorEstudianteId) {
        SolicitudMentoria solicitud = solicitudMentoriaRepository.findByIdAndMentorEstudianteId(solicitudId, mentorEstudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new IllegalStateException("Solo puedes rechazar solicitudes pendientes");
        }

        solicitud.rechazar("Rechazada por mentor");
        solicitudMentoriaRepository.save(solicitud);
        cerrarSolicitudesDuplicadasPendientes(solicitud, "Duplicada: solicitud ya rechazada");

        try {
            notificacionService.crearNotificacion(
                    solicitud.getEstudiante().getId(),
                    "MENTORIA",
                    "Solicitud de mentoría no aceptada",
                    "El mentor no pudo aceptar tu solicitud en este momento. Puedes intentar con otro mentor.",
                    "MEDIA",
                    null
            );
        } catch (Exception e) {
            log.warn("No se pudo crear notificación de mentoría rechazada: {}", e.getMessage());
        }
    }

    @Transactional
    public SolicitudResponse calificarMentoria(Long solicitudId, Long estudianteId, CalificarMentoriaDTO dto) {
        SolicitudMentoria solicitud = solicitudMentoriaRepository.findByIdAndEstudianteId(solicitudId, estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

        if (!"FINALIZADA".equals(solicitud.getEstado())) {
            throw new IllegalStateException("Primero debes finalizar la mentoría");
        }

        if (!solicitud.puedeCalificar()) {
            throw new IllegalStateException("La mentoría ya fue calificada o no está lista");
        }

        solicitud.calificar(dto.getEstrellas(), dto.getComentario());
        solicitudMentoriaRepository.save(solicitud);

        actualizarSesionesActivas(solicitud.getMentor().getId(), -1);
        actualizarEstadisticasMentor(solicitud.getMentor().getId());

        return toSolicitudResponse(solicitud);
    }

    @Transactional
    public SolicitudResponse finalizarMentoria(Long solicitudId, Long usuarioId) {
        SolicitudMentoria solicitud = solicitarSiParticipa(solicitudId, usuarioId);

        if (!solicitud.getEstudiante().getId().equals(usuarioId)) {
            throw new IllegalStateException("Solo el estudiante puede finalizar la mentoría");
        }

        if (!"CONFIRMADA".equals(solicitud.getEstado())) {
            throw new IllegalStateException("Solo puedes finalizar una mentoría confirmada");
        }

        solicitud.finalizar();
        solicitudMentoriaRepository.save(solicitud);

        try {
            notificacionService.crearNotificacion(
                    solicitud.getMentor().getEstudiante().getId(),
                    "MENTORIA",
                    "Mentoría finalizada",
                    solicitud.getEstudiante().getPrimerNombre() + " finalizó la sesión de mentoría.",
                    "MEDIA",
                    null
            );
        } catch (Exception e) {
            log.warn("No se pudo crear notificación de mentoría finalizada: {}", e.getMessage());
        }

        return toSolicitudResponse(solicitud);
    }

    @Transactional(readOnly = true)
    public List<MensajeMentoriaResponse> obtenerMensajes(Long solicitudId, Long usuarioId) {
        SolicitudMentoria solicitud = solicitarSiParticipa(solicitudId, usuarioId);
        return mensajeMentoriaRepository.findBySolicitudIdOrderByFechaEnvioAsc(solicitud.getId())
                .stream()
                .map(mensaje -> MensajeMentoriaResponse.builder()
                        .id(mensaje.getId())
                        .emisorId(mensaje.getEmisor().getId())
                        .emisorNombre(mensaje.getEmisor().getPrimerNombre() + " " + mensaje.getEmisor().getPrimerApellido())
                        .contenido(mensaje.getContenido())
                        .fechaEnvio(mensaje.getFechaEnvio())
                        .esMio(mensaje.getEmisor().getId().equals(usuarioId))
                        .build())
                .toList();
    }

    @Transactional
    public MensajeMentoriaResponse enviarMensaje(Long solicitudId, Long usuarioId, EnviarMensajeMentoriaDTO dto) {
        SolicitudMentoria solicitud = solicitarSiParticipa(solicitudId, usuarioId);

        MensajeMentoria mensaje = MensajeMentoria.builder()
                .solicitud(solicitud)
                .emisor(usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado")))
                .contenido(dto.getContenido().trim())
                .build();

        MensajeMentoria saved = mensajeMentoriaRepository.save(mensaje);

        try {
            Long receptorId = solicitud.getEstudiante().getId().equals(usuarioId)
                    ? solicitud.getMentor().getEstudiante().getId()
                    : solicitud.getEstudiante().getId();

            notificacionService.crearNotificacion(
                    receptorId,
                    "MENTORIA",
                    "Nuevo mensaje en tu mentoría",
                    saved.getEmisor().getPrimerNombre() + " te envió un mensaje.",
                    "MEDIA",
                    null
            );
        } catch (Exception e) {
            log.warn("No se pudo crear notificación de mensaje de mentoría: {}", e.getMessage());
        }

        return MensajeMentoriaResponse.builder()
                .id(saved.getId())
                .emisorId(usuarioId)
                .emisorNombre(saved.getEmisor().getPrimerNombre() + " " + saved.getEmisor().getPrimerApellido())
                .contenido(saved.getContenido())
                .fechaEnvio(saved.getFechaEnvio())
                .esMio(true)
                .build();
    }

    @Transactional
    public void actualizarSesionesActivas(Long id, Integer delta) {
        Mentor mentor = findMentorById(id);
        int target = mentor.getSesionesActivas() + delta;
        if (target < 0) {
            target = 0;
        }
        if (target > 5) {
            throw new MentorSinCapacidadException("No se pueden superar 5 sesiones activas");
        }

        mentorRepository.updateSesionesActivasWhenId(id, delta > 0 ? Delta.INCREMENT : Delta.DECREMENT);
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> obtenerMisSolicitudes(Long estudianteId) {
        Map<Long, SolicitudMentoria> porMentor = new LinkedHashMap<>();
        for (SolicitudMentoria solicitud : solicitudMentoriaRepository.findByEstudianteIdOrderByFechaSolicitudDesc(estudianteId)) {
            porMentor.merge(solicitud.getMentor().getId(), solicitud, this::elegirSolicitudPrioritaria);
        }
        return porMentor.values().stream().map(this::toSolicitudResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> obtenerChats(Long usuarioId) {
        Map<Long, ChatResponse> chatPorInterlocutor = new LinkedHashMap<>();

        for (SolicitudMentoria solicitud : solicitudMentoriaRepository.findAllByParticipanteId(usuarioId)) {
            ChatResponse chat = toChatResponse(solicitud, usuarioId);
            chatPorInterlocutor.merge(chat.getOtroUsuarioId(), chat, this::elegirChatMasReciente);
        }

        return List.copyOf(chatPorInterlocutor.values());
    }

    private ChatResponse toChatResponse(SolicitudMentoria solicitud, Long usuarioId) {
        boolean soySolicitante = solicitud.getEstudiante().getId().equals(usuarioId);
        Long otroId;
        String otroNombre;
        if (soySolicitante) {
            otroId = solicitud.getMentor().getEstudiante().getId();
            otroNombre = solicitud.getMentor().getEstudiante().getPrimerNombre()
                    + " " + solicitud.getMentor().getEstudiante().getPrimerApellido();
        } else {
            otroId = solicitud.getEstudiante().getId();
            otroNombre = solicitud.getEstudiante().getPrimerNombre()
                    + " " + solicitud.getEstudiante().getPrimerApellido();
        }

        return mensajeMentoriaRepository
                .findTopBySolicitudIdOrderByFechaEnvioDesc(solicitud.getId())
                .map(ultimo -> ChatResponse.builder()
                        .solicitudId(solicitud.getId())
                        .estado(solicitud.getEstado())
                        .otroUsuarioId(otroId)
                        .otroUsuarioNombre(otroNombre)
                        .ultimoMensaje(ultimo.getContenido())
                        .fechaUltimoMensaje(ultimo.getFechaEnvio() != null ? ultimo.getFechaEnvio().toString() : null)
                        .soySolicitante(soySolicitante)
                        .build())
                .orElseGet(() -> ChatResponse.builder()
                        .solicitudId(solicitud.getId())
                        .estado(solicitud.getEstado())
                        .otroUsuarioId(otroId)
                        .otroUsuarioNombre(otroNombre)
                        .ultimoMensaje(null)
                        .fechaUltimoMensaje(null)
                        .soySolicitante(soySolicitante)
                        .build());
    }

    private ChatResponse elegirChatMasReciente(ChatResponse actual, ChatResponse candidato) {
        if (actual.getFechaUltimoMensaje() == null) {
            return candidato;
        }
        if (candidato.getFechaUltimoMensaje() == null) {
            return actual;
        }
        return candidato.getFechaUltimoMensaje().compareTo(actual.getFechaUltimoMensaje()) > 0
                ? candidato
                : actual;
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> obtenerSolicitudesRecibidas(Long mentorEstudianteId) {
        Map<Long, SolicitudMentoria> porEstudiante = new LinkedHashMap<>();
        for (SolicitudMentoria solicitud : solicitudMentoriaRepository.findByMentorEstudianteIdOrderByFechaSolicitudDesc(mentorEstudianteId)) {
            porEstudiante.merge(solicitud.getEstudiante().getId(), solicitud, this::elegirSolicitudPrioritaria);
        }
        return porEstudiante.values().stream().map(this::toSolicitudResponse).toList();
    }

    private void cerrarSolicitudesDuplicadasPendientes(SolicitudMentoria principal, String motivo) {
        List<SolicitudMentoria> duplicadas = solicitudMentoriaRepository
                .findByEstudianteIdAndMentorIdAndEstadoAndIdNotOrderByFechaSolicitudDesc(
                        principal.getEstudiante().getId(),
                        principal.getMentor().getId(),
                        "PENDIENTE",
                        principal.getId());
        for (SolicitudMentoria duplicada : duplicadas) {
            duplicada.rechazar(motivo);
            solicitudMentoriaRepository.save(duplicada);
        }
    }

    private SolicitudMentoria elegirSolicitudPrioritaria(SolicitudMentoria actual, SolicitudMentoria candidato) {
        int prioridadActual = prioridadEstado(actual.getEstado());
        int prioridadCandidato = prioridadEstado(candidato.getEstado());
        if (prioridadCandidato != prioridadActual) {
            return prioridadCandidato > prioridadActual ? candidato : actual;
        }
        if (candidato.getFechaSolicitud() == null) {
            return actual;
        }
        if (actual.getFechaSolicitud() == null) {
            return candidato;
        }
        return candidato.getFechaSolicitud().isAfter(actual.getFechaSolicitud()) ? candidato : actual;
    }

    private int prioridadEstado(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> 4;
            case "CONFIRMADA" -> 3;
            case "FINALIZADA" -> 2;
            case "CALIFICADA" -> 1;
            default -> 0;
        };
    }

    private SolicitudMentoria solicitarSiParticipa(Long solicitudId, Long usuarioId) {
        SolicitudMentoria solicitud = solicitudMentoriaRepository.findById(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

        boolean participa = solicitud.getEstudiante().getId().equals(usuarioId)
                || solicitud.getMentor().getEstudiante().getId().equals(usuarioId);
        if (!participa) {
            throw new RecursoNoEncontradoException("Solicitud no encontrada");
        }
        return solicitud;
    }

    private SolicitudResponse toSolicitudResponse(SolicitudMentoria solicitud) {
        SolicitudResponse response = mentoriaMapper.toSolicitudResponse(solicitud);
        response.setPuedeCalificar(solicitud.puedeCalificar());
        response.setHorasRestantesCalificacion(0L);
        return response;
    }

    private void actualizarEstadisticasMentor(Long mentorId) {
        Mentor mentor = findMentorById(mentorId);
        List<SolicitudMentoria> calificadas = mentor.getSolicitudes().stream()
                .filter(s -> s.getCalificacionFinal() != null)
                .toList();

        double promedio = calificadas.isEmpty()
                ? 0.0
                : calificadas.stream().mapToInt(SolicitudMentoria::getCalificacionFinal).average().orElse(0.0);

        mentor.setCalificacionPromedio(promedio);
        mentor.setSesionesCompletadas(calificadas.size());
        mentorRepository.save(mentor);
    }

    private Estudiante getEstudiante(Long estudianteId) {
        Usuario usuario = usuarioRepository.findById(estudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado"));

        if (!(usuario instanceof Estudiante estudiante)) {
            throw new RecursoNoEncontradoException("Usuario no es estudiante");
        }
        return estudiante;
    }

    private Double calcularCompatibilidad(Estudiante estudiante, Mentor mentor) {
        List<String> materiasMentor = mentor.getMaterias() == null ? List.of() : mentor.getMaterias();
        List<String> materiasEstudiante = obtenerMateriasConDificultad(estudiante);

        long coincidencias = materiasMentor.stream()
                .map(String::toLowerCase)
                .filter(materia -> materiasEstudiante.stream().map(String::toLowerCase).anyMatch(materia::equals))
                .count();

        double scoreMateria = (coincidencias / (double) Math.max(materiasEstudiante.size(), 1)) * PESO_MATERIA;
        double scoreCalificacion = (mentor.getCalificacionPromedio() / 5.0) * PESO_CALIFICACION;
        double scoreSesiones = Math.min(mentor.getSesionesCompletadas() / 50.0, 1.0) * PESO_SESIONES;
        double scoreDisponibilidad = calcularSolapamientoHorario(estudiante, mentor) * PESO_DISPONIBILIDAD;

        return scoreMateria + scoreCalificacion + scoreSesiones + scoreDisponibilidad;
    }

    private List<String> obtenerMateriasConDificultad(Estudiante estudiante) {
        return List.of();
    }

    private double calcularSolapamientoHorario(Estudiante estudiante, Mentor mentor) {
        return mentor.getDisponibilidad() == null || mentor.getDisponibilidad().isBlank() ? 0.0 : 1.0;
    }
}
