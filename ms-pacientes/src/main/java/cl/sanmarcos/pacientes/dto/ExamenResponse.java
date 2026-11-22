package cl.sanmarcos.pacientes.dto;

import cl.sanmarcos.pacientes.model.Examen;
import java.time.LocalDate;

public record ExamenResponse(
        Long id, Long mascotaId, LocalDate fecha, String tipoExamen, String motivo,
        String resultados, String veterinario, String observaciones
) {
    public static ExamenResponse desde(Examen e) {
        return new ExamenResponse(e.getId(), e.getMascota().getId(), e.getFecha(), e.getTipoExamen(),
                e.getMotivo(), e.getResultados(), e.getVeterinario(), e.getObservaciones());
    }
}
