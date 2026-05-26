package com.unired.infrastructure.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unired.application.dto.response.PublicacionRRSSDTO;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstagramAdapter implements RedSocialAdapter {

    private static final String GRAPH_BASE = "https://graph.instagram.com/v19.0";
    private static final String MEDIA_FIELDS = "id,caption,media_type,media_url,thumbnail_url,permalink,timestamp,like_count,comments_count";

    private final RestTemplate restTemplate;

    @Value("${INSTAGRAM_TOKEN:}")
    private String token;

    @Value("${INSTAGRAM_USER_ID:}")
    private String igUserId;

    @Value("${INSTAGRAM_PERFIL_NOMBRE:Uniminuto Cundinamarca}")
    private String perfilNombre;

    @Override
    public List<PublicacionRRSSDTO> obtenerPublicaciones() {
        if (token.isBlank()) {
            return Collections.emptyList();
        }

        try {
            String userId = resolverUserId();
            if (userId.isBlank()) return Collections.emptyList();

            String url = UriComponentsBuilder
                    .fromHttpUrl(GRAPH_BASE + "/" + userId + "/media")
                    .queryParam("fields", MEDIA_FIELDS)
                    .queryParam("limit", 20)
                    .queryParam("access_token", token)
                    .toUriString();

            MediaListResponse response = restTemplate.getForObject(url, MediaListResponse.class);
            if (response == null || response.getData() == null) return Collections.emptyList();

            return response.getData().stream()
                    .filter(m -> "IMAGE".equals(m.getMediaType()) || "CAROUSEL_ALBUM".equals(m.getMediaType()))
                    .map(m -> PublicacionRRSSDTO.builder()
                            .redSocial("INSTAGRAM")
                            .perfilNombre(perfilNombre)
                            .contenidoTexto(m.getCaption() != null ? m.getCaption() : "")
                            .imagenUrl(m.getMediaUrl())
                            .urlPublicacion(m.getPermalink())
                            .hashtags(extraerHashtags(m.getCaption()))
                            .likes(m.getLikeCount() != null ? m.getLikeCount() : 0)
                            .comentarios(m.getCommentsCount() != null ? m.getCommentsCount() : 0)
                            .fechaPublicacion(parseFecha(m.getTimestamp()))
                            .build())
                    .toList();

        } catch (Exception e) {
            log.warn("Error al obtener posts de Instagram: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public String getNombre() {
        return "INSTAGRAM";
    }

    private String resolverUserId() {
        if (!igUserId.isBlank()) return igUserId;
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(GRAPH_BASE + "/me")
                    .queryParam("fields", "id,username")
                    .queryParam("access_token", token)
                    .toUriString();
            MeResponse me = restTemplate.getForObject(url, MeResponse.class);
            return me != null ? me.getId() : "";
        } catch (Exception e) {
            log.warn("No se pudo resolver el ID de usuario de Instagram: {}", e.getMessage());
            return "";
        }
    }

    private String extraerHashtags(String caption) {
        if (caption == null || caption.isBlank()) return "";
        return Arrays.stream(caption.split("\\s+"))
                .filter(w -> w.startsWith("#"))
                .map(w -> w.replaceAll("[^\\w#áéíóúÁÉÍÓÚüÜñÑ]", ""))
                .filter(w -> w.length() > 1)
                .collect(Collectors.joining(","));
    }

    private LocalDateTime parseFecha(String timestamp) {
        if (timestamp == null) return LocalDateTime.now();
        try {
            return OffsetDateTime.parse(timestamp).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    // DTO internos para parsear la respuesta de Graph API

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class MediaListResponse {
        private List<MediaItem> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class MediaItem {
        private String id;
        private String caption;
        @JsonProperty("media_type")
        private String mediaType;
        @JsonProperty("media_url")
        private String mediaUrl;
        @JsonProperty("thumbnail_url")
        private String thumbnailUrl;
        private String permalink;
        private String timestamp;
        @JsonProperty("like_count")
        private Integer likeCount;
        @JsonProperty("comments_count")
        private Integer commentsCount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class MeResponse {
        private String id;
        private String username;
    }
}
