package cl.sanmarcos.pacientes.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Formato único de error (el documento exige JSON también en los errores). */
public record ErrorResponse(String timestamp, int status, String error, String mensaje, List<String> detalles) {
    public static ErrorResponse de(int status, String error, String mensaje, List<String> detalles) {
        return new ErrorResponse(LocalDateTime.now().toString(), status, error, mensaje, detalles);
    }
}
