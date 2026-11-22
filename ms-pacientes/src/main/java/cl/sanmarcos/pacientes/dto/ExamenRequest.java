package cl.sanmarcos.pacientes.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Si no se envía fecha, se usa la de hoy. */
public record ExamenRequest(
        LocalDate fecha,
        @NotBlank(message = "El tipo de examen es obligatorio") @Size(max = 100) String tipoExamen,
        @Size(max = 255) String motivo,
        @Size(max = 2000) String resultados,
        @Size(max = 120) String veterinario,
        @Size(max = 1000) String observaciones
) {}
