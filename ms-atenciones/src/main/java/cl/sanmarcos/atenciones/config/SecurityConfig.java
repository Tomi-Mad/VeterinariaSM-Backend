package cl.sanmarcos.atenciones.config;

import cl.sanmarcos.atenciones.dto.ErrorResponse;
import cl.sanmarcos.atenciones.security.JwtAuthFilter;
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
                // Reporte general de atenciones: Administrador y Veterinario
                .requestMatchers(HttpMethod.GET, "/api/atenciones/reporte")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                // Crear, editar o eliminar fichas clínicas, vacunas, exámenes, diagnósticos o recetas: solo Veterinario y Administrador
                .requestMatchers(HttpMethod.POST, "/api/atenciones/**")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                .requestMatchers(HttpMethod.PUT, "/api/atenciones/**")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                .requestMatchers(HttpMethod.DELETE, "/api/atenciones/**")
                        .hasAnyRole("ADMINISTRADOR", "VETERINARIO")
                // Consultar historial de atenciones: cualquier usuario autenticado (el service valida dueño vs clínica)
                .requestMatchers(HttpMethod.GET, "/api/atenciones/**", "/api/atenciones")
                        .authenticated()
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
