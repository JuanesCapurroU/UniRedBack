package com.unired.application.service;

import com.unired.application.dto.request.PublicacionRequest;
import com.unired.application.dto.response.PublicacionRRSSResponse;
import com.unired.application.mapper.RRSSMapper;
import com.unired.domain.model.PublicacionRRSS;
import com.unired.exception.custom.RecursoNoEncontradoException;
import com.unired.domain.repository.PublicacionRRSSRepository;
import com.unired.infrastructure.external.RedSocialAdapter;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RRSSService {

    private final PublicacionRRSSRepository publicacionRRSSRepository;
    private final List<RedSocialAdapter> adapters;
    private final RRSSMapper rrssMapper;

    @Transactional(readOnly = true)
    public Page<PublicacionRRSSResponse> obtenerFeed(String redSocial, String hashtag, Integer page, Integer size) {
        List<PublicacionRRSS> publicaciones = obtenerTodasPublicaciones(redSocial);

        if (hashtag != null && !hashtag.isBlank()) {
            String normalized = hashtag.startsWith("#") ? hashtag.substring(1) : hashtag;
            publicaciones = publicaciones.stream()
                    .filter(p -> p.getHashtags() != null && p.getHashtags().toLowerCase().contains(normalized.toLowerCase()))
                    .toList();
        }

        publicaciones = publicaciones.stream()
                .sorted(Comparator.comparing(PublicacionRRSS::getFechaPublicacion, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int currentPage = page == null ? 0 : Math.max(page, 0);
        int pageSize = size == null ? 20 : Math.max(size, 1);
        int start = Math.min(currentPage * pageSize, publicaciones.size());
        int end = Math.min(start + pageSize, publicaciones.size());

        List<PublicacionRRSSResponse> content = publicaciones.subList(start, end)
                .stream()
                .map(rrssMapper::toResponse)
                .toList();

        return new PageImpl<>(content, PageRequest.of(currentPage, pageSize), publicaciones.size());
    }

    /** Red social ficticia para las publicaciones propias de UniRed. */
    public static final String RED_UNIRED = "UNIRED";

    @Transactional
    public PublicacionRRSSResponse publicar(PublicacionRequest request) {
        PublicacionRRSS publicacion = PublicacionRRSS.builder()
                .redSocial(RED_UNIRED)
                .perfilNombre(request.getPerfilNombre() == null || request.getPerfilNombre().isBlank()
                        ? "UniRed UNIMINUTO" : request.getPerfilNombre())
                .contenidoTexto(request.getContenidoTexto())
                .imagenUrl(request.getImagenUrl())
                .urlPublicacion(request.getUrlPublicacion())
                .hashtags(request.getHashtags())
                .likes(0)
                .comentarios(0)
                .fechaPublicacion(LocalDateTime.now())
                .fechaCache(LocalDateTime.now())
                .sincronizada(false)
                .build();

        return rrssMapper.toResponse(publicacionRRSSRepository.save(publicacion));
    }

    @Transactional
    public void eliminarPublicacion(Long id) {
        PublicacionRRSS publicacion = publicacionRRSSRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Publicación no encontrada"));
        if (!RED_UNIRED.equals(publicacion.getRedSocial())) {
            throw new IllegalStateException("Solo se pueden eliminar publicaciones propias de UniRed");
        }
        publicacionRRSSRepository.delete(publicacion);
    }

    @Transactional
    public void sincronizarFeed() {
        for (RedSocialAdapter adapter : adapters) {
            try {
                List<PublicacionRRSS> publicaciones = adapter.obtenerPublicaciones().stream()
                        .map(rrssMapper::toEntity)
                        .peek(p -> p.setRedSocial(adapter.getNombre()))
                        .toList();

                if (!publicaciones.isEmpty()) {
                    publicacionRRSSRepository.saveAll(publicaciones);
                }
            } catch (Exception ex) {
                log.warn("Error sincronizando feed de {}. Se mantiene cache local", adapter.getNombre());
            }
        }

        publicacionRRSSRepository.deleteOlderThan(LocalDateTime.now().minusMonths(6));
    }

    @Transactional(readOnly = true)
    public List<PublicacionRRSSResponse> filtrarPorHashtag(String hashtag) {
        String normalized = hashtag.startsWith("#") ? hashtag.substring(1) : hashtag;
        return publicacionRRSSRepository.findByHashtagsContaining(normalized)
                .stream()
                .map(rrssMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicacionRRSSResponse> filtrarPorRedSocial(String redSocial) {
        return publicacionRRSSRepository.findByRedSocial(redSocial)
                .stream()
                .map(rrssMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PublicacionRRSSResponse> obtenerTodasPublicaciones(String redSocial, Integer page, Integer size) {
        return obtenerFeed(redSocial, null, page, size);
    }

    @Transactional(readOnly = true)
    public List<PublicacionRRSS> obtenerTodasPublicaciones(String redSocial) {
        if (redSocial == null || redSocial.isBlank()) {
            return publicacionRRSSRepository.findAll();
        }

        return publicacionRRSSRepository.findByRedSocialOrderByFechaPublicacionDesc(redSocial);
    }

    @Transactional(readOnly = true)
    public List<PublicacionRRSSResponse> obtenerProximaMentoria(String tag) {
        return filtrarPorHashtag(tag);
    }
}
