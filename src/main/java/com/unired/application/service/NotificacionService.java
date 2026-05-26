package com.unired.application.service;

import com.unired.application.dto.request.ConfiguracionNotificacionesRequest;
import com.unired.application.dto.response.ConfiguracionNotificacionesResponse;
import com.unired.application.dto.response.NotificacionResponse;
import com.unired.application.mapper.NotificacionMapper;
import com.unired.domain.model.ConfiguracionNotificaciones;
import com.unired.domain.model.Notificacion;
import com.unired.domain.model.Usuario;
import com.unired.domain.repository.ConfiguracionNotificacionesRepository;
import com.unired.domain.repository.NotificacionRepository;
import com.unired.domain.repository.UsuarioRepository;
import com.unired.exception.custom.RecursoNoEncontradoException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionNotificacionesRepository configuracionNotificacionesRepository;
    private final NotificacionMapper notificacionMapper;

    @Transactional(readOnly = true)
    public List<NotificacionResponse> obtenerNotificacionesPorUsuario(Long id) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdOrderByFechaDesc(id);

        if (notificaciones.size() > 20) {
            List<Notificacion> keep = notificaciones.subList(0, 20);
            List<Notificacion> delete = notificaciones.subList(20, notificaciones.size());
            notificacionRepository.deleteAll(delete);
            return keep.stream().map(notificacionMapper::toResponse).toList();
        }

        return notificaciones.stream().map(notificacionMapper::toResponse).toList();
    }

    @Transactional
    public void marcarLeida(Long notifId, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(notifId)
                .filter(n -> n.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación no encontrada"));

        notificacion.marcarLeida();
        notificacionRepository.save(notificacion);
    }

    @Transactional
    public void marcarTodasLeidas(Long usuarioId) {
        notificacionRepository.marcarTodasLeidas(usuarioId);
    }

    @Transactional
    public void marcarActiva(Long usuarioId, Boolean activo) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
        notificaciones.forEach(n -> {
            if ("ALTA".equalsIgnoreCase(n.getPrioridad()) && Boolean.FALSE.equals(activo)) {
                return;
            }
            n.marcarActiva(activo);
        });
        notificacionRepository.saveAll(notificaciones);
    }

    @Transactional
    public void eliminarAntiguas(Long usuarioId, Long keep) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
        List<Notificacion> toDelete = notificaciones.stream()
                .sorted(Comparator.comparing(Notificacion::getFecha).reversed())
                .skip(keep)
                .toList();
        notificacionRepository.deleteAll(toDelete);
    }

    @Transactional
    public void crearNotificacion(
            Long usuarioId,
            String tipo,
            String titulo,
            String mensaje,
            String prioridad,
            String urlAccion
    ) {
        Usuario usuario = getUsuario(usuarioId);
        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .tipo(tipo)
                .titulo(titulo)
                .mensaje(mensaje)
                .prioridad(prioridad)
                .urlAccion(urlAccion)
                .leida(false)
                .esActiva(true)
                .build();
        notificacionRepository.save(notificacion);
        log.debug("Notificación creada para usuario {}: {}", usuarioId, titulo);
    }

    @Transactional(readOnly = true)
    public Long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countNoLeidas(usuarioId);
    }

    @Transactional
    public ConfiguracionNotificacionesResponse obtenerConfiguracion(Long usuarioId) {
        ConfiguracionNotificaciones config = configuracionNotificacionesRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearConfiguracionDefault(getUsuario(usuarioId)));
        return notificacionMapper.toConfigResponse(config);
    }

    @Transactional
    public void actualizarConfiguracion(Long usuarioId, ConfiguracionNotificacionesRequest request) {
        ConfiguracionNotificaciones config = configuracionNotificacionesRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> crearConfiguracionDefault(getUsuario(usuarioId)));

        if (request.getRecibirTramites() != null) {
            config.setRecibirTramites(request.getRecibirTramites());
        }
        if (request.getRecibirActividades() != null) {
            config.setRecibirActividades(request.getRecibirActividades());
        }
        if (request.getRecibirMentoria() != null) {
            config.setRecibirMentoria(request.getRecibirMentoria());
        }
        if (request.getRecibirRrss() != null) {
            config.setRecibirRrss(request.getRecibirRrss());
        }
        if (request.getRecibirBot() != null) {
            config.setRecibirBot(request.getRecibirBot());
        }
        if (request.getModificadoMentoria() != null) {
            config.setModificadoMentoria(request.getModificadoMentoria());
        }
        if (request.getNumeroWhatsapp() != null) {
            config.setNumeroWhatsapp(request.getNumeroWhatsapp());
        }

        configuracionNotificacionesRepository.save(config);
    }

    private ConfiguracionNotificaciones crearConfiguracionDefault(Usuario usuario) {
        ConfiguracionNotificaciones config = ConfiguracionNotificaciones.builder()
                .usuario(usuario)
                .build();
        return configuracionNotificacionesRepository.save(config);
    }

    private Usuario getUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
