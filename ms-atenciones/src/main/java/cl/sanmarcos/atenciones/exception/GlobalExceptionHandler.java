package cl.sanmarcos.atenciones.exception;

import cl.sanmarcos.atenciones.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        HttpStatus s = ex.getStatus();
        return ResponseEntity.status(s).body(ErrorResponse.de(s.value(), s.getReasonPhrase(), ex.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        return ResponseEntity.badRequest()
                .body(ErrorResponse.de(400, "Bad Request", "Datos inválidos", detalles));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.de(400, "Bad Request", "El cuerpo de la petición no es un JSON válido", List.of()));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleParametros(Exception ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.de(400, "Bad Request", "Parámetro inválido o faltante en la petición", List.of()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodo(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(405)
                .body(ErrorResponse.de(405, "Method Not Allowed", "Método HTTP no permitido para esta ruta", List.of()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(NoResourceFoundException ex) {
        return ResponseEntity.status(404)
                .body(ErrorResponse.de(404, "Not Found", "Ruta no encontrada", List.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridad(DataIntegrityViolationException ex) {
        return ResponseEntity.status(409)
                .body(ErrorResponse.de(409, "Conflict", "Los datos entran en conflicto con un registro existente", List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(500)
                .body(ErrorResponse.de(500, "Internal Server Error", "Error interno del servidor", List.of()));
    }
}
