package cl.sanmarcos.atenciones.dto;

import cl.sanmarcos.atenciones.model.Atencion;
import cl.sanmarcos.atenciones.model.TipoAtencion;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AtencionResponse(
        Long id,
        Long mascotaId,
        String mascotaNombre,
        Long duenoId,
        Long veterinarioId,
        String veterinarioEmail,
        TipoAtencion tipo,
        LocalDate fecha,
        String motivo,
        String descripcion,
        String productoOExamen,
        String dosisOResultado,
        LocalDate proximaFecha,
        LocalDateTime creadoEn
) {
    public static AtencionResponse desde(Atencion a) {
        return new AtencionResponse(
                a.getId(),
                a.getMascotaId(),
                a.getMascotaNombre(),
                a.getDuenoId(),
                a.getVeterinarioId(),
                a.getVeterinarioEmail(),
                a.getTipo(),
                a.getFecha(),
                a.getMotivo(),
                a.getDescripcion(),
                a.getProductoOExamen(),
                a.getDosisOResultado(),
                a.getProximaFecha(),
                a.getCreadoEn()
        );
    }
}