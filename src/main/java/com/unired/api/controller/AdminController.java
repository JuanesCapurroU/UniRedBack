package com.unired.api.controller;

import com.unired.application.dto.request.ActualizarEstudianteRequest;
import com.unired.application.dto.request.CrearEstudianteRequest;
import com.unired.application.dto.request.EnviarNotificacionRequest;
import com.unired.application.dto.response.ApiResponse;
import com.unired.application.dto.response.EstudianteResponse;
import com.unired.application.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Admin", description = "Operaciones administrativas")
public class AdminController extends BaseController {

    private final AdminService adminService;

    @Operation(summary = "Crear estudiante")
    @PostMapping("/usuarios")
    public ResponseEntity<ApiResponse<EstudianteResponse>> crearEstudiante(
            @Valid @RequestBody CrearEstudianteRequest request
    ) {
        return ok("Estudiante creado", adminService.crearEstudiante(request));
    }

    @Operation(summary = "Listar estudiantes")
    @GetMapping("/usuarios")
    public ResponseEntity<ApiResponse<Page<EstudianteResponse>>> listarEstudiantes(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ok("Estudiantes obtenidos", adminService.listarEstudiantes(page, size));
    }

    @Operation(summary = "Actualizar estudiante")
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<ApiResponse<EstudianteResponse>> actualizarEstudiante(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstudianteRequest request
    ) {
        return ok("Estudiante actualizado", adminService.actualizarEstudiante(id, request));
    }

    @Operation(summary = "Obtener asistentes de actividad")
    @GetMapping("/actividades/{id}/asistentes")
    public ResponseEntity<ApiResponse<List<EstudianteResponse>>> obtenerAsistentes(@PathVariable Long id) {
        return ok("Asistentes obtenidos", adminService.obtenerAsistentesActividad(id));
    }

    @Operation(summary = "Enviar notificación a usuario")
    @PostMapping("/notificaciones/enviar")
    public ResponseEntity<ApiResponse<Void>> enviarNotificacion(
            @Valid @RequestBody EnviarNotificacionRequest request
    ) {
        adminService.enviarNotificacion(request);
        return ok("Notificación enviada");
    }
}
