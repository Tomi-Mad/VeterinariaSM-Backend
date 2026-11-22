package cl.sanmarcos.pacientes.dto;

import cl.sanmarcos.pacientes.model.Consulta;
import java.time.LocalDate;

public record ConsultaResponse(
        Long id, Long mascotaId, LocalDate fecha, String motivo, String diagnostico,
        String tratamiento, String medicamentos, String veterinario, LocalDate proximoControl
) {
    public static ConsultaResponse desde(Consulta c) {
        return new ConsultaResponse(c.getId(), c.getMascota().getId(), c.getFecha(), c.getMotivo(),
                c.getDiagnostico(), c.getTratamiento(), c.getMedicamentos(), c.getVeterinario(), c.getProximoControl());
    }
}
