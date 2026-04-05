package com.unired.api.controller;

import com.unired.application.dto.request.ConfiguracionNotificacionesRequest;
import com.unired.application.dto.response.ApiResponse;
import com.unired.application.dto.response.ConfiguracionNotificacionesResponse;
import com.unired.application.dto.response.NotificacionResponse;
import com.unired.application.service.NotificacionService;
import com.unired.infrastructure.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Gestión de notificaciones y preferencias")
public class NotificacionController extends BaseController {

    private final NotificacionService notificacionService;

    @Operation(summary = "Obtener notificaciones")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> obtenerNotificaciones(
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return ok("Notificaciones obtenidas", notificacionService.obtenerNotificacionesPorUsuario(user.getId()));
    }

    @Operation(summary = "Marcar notificación como leída")
    @PutMapping("/{id}/leer")
    public ResponseEntity<ApiResponse<Void>> marcarLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        notificacionService.marcarLeida(id, user.getId());
        return ok("Notificación marcada como leída");
    }

    @Operation(summary = "Marcar todas las notificaciones como leídas")
    @PutMapping("/leer-todas")
    public ResponseEntity<ApiResponse<Void>> marcarTodasLeidas(@AuthenticationPrincipal AppUserDetails user) {
        notificacionService.marcarTodasLeidas(user.getId());
        return ok("Todas las notificaciones fueron marcadas como leídas");
    }

    @Operation(summary = "Obtener configuración de notificaciones")
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<ConfiguracionNotificacionesResponse>> obtenerConfig(
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return ok("Configuración obtenida", notificacionService.obtenerConfiguracion(user.getId()));
    }

    @Operation(summary = "Actualizar configuración de notificaciones")
    @PutMapping("/config")
    public ResponseEntity<ApiResponse<Void>> actualizarConfig(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody ConfiguracionNotificacionesRequest request
    ) {
        notificacionService.actualizarConfiguracion(user.getId(), request);
        return ok("Configuración actualizada");
    }
}
