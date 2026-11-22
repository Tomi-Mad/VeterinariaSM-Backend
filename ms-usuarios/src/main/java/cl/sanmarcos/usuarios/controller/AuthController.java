package cl.sanmarcos.usuarios.controller;

import cl.sanmarcos.usuarios.dto.*;
import cl.sanmarcos.usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, login, renovación de token y perfil")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registro público de un dueño de mascota (rol DUENO)")
    public AuthResponse registro(@Valid @RequestBody RegistroRequest req) {
        return service.registrar(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y entrega access token + refresh token")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renueva los tokens usando un refresh token válido")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return service.renovar(req);
    }

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario autenticado (útil para restaurar sesión al recargar la página)")
    public UsuarioResponse me(Authentication auth) {
        return service.perfil(auth.getName());
    }
}
