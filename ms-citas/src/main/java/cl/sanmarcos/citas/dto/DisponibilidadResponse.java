package cl.sanmarcos.citas.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DisponibilidadResponse(LocalDate fecha, int cuposPorBloque, List<Bloque> bloques) {
    public record Bloque(@JsonFormat(pattern = "HH:mm") LocalTime hora, int ocupados, int disponibles) {}
}
