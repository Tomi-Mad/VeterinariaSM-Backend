package cl.sanmarcos.citas.dto;

import java.time.LocalDate;
import java.util.Map;

public record ReporteCitasResponse(
        LocalDate desde, LocalDate hasta, long total,
        Map<String, Long> porEstado,
        /** NO_ASISTIO / (ATENDIDA + NO_ASISTIO) * 100. El caso de estudio parte de ~20 %. */
        double tasaInasistenciaPorcentaje
) {}
