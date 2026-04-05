package com.unired.api.controller;

import com.unired.application.dto.request.ChangePasswordRequest;
import com.unired.application.dto.request.FotoRequest;
import com.unired.application.dto.request.TelefonoRequest;
import com.unired.application.dto.response.ApiResponse;
import com.unired.application.dto.response.EstadisticasResponse;
import com.unired.application.dto.response.InscripcionResponse;
import com.unired.application.dto.response.PerfilResponse;
import com.unired.application.service.PerfilService;
import com.unired.infrastructure.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/perfil")
@RequiredArgsConstructor
@Tag(name = "Perfil", description = "Gestión del perfil académico")
public class PerfilController extends BaseController {

    private final PerfilService perfilService;

    @Operation(summary = "Obtener perfil")
    @GetMapping
    public ResponseEntity<ApiResponse<PerfilResponse>> obtenerPerfil(@AuthenticationPrincipal AppUserDetails user) {
        return ok("Perfil obtenido", perfilService.obtenerPerfil(user.getId()));
    }

    @Operation(summary = "Actualizar teléfono")
    @PutMapping("/telefono")
    public ResponseEntity<ApiResponse<Void>> actualizarTelefono(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody TelefonoRequest request
    ) {
        perfilService.actualizarTelefono(user.getId(), request.getTelefono());
        return ok("Teléfono actualizado");
    }

    @Operation(summary = "Cambiar contraseña")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        perfilService.cambiarPassword(user.getId(), request);
        return ok("Contraseña actualizada");
    }

    @Operation(summary = "Actualizar foto")
    @PutMapping("/foto")
    public ResponseEntity<ApiResponse<String>> actualizarFoto(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody FotoRequest request
    ) {
        return ok("Foto actualizada", perfilService.actualizarFoto(user.getId(), request.getFotoUrl()));
    }

    @Operation(summary = "Obtener estadísticas del perfil")
    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<EstadisticasResponse>> estadisticas(@AuthenticationPrincipal AppUserDetails user) {
        return ok("Estadísticas obtenidas", perfilService.obtenerEstadisticas(user.getId()));
    }

    @Operation(summary = "Obtener historial de asistencias")
    @GetMapping("/historial-asistencias")
    public ResponseEntity<ApiResponse<List<InscripcionResponse>>> historialAsistencias(
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return ok("Historial obtenido", perfilService.obtenerHistorialAsistencias(user.getId()));
    }
}
