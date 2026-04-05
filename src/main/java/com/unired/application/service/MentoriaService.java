package com.unired.application.service;

import com.unired.application.dto.request.PostulacionMentorDTO;
import com.unired.application.dto.request.SolicitudMentoriaDTO;
import com.unired.application.dto.response.MentorDetalleResponse;
import com.unired.application.dto.response.MentorResponse;
import com.unired.application.dto.response.SolicitudResponse;
import com.unired.application.mapper.MentoriaMapper;
import com.unired.domain.enums.Delta;
import com.unired.domain.model.Estudiante;
import com.unired.domain.model.Mentor;
import com.unired.domain.model.SolicitudMentoria;
import com.unired.domain.model.Usuario;
import com.unired.domain.repository.MentorRepository;
import com.unired.domain.repository.SolicitudMentoriaRepository;
import com.unired.domain.repository.UsuarioRepository;
import com.unired.exception.custom.MentorSinCapacidadException;
import com.unired.exception.custom.RecursoNoEncontradoException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentoriaService {

    private static final double PESO_MATERIA = 40.0;
    private static final double PESO_CALIFICACION = 30.0;
    private static final double PESO_SESIONES = 20.0;
    private static final double PESO_DISPONIBILIDAD = 10.0;

    private final MentorRepository mentorRepository;
    private final UsuarioRepository usuarioRepository;
    private final SolicitudMentoriaRepository solicitudMentoriaRepository;
    private final MentoriaMapper mentoriaMapper;

    @Transactional(readOnly = true)
    public List<MentorResponse> recomendarMentores(Long estudianteId) {
        Estudiante estudiante = getEstudiante(estudianteId);

        return mentorRepository.findByActivoTrue().stream()
                .map(mentor -> {
                    MentorResponse response = mentoriaMapper.toMentorResponse(mentor);
                    response.setPorcentajeCompatibilidad(calcularCompatibilidad(estudiante, mentor));
                    return response;
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
        return mentoriaMapper.toSolicitudResponse(saved);
    }

    @Transactional
    public void confirmarMentoria(Long solicitudId, Long mentorEstudianteId) {
        SolicitudMentoria solicitud = solicitudMentoriaRepository.findByIdAndMentorEstudianteId(solicitudId, mentorEstudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

        if (!solicitud.getMentor().tieneCapacidad()) {
            throw new MentorSinCapacidadException("El mentor no tiene capacidad disponible");
        }

        solicitud.confirmar();
        solicitudMentoriaRepository.save(solicitud);
        actualizarSesionesActivas(solicitud.getMentor().getId(), 1);
    }

    @Transactional
    public void rechazarMentoria(Long solicitudId, Long mentorEstudianteId) {
        SolicitudMentoria solicitud = solicitudMentoriaRepository.findByIdAndMentorEstudianteId(solicitudId, mentorEstudianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud no encontrada"));

        solicitud.rechazar("Rechazada por mentor");
        solicitudMentoriaRepository.save(solicitud);
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
        return solicitudMentoriaRepository.findByEstudianteIdOrderByFechaSolicitudDesc(estudianteId)
                .stream()
                .map(mentoriaMapper::toSolicitudResponse)
                .toList();
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
