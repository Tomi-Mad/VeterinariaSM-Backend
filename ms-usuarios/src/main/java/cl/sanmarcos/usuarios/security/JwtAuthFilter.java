package cl.sanmarcos.usuarios.security;

import cl.sanmarcos.usuarios.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Se ejecuta en cada petición: lee "Authorization: Bearer <token>", valida el JWT
 * y deja al usuario autenticado en el contexto de seguridad.
 * Si el token falta o es inválido, simplemente no autentica (y Spring responde 401).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.leer(header.substring(7));
                if (jwtService.esAccess(claims) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Se consulta la BD para que un usuario desactivado o con rol cambiado
                    // pierda acceso de inmediato, aunque su token siga vigente.
                    usuarioRepository.findByEmail(claims.getSubject())
                            .filter(u -> u.isActivo())
                            .ifPresent(u -> {
                                var auth = new UsernamePasswordAuthenticationToken(
                                        u.getEmail(), null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            });
                }
            } catch (JwtException | IllegalArgumentException ignorado) {
                // token inválido o expirado: se deja sin autenticar
            }
        }
        chain.doFilter(request, response);
    }
}
