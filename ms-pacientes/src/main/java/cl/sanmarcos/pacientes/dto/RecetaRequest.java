package cl.sanmarcos.pacientes.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Si no se envía fecha, se usa la de hoy. */
public record RecetaRequest(
        @PastOrPresent(message = "La fecha de la receta no puede ser futura") LocalDate fecha,
        @NotBlank(message = "El medicamento es obligatorio") @Size(max = 150) String medicamento,
        @Size(max = 100) String dosis,
        @Size(max = 100) String frecuencia,
        @Size(max = 100) String duracion,
        @Size(max = 1000) String indicaciones,
        @Size(max = 120) String veterinario
) {}
