package cl.sanmarcos.pacientes.dto;

import cl.sanmarcos.pacientes.model.Receta;
import java.time.LocalDate;

public record RecetaResponse(
        Long id, Long mascotaId, LocalDate fecha, String medicamento, String dosis,
        String frecuencia, String duracion, String indicaciones, String veterinario
) {
    public static RecetaResponse desde(Receta r) {
        return new RecetaResponse(r.getId(), r.getMascota().getId(), r.getFecha(), r.getMedicamento(), r.getDosis(),
                r.getFrecuencia(), r.getDuracion(), r.getIndicaciones(), r.getVeterinario());
    }
}
