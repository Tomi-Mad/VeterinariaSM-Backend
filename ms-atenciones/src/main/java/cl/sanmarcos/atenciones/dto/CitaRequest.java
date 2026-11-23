package cl.sanmarcos.citas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRequest(
        @NotNull(message = "La mascota es obligatoria") Long mascotaId,
        @NotNull(message = "La fecha es obligatoria") LocalDate fecha,
        @NotNull(message = "La hora es obligatoria") @JsonFormat(pattern = "HH:mm") LocalTime hora,
        @NotBlank(message = "El motivo es obligatorio") @Size(max = 255) String motivo
) {}
