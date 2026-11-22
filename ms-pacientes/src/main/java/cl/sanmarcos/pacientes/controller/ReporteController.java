package cl.sanmarcos.pacientes.controller;

import cl.sanmarcos.pacientes.dto.ReporteResponse;
import cl.sanmarcos.pacientes.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Estadísticas por período (Administrador y Recepcionista)")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Pacientes nuevos y atenciones por mes. Fechas ISO: ?desde=2026-06-01&hasta=2026-11-30")
    public ReporteResponse resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.resumen(desde, hasta);
    }
}
