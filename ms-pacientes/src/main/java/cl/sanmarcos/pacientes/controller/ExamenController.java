package cl.sanmarcos.pacientes.controller;

import cl.sanmarcos.pacientes.dto.ExamenRequest;
import cl.sanmarcos.pacientes.dto.ExamenResponse;
import cl.sanmarcos.pacientes.service.ExamenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/examenes")
@Tag(name = "Exámenes", description = "Edición y eliminación de exámenes")
public class ExamenController {

    private final ExamenService service;

    public ExamenController(ExamenService service) {
        this.service = service;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita un examen (Veterinario / Administrador)")
    public ExamenResponse actualizar(@PathVariable Long id, @Valid @RequestBody ExamenRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un examen (solo Administrador)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
