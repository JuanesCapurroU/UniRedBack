package com.unired.api.controller;

import com.unired.application.dto.request.PostulacionMentorDTO;
import com.unired.application.dto.request.SolicitudMentoriaDTO;
import com.unired.application.dto.response.ApiResponse;
import com.unired.application.dto.response.MentorDetalleResponse;
import com.unired.application.dto.response.MentorResponse;
import com.unired.application.dto.response.SolicitudResponse;
import com.unired.application.service.MentoriaService;
import com.unired.infrastructure.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mentorias")
@RequiredArgsConstructor
@Tag(name = "Mentorías", description = "Módulo de mentorías académicas")
public class MentoriaController extends BaseController {

    private final MentoriaService mentoriaService;

    @Operation(summary = "Recomendar mentores")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mentores recomendados")
    })
    @GetMapping("/mentores")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<List<MentorResponse>>> recomendarMentores(
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return ok("Mentores recomendados", mentoriaService.recomendarMentores(user.getId()));
    }

    @Operation(summary = "Obtener detalle de mentor")
    @GetMapping("/mentores/{id}")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<MentorDetalleResponse>> detalleMentor(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return ok("Detalle de mentor", mentoriaService.obtenerMentorDetalle(user.getId(), id));
    }

    @Operation(summary = "Postular como mentor")
    @PostMapping("/postular")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<MentorResponse>> postular(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody PostulacionMentorDTO request
    ) {
        return ok("Postulación enviada", mentoriaService.postularComoMentor(user.getId(), request));
    }

    @Operation(summary = "Solicitar mentoría")
    @PostMapping("/solicitar")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<SolicitudResponse>> solicitar(
            @AuthenticationPrincipal AppUserDetails user,
            @Valid @RequestBody SolicitudMentoriaDTO request
    ) {
        return ok("Solicitud enviada", mentoriaService.solicitarMentoria(user.getId(), request));
    }

    @Operation(summary = "Confirmar solicitud de mentoría")
    @PutMapping("/solicitudes/{id}/confirmar")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<Void>> confirmar(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        mentoriaService.confirmarMentoria(id, user.getId());
        return ok("Mentoría confirmada");
    }

    @Operation(summary = "Rechazar solicitud de mentoría")
    @PutMapping("/solicitudes/{id}/rechazar")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<Void>> rechazar(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        mentoriaService.rechazarMentoria(id, user.getId());
        return ok("Mentoría rechazada");
    }

    @Operation(summary = "Listar mis solicitudes")
    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<List<SolicitudResponse>>> misSolicitudes(
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return ok("Solicitudes obtenidas", mentoriaService.obtenerMisSolicitudes(user.getId()));
    }

    @Operation(summary = "Aprobar mentor")
    @PutMapping("/mentores/{id}/aprobar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> aprobarMentor(@PathVariable Long id) {
        mentoriaService.aprobarMentor(id);
        return ok("Mentor aprobado");
    }

    @Operation(summary = "Rechazar mentor")
    @PutMapping("/mentores/{id}/rechazar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> rechazarMentor(@PathVariable Long id) {
        mentoriaService.rechazarMentor(id);
        return ok("Mentor rechazado");
    }
}
