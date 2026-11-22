package cl.sanmarcos.pacientes.dto;

import cl.sanmarcos.pacientes.model.Vacuna;
import java.time.LocalDate;

public record VacunaResponse(
        Long id, Long mascotaId, String mascotaNombre, String nombre, LocalDate fechaAplicacion,
        LocalDate fechaProximaDosis, String dosis, String veterinario, String lote, String observaciones
) {
    public static VacunaResponse desde(Vacuna v) {
        return new VacunaResponse(v.getId(), v.getMascota().getId(), v.getMascota().getNombre(), v.getNombre(),
                v.getFechaAplicacion(), v.getFechaProximaDosis(), v.getDosis(), v.getVeterinario(), v.getLote(), v.getObservaciones());
    }
}
