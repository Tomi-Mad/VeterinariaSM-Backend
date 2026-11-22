package cl.sanmarcos.usuarios.security;

import cl.sanmarcos.usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

/** Genera y valida tokens JWT (access y refresh) firmados con HMAC-SHA256. */
@Service
public class JwtService {

    private static final String TIPO_ACCESS = "access";
    private static final String TIPO_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessMinutos;
    private final long refreshDias;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-expiration-minutes}") long accessMinutos,
                      @Value("${app.jwt.refresh-expiration-days}") long refreshDias) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessMinutos = accessMinutos;
        this.refreshDias = refreshDias;
    }

    public String generarAccessToken(Usuario u) {
        return construir(u, TIPO_ACCESS, Duration.ofMinutes(accessMinutos));
    }

    public String generarRefreshToken(Usuario u) {
        return construir(u, TIPO_REFRESH, Duration.ofDays(refreshDias));
    }

    public long segundosAccessToken() {
        return accessMinutos * 60;
    }

    private String construir(Usuario u, String tipo, Duration duracion) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(u.getEmail())
                .claim("uid", u.getId())
                .claim("rol", u.getRol().name())
                .claim("tipo", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracion.toMillis()))
                .signWith(key)
                .compact();
    }

    /** Devuelve los claims si la firma y la expiración son válidas; si no, lanza JwtException. */
    public Claims leer(String token) throws JwtException {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean esAccess(Claims c) { return TIPO_ACCESS.equals(c.get("tipo", String.class)); }
    public boolean esRefresh(Claims c) { return TIPO_REFRESH.equals(c.get("tipo", String.class)); }
}
