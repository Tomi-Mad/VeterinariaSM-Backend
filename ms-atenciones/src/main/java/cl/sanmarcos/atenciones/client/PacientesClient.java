package cl.sanmarcos.citas.client;

import cl.sanmarcos.citas.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Comunicación entre microservicios: ms-citas le pregunta a ms-pacientes por la mascota,
 * reenviando el mismo JWT del usuario. Así ms-pacientes aplica su regla de privacidad
 * (un dueño solo puede ver mascotas propias) y ms-citas no necesita acceder a su base de datos.
 */
@Component
public class PacientesClient {

    public record MascotaInfo(Long id, String nombre, Long duenoId, boolean activo) {}

    private final RestClient rest;

    public PacientesClient(@Value("${app.pacientes.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.rest = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public MascotaInfo obtenerMascota(Long mascotaId, String token) {
        try {
            MascotaInfo info = rest.get()
                    .uri("/api/mascotas/{id}", mascotaId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(MascotaInfo.class);
            if (info == null) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Respuesta vacía del servicio de pacientes");
            }
            return info;
        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (status == 404) throw new ApiException(HttpStatus.NOT_FOUND, "La mascota indicada no existe");
            if (status == 403) throw new ApiException(HttpStatus.FORBIDDEN, "No tienes acceso a esta mascota");
            if (status == 401) throw new ApiException(HttpStatus.UNAUTHORIZED, "Sesión inválida o expirada");
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Error al consultar el servicio de pacientes");
        } catch (ResourceAccessException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio de pacientes no está disponible");
        }
    }
}
