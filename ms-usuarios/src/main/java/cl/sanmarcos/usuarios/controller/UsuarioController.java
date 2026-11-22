package cl.sanmarcos.usuarios.controller;

import cl.sanmarcos.usuarios.dto.*;
import cl.sanmarcos.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios (solo Administrador)", description = "CRUD de usuarios del sistema y asignación de roles")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos los usuarios")
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @GetMapping("/duenos")
    @Operation(summary = "Lista dueños activos (Administrador y Recepcionista). Filtro opcional ?q=")
    public List<UsuarioResponse> duenos(@RequestParam(required = false) String q) {
        return service.listarDuenos(q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario por id")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario con el rol indicado")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioCrearRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita datos y rol de un usuario")
    public UsuarioResponse editar(@PathVariable Long id, @Valid @RequestBody UsuarioEditarRequest req,
                                  Authentication auth) {
        return service.editar(id, req, auth.getName());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactiva un usuario (borrado lógico)")
    public UsuarioResponse desactivar(@PathVariable Long id, Authentication auth) {
        return service.desactivar(id, auth.getName());
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Reactiva un usuario desactivado")
    public UsuarioResponse activar(@PathVariable Long id) {
        return service.activar(id);
    }
}
