package cl.sanmarcos.atenciones.dto;

import cl.sanmarcos.atenciones.model.TipoAtencion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AtencionRequest(
        @NotNull(message = "El ID de la mascota es obligatorio")
        Long mascotaId,

        @NotNull(message = "El tipo de atención es obligatorio")
        TipoAtencion tipo,

        LocalDate fecha,

        @NotBlank(message = "El motivo de la atención es obligatorio")
        @Size(max = 250, message = "El motivo no puede superar los 250 caracteres")
        String motivo,

        String descripcion,

        @Size(max = 150, message = "El producto o examen no puede superar los 150 caracteres")
        String productoOExamen,

        @Size(max = 150, message = "La dosis o resultado no puede superar los 150 caracteres")
        String dosisOResultado,

        LocalDate proximaFecha
) {}