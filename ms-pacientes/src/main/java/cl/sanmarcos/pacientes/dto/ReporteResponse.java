package cl.sanmarcos.pacientes.dto;

import java.time.LocalDate;
import java.util.List;

public record ReporteResponse(
        LocalDate desde, LocalDate hasta, long totalPacientesNuevos, long totalAtenciones, List<Mes> porMes
) {
    public record Mes(String mes, long pacientesNuevos, long atenciones) {}
}
