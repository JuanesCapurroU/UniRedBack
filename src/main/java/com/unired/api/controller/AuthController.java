package com.unired.api.controller;

import com.unired.application.dto.request.FcmTokenRequest;
import com.unired.application.dto.request.LoginRequest;
import com.unired.application.dto.request.RefreshTokenRequest;
import com.unired.application.dto.response.ApiResponse;
import com.unired.application.dto.response.LoginResponse;
import com.unired.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticación y gestión de sesión")
public class AuthController extends BaseController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getCorreo(), request.getPassword());
        return ok("Inicio de sesión exitoso", response);
    }

    @Operation(summary = "Refrescar token")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token renovado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Refresh token inválido")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return ok("Token renovado correctamente", response);
    }

    @Operation(summary = "Cerrar sesión")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Sesión cerrada")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization);
        return ok("Sesión cerrada");
    }

    @Operation(summary = "Actualizar token FCM")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token FCM actualizado")
    })
    @PutMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody FcmTokenRequest request
    ) {
        authService.actualizarFcmToken(authorization, request.getFcmToken());
        return ok("Token FCM actualizado");
    }
}
