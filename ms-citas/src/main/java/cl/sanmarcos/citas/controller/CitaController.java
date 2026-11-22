package cl.sanmarcos.citas.controller;

import cl.sanmarcos.citas.dto.*;
import cl.sanmarcos.citas.model.EstadoCita;
import cl.sanmarcos.citas.security.UsuarioAutenticado;
import cl.sanmarcos.citas.service.CitaService;
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
@RequestMapping("/api/citas")
@Tag(name = "Citas", description = "Solicitud de citas: el dueño solicita, recepción confirma o propone otro horario")
public class CitaController {

    private final CitaService service;

    public CitaController(CitaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Solicita una cita (dueño → PENDIENTE; personal → CONFIRMADA)")
    public CitaResponse solicitar(@Valid @RequestBody CitaRequest req, @AuthenticationPrincipal UsuarioAutenticado user) {
        return service.solicitar(req, user);
    }

    @GetMapping
    @Operation(summary = "Lista citas (personal: todas, con filtros ?estado= ?fecha= ?mascotaId=; dueño: solo las suyas)")
    public List<CitaResponse> listar(@AuthenticationPrincipal UsuarioAutenticado user,
                                     @RequestParam(required = false) EstadoCita estado,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                     @RequestParam(required = false) Long mascotaId) {
        return service.listar(user, estado, fecha, mascotaId);
    }

    @GetMapping("/disponibilidad")
    @Operation(summary = "Cupos disponibles por bloque horario en una fecha (?fecha=2026-12-01)")
    public DisponibilidadResponse disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.disponibilidad(fecha);
    }

    @GetMapping("/reporte")
    @Operation(summary = "Resumen de citas por estado y tasa de inasistencia (Administrador y Recepcionista)")
    public ReporteCitasResponse reporte(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.reporte(desde, hasta);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle y estado de una cita")
    public CitaResponse obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado user) {
        return service.obtener(id, user);
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirma una cita PENDIENTE (Recepcionista / Administrador)")
    public CitaResponse confirmar(@PathVariable Long id) {
        return service.confirmar(id);
    }

    @PatchMapping("/{id}/proponer-horario")
    @Operation(summary = "Propone otro horario → HORARIO_PROPUESTO (Recepcionista / Administrador)")
    public CitaResponse proponerHorario(@PathVariable Long id, @Valid @RequestBody ProponerHorarioRequest req) {
        return service.proponerHorario(id, req);
    }

    @PatchMapping("/{id}/aceptar-horario")
    @Operation(summary = "El dueño acepta el horario propuesto → CONFIRMADA")
    public CitaResponse aceptarHorario(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado user) {
        return service.aceptarHorario(id, user);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancela una cita (el dueño solo las suyas)")
    public CitaResponse cancelar(@PathVariable Long id, @RequestBody(required = false) @Valid CancelarRequest req,
                                 @AuthenticationPrincipal UsuarioAutenticado user) {
        return service.cancelar(id, req, user);
    }

    @PatchMapping("/{id}/atender")
    @Operation(summary = "Marca la cita como ATENDIDA")
    public CitaResponse atender(@PathVariable Long id) {
        return service.atender(id);
    }

    @PatchMapping("/{id}/no-asistio")
    @Operation(summary = "Marca la cita como NO_ASISTIO (solo después de la hora de la cita)")
    public CitaResponse noAsistio(@PathVariable Long id) {
        return service.marcarNoAsistio(id);
    }
}
