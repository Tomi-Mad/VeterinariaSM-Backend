package cl.sanmarcos.pacientes.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Si no se envía fecha, se usa la de hoy. */
public record ConsultaRequest(
        @PastOrPresent(message = "La fecha de la consulta no puede ser futura") LocalDate fecha,
        @Size(max = 255) String motivo,
        @Size(max = 1000) String diagnostico,
        @Size(max = 1000) String tratamiento,
        @Size(max = 1000) String medicamentos,
        @Size(max = 120) String veterinario,
        LocalDate proximoControl
) {}
