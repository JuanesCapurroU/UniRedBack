package com.unired.api.controller;

import com.unired.application.dto.response.ApiResponse;
import com.unired.application.dto.response.PublicacionRRSSResponse;
import com.unired.application.service.RRSSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rrss")
@RequiredArgsConstructor
@Tag(name = "RRSS", description = "Feed institucional cacheado de redes sociales")
public class RRSSController extends BaseController {

    private final RRSSService rrssService;

    @Operation(summary = "Obtener feed de publicaciones")
    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<Page<PublicacionRRSSResponse>>> obtenerFeed(
            @RequestParam(required = false) String red,
            @RequestParam(required = false) String hashtag,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ok("Feed obtenido", rrssService.obtenerFeed(red, hashtag, page, size));
    }

    @Operation(summary = "Sincronizar feed manualmente")
    @PostMapping("/sincronizar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> sincronizar() {
        rrssService.sincronizarFeed();
        return ok("Sincronización ejecutada");
    }
}
