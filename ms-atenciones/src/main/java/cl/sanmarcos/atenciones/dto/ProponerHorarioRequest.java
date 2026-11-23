package cl.sanmarcos.citas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record ProponerHorarioRequest(
        @NotNull(message = "La fecha propuesta es obligatoria") LocalDate fecha,
        @NotNull(message = "La hora propuesta es obligatoria") @JsonFormat(pattern = "HH:mm") LocalTime hora,
        @Size(max = 500) String mensaje
) {}
