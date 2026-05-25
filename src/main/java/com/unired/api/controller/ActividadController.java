package com.unired.api.controller;

import com.unired.application.dto.request.ActividadRequest;
import com.unired.application.dto.request.FiltroActividadDTO;
import com.unired.application.dto.response.ActividadResponse;
import com.unired.application.dto.response.ApiResponse;
import com.unired.application.dto.response.InscripcionResponse;
import com.unired.application.service.ActividadService;
import com.unired.domain.enums.CategoriaActividad;
import com.unired.infrastructure.security.AppUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actividades")
@RequiredArgsConstructor
@Tag(name = "Actividades", description = "Gestión de actividades institucionales")
public class ActividadController extends BaseController {

    private final ActividadService actividadService;

    @Operation(summary = "Listar actividades")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Listado exitoso")
    })
    @GetMapping
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<Page<ActividadResponse>>> listarActividades(
            @RequestParam(required = false) CategoriaActividad categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Boolean recordatorioWa,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        FiltroActividadDTO filtro = new FiltroActividadDTO();
        filtro.setCategoria(categoria);
        filtro.setFecha(fecha);
        filtro.setRecordatorioWa(recordatorioWa);
        filtro.setPage(page);
        filtro.setSize(size);

        Page<ActividadResponse> response = actividadService.listarActividades(filtro, user.getId());
        return ok("Actividades obtenidas", response);
    }

    @Operation(summary = "Obtener actividad por ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Actividad encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<ActividadResponse>> obtenerActividad(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return ok("Actividad obtenida", actividadService.obtenerActividad(id, user.getId()));
    }

    @Operation(summary = "Crear actividad")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Actividad creada")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ESTUDIANTE', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ActividadResponse>> crearActividad(
            @Valid @RequestBody ActividadRequest request,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return ok("Actividad creada", actividadService.crearActividad(request, user.getId()));
    }

    @Operation(summary = "Actualizar actividad")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Actividad actualizada")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ActividadResponse>> actualizarActividad(
            @PathVariable Long id,
            @Valid @RequestBody ActividadRequest request
    ) {
        return ok("Actividad actualizada", actividadService.actualizarActividad(id, request));
    }

    @Operation(summary = "Eliminar actividad")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> eliminarActividad(@PathVariable Long id) {
        actividadService.eliminarActividad(id);
        return ok("Actividad eliminada");
    }

    @Operation(summary = "Inscribirse en actividad")
    @PostMapping("/{id}/inscribir")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<InscripcionResponse>> inscribirse(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        return ok("Inscripción realizada", actividadService.inscribirse(user.getId(), id));
    }

    @Operation(summary = "Cancelar inscripción")
    @DeleteMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<Void>> cancelarInscripcion(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        actividadService.cancelarInscripcion(id, user.getId());
        return ok("Inscripción cancelada");
    }

    @Operation(summary = "Registrar asistencia")
    @PostMapping("/{id}/asistencia/{estudianteId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Void>> registrarAsistencia(
            @PathVariable Long id,
            @PathVariable Long estudianteId
    ) {
        actividadService.registrarAsistencia(id, estudianteId);
        return ok("Asistencia registrada");
    }
}
