package cl.sanmarcos.pacientes.controller;

import cl.sanmarcos.pacientes.dto.ConsultaRequest;
import cl.sanmarcos.pacientes.dto.ConsultaResponse;
import cl.sanmarcos.pacientes.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultas")
@Tag(name = "Consultas", description = "Edición y eliminación de registros de la ficha clínica")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita una consulta (personal)")
    public ConsultaResponse actualizar(@PathVariable Long id, @Valid @RequestBody ConsultaRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una consulta (solo Administrador)")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
