package cl.sanmarcos.citas.config;

import cl.sanmarcos.citas.dto.ErrorResponse;
import cl.sanmarcos.citas.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;
    private final String[] origenes;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, ObjectMapper objectMapper,
                          @Value("${app.cors.allowed-origins}") String origenes) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.objectMapper = objectMapper;
        this.origenes = origenes.split(",");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // Reporte de citas: Administrador y Recepcionista
                .requestMatchers(HttpMethod.GET, "/api/citas/reporte").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                // Gestionar solicitudes: confirmar, proponer horario, marcar inasistencia
                .requestMatchers(HttpMethod.PATCH, "/api/citas/*/confirmar", "/api/citas/*/proponer-horario",
                        "/api/citas/*/no-asistio").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                // Marcar como atendida: también el veterinario
                .requestMatchers(HttpMethod.PATCH, "/api/citas/*/atender")
                        .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "VETERINARIO")
                // Solicitar, ver, aceptar horario y cancelar: cualquier usuario autenticado
                // (el service verifica que el dueño solo toque sus propias citas)
                .requestMatchers("/api/citas/**", "/api/citas").authenticated()
                .anyRequest().denyAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> escribir(res, 401, "Unauthorized",
                        "No autenticado: falta el token o es inválido/expirado"))
                .accessDeniedHandler((req, res, ex) -> escribir(res, 403, "Forbidden",
                        "No tienes permisos para acceder a este recurso")))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void escribir(HttpServletResponse res, int status, String error, String msg) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(res.getOutputStream(), ErrorResponse.de(status, error, msg, List.of()));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(Arrays.stream(origenes).map(String::trim).toList());
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
