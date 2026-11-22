package cl.sanmarcos.pacientes.controller;

import cl.sanmarcos.pacientes.dto.RecetaRequest;
import cl.sanmarcos.pacientes.dto.RecetaResponse;
import cl.sanmarcos.pacientes.service.RecetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recetas")
@Tag(name = "Recetas", description = "Edición y eliminación de recetas")
public class RecetaController {

    private final RecetaService service;

    public RecetaController(RecetaService service) {
        this.service = service;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita una receta (Veterinario / Administrador)")
    public RecetaResponse actualizar(@PathVariable Long id, @Valid @RequestBody RecetaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una receta (solo Administrador)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
