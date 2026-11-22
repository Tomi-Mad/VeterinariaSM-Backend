package cl.sanmarcos.pacientes.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record VacunaRequest(
        @NotBlank(message = "El nombre de la vacuna es obligatorio") @Size(max = 100) String nombre,
        @NotNull(message = "La fecha de aplicación es obligatoria")
        @PastOrPresent(message = "La fecha de aplicación no puede ser futura") LocalDate fechaAplicacion,
        LocalDate fechaProximaDosis,
        @Size(max = 50) String dosis,
        @Size(max = 120) String veterinario,
        @Size(max = 50) String lote,
        @Size(max = 500) String observaciones
) {}
