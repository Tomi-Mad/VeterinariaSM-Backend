package cl.sanmarcos.pacientes.controller;

import cl.sanmarcos.pacientes.dto.VacunaRequest;
import cl.sanmarcos.pacientes.dto.VacunaResponse;
import cl.sanmarcos.pacientes.security.UsuarioAutenticado;
import cl.sanmarcos.pacientes.service.VacunaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacunas")
@Tag(name = "Vacunas", description = "Vencimientos, edición y eliminación de vacunas")
public class VacunaController {

    private final VacunaService service;

    public VacunaController(VacunaService service) {
        this.service = service;
    }

    @GetMapping("/por-vencer")
    @Operation(summary = "Vacunas con próxima dosis dentro de ?dias=30 (dueño: solo las de sus mascotas)")
    public List<VacunaResponse> porVencer(@RequestParam(required = false) Integer dias,
                                          @AuthenticationPrincipal UsuarioAutenticado user) {
        return service.porVencer(dias, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita una vacuna (personal)")
    public VacunaResponse actualizar(@PathVariable Long id, @Valid @RequestBody VacunaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una vacuna (solo Administrador)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
