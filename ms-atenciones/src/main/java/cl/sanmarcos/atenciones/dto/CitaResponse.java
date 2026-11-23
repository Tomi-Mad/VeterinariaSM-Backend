package cl.sanmarcos.citas.dto;

import cl.sanmarcos.citas.model.Cita;
import cl.sanmarcos.citas.model.EstadoCita;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CitaResponse(
        Long id, Long mascotaId, String mascotaNombre, Long duenoId,
        LocalDate fecha, @JsonFormat(pattern = "HH:mm") LocalTime hora,
        String motivo, EstadoCita estado,
        LocalDate fechaPropuesta, @JsonFormat(pattern = "HH:mm") LocalTime horaPropuesta,
        String mensaje, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion
) {
    public static CitaResponse desde(Cita c) {
        return new CitaResponse(c.getId(), c.getMascotaId(), c.getMascotaNombre(), c.getDuenoId(),
                c.getFecha(), c.getHora(), c.getMotivo(), c.getEstado(),
                c.getFechaPropuesta(), c.getHoraPropuesta(), c.getMensaje(),
                c.getFechaCreacion(), c.getFechaActualizacion());
    }
}
