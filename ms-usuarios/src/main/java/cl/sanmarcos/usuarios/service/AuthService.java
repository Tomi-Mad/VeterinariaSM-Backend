package cl.sanmarcos.usuarios.service;

import cl.sanmarcos.usuarios.dto.*;
import cl.sanmarcos.usuarios.exception.ApiException;
import cl.sanmarcos.usuarios.model.Rol;
import cl.sanmarcos.usuarios.model.Usuario;
import cl.sanmarcos.usuarios.repository.UsuarioRepository;
import cl.sanmarcos.usuarios.security.JwtService;
import cl.sanmarcos.usuarios.util.RutUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository repo, PasswordEncoder encoder, JwtService jwt) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Registro público: el rol NO lo elige el cliente, siempre es DUENO. */
    public AuthResponse registrar(RegistroRequest req) {
        String email = req.email().trim().toLowerCase();
        if (repo.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }
        String rut = RutUtil.normalizar(req.rut());
        if (!RutUtil.esValido(rut)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El RUT ingresado no es válido");
        }
        if (repo.existsByRut(rut)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese RUT");
        }
        Usuario u = new Usuario();
        u.setRut(rut);
        u.setNombre(req.nombre().trim());
        u.setApellido(req.apellido().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password()));
        u.setTelefono(req.telefono());
        u.setRol(Rol.DUENO);
        return respuesta(repo.save(u));
    }

    public AuthResponse login(LoginRequest req) {
        // Mismo mensaje para "no existe" y "clave incorrecta": no revela qué emails están registrados.
        Usuario u = repo.findByEmail(req.email().trim().toLowerCase())
                .filter(x -> encoder.matches(req.password(), x.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos"));
        if (!u.isActivo()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Usuario desactivado. Contacta al administrador");
        }
        return respuesta(u);
    }

    /** Renovación: con un refresh token válido se entrega un nuevo par de tokens. */
    public AuthResponse renovar(RefreshRequest req) {
        try {
            Claims c = jwt.leer(req.refreshToken());
            if (!jwt.esRefresh(c)) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "El token entregado no es un refresh token");
            }
            Usuario u = repo.findByEmail(c.getSubject())
                    .filter(Usuario::isActivo)
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no válido"));
            return respuesta(u);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token inválido o expirado");
        }
    }

    public UsuarioResponse perfil(String email) {
        return repo.findByEmail(email).map(UsuarioResponse::desde)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private AuthResponse respuesta(Usuario u) {
        return new AuthResponse(jwt.generarAccessToken(u), jwt.generarRefreshToken(u),
                "Bearer", jwt.segundosAccessToken(), UsuarioResponse.desde(u));
    }
}
