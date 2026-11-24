package cl.sanmarcos.atenciones.dto;

import java.time.LocalDate;
import java.util.Map;

public record ReporteAtencionesResponse(
        LocalDate desde,
        LocalDate hasta,
        long totalAtenciones,
        Map<String, Long> porTipo
) {}