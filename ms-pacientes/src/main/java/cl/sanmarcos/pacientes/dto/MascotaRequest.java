package cl.sanmarcos.pacientes.dto;

import cl.sanmarcos.pacientes.model.Especie;
import cl.sanmarcos.pacientes.model.Sexo;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** duenoId: obligatorio si lo envía el personal; si lo envía un DUENO se ignora (se usa su propio id). */
public record MascotaRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
        @NotNull(message = "La especie es obligatoria") Especie especie,
        @Size(max = 80) String raza,
        @NotNull(message = "El sexo es obligatorio") Sexo sexo,
        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura") LocalDate fechaNacimiento,
        @Positive(message = "El peso debe ser mayor a 0") @DecimalMax(value = "999.99", message = "Peso fuera de rango") BigDecimal pesoKg,
        Long duenoId
) {}
