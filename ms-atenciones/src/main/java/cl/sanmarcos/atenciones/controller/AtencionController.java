package cl.sanmarcos.atenciones.controller;

import cl.sanmarcos.atenciones.dto.AtencionRequest;
import cl.sanmarcos.atenciones.dto.AtencionResponse;
import cl.sanmarcos.atenciones.dto.ReporteAtencionesResponse;
import cl.sanmarcos.atenciones.model.TipoAtencion;
import cl.sanmarcos.atenciones.security.UsuarioAutenticado;
import cl.sanmarcos.atenciones.service.AtencionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/atenciones")
@Tag(name = "Atenciones Clínicas", description = "Registro y consulta de consultas, vacunas, exámenes, diagnósticos y recetas")

public class AtencionController {
    private final AtencionService service;

    public AtencionController(AtencionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una nueva atención clínica (Veterinario / Administrador)")
    public AtencionResponse crear(@Valid @RequestBody AtencionRequest req,
                                @AuthenticationPrincipal UsuarioAutenticado user) {
        return service.crear(req, user);
    }

    @GetMapping
    @Operation(summary = "Lista atenciones (personal: todas con filtros ?tipo= ?mascotaId= ?fecha=; dueño: solo las de sus mascotas)")
    public List<AtencionResponse> listar(@AuthenticationPrincipal UsuarioAutenticado user,
                                        @RequestParam(required = false) TipoAtencion tipo,
                                        @RequestParam(required = false) Long mascotaId,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.listar(user, tipo, mascotaId, fecha);
    }

    @GetMapping("/reporte")
    @Operation(summary = "Resumen de atenciones por tipo en un rango de fechas (Administrador y Veterinario)")
    public ReporteAtencionesResponse reporte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.reporte(desde, hasta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una atención clínica")
    public AtencionResponse obtener(@PathVariable Long id,
                                    @AuthenticationPrincipal UsuarioAutenticado user) {
        return service.obtener(id, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una atención clínica existente (Veterinario / Administrador)")
    public AtencionResponse actualizar(@PathVariable Long id,
                                    @Valid @RequestBody AtencionRequest req,
                                    @AuthenticationPrincipal UsuarioAutenticado user) {
        return service.actualizar(id, req, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un registro de atención clínica (Veterinario / Administrador)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}

