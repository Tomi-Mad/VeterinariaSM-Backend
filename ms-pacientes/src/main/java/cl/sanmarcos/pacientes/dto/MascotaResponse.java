package cl.sanmarcos.pacientes.dto;

import cl.sanmarcos.pacientes.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MascotaResponse(
        Long id, String nombre, Especie especie, String raza, Sexo sexo,
        LocalDate fechaNacimiento, BigDecimal pesoKg, Long duenoId, boolean activo, LocalDateTime fechaRegistro
) {
    public static MascotaResponse desde(Mascota m) {
        return new MascotaResponse(m.getId(), m.getNombre(), m.getEspecie(), m.getRaza(), m.getSexo(),
                m.getFechaNacimiento(), m.getPesoKg(), m.getDuenoId(), m.isActivo(), m.getFechaRegistro());
    }
}
