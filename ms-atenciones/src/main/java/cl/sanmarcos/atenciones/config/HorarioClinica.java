package cl.sanmarcos.citas.config;

import cl.sanmarcos.citas.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Reglas de la agenda: horario de atención, bloques y anticipación máxima. */
@Component
public class HorarioClinica {

    private final LocalTime apertura;
    private final LocalTime cierre;
    private final int minutosBloque;
    private final int capacidad;
    private final int diasMaximo;

    public HorarioClinica(@Value("${app.citas.hora-apertura}") String apertura,
                          @Value("${app.citas.hora-cierre}") String cierre,
                          @Value("${app.citas.minutos-bloque}") int minutosBloque,
                          @Value("${app.citas.capacidad-por-bloque}") int capacidad,
                          @Value("${app.citas.dias-maximo-anticipacion}") int diasMaximo) {
        this.apertura = LocalTime.parse(apertura);
        this.cierre = LocalTime.parse(cierre);
        this.minutosBloque = minutosBloque;
        this.capacidad = capacidad;
        this.diasMaximo = diasMaximo;
    }

    public int capacidad() { return capacidad; }

    /** Lanza ApiException si la fecha/hora no es un bloque de atención válido y futuro. */
    public void validarBloque(LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes indicar fecha y hora");
        }
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La clínica no atiende los domingos");
        }
        if (!LocalDateTime.of(fecha, hora).isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La cita debe tener una fecha y hora futuras");
        }
        if (fecha.isAfter(LocalDate.now().plusDays(diasMaximo))) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Solo se puede agendar con un máximo de " + diasMaximo + " días de anticipación");
        }
        boolean dentroDeHorario = !hora.isBefore(apertura) && !hora.isAfter(cierre.minusMinutes(minutosBloque));
        boolean enBloque = ((hora.getHour() * 60 + hora.getMinute()) - (apertura.getHour() * 60 + apertura.getMinute()))
                % minutosBloque == 0 && hora.getSecond() == 0;
        if (!dentroDeHorario || !enBloque) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Horario inválido. Se atiende de " + apertura + " a " + cierre
                            + " en bloques de " + minutosBloque + " minutos");
        }
    }

    /** Todas las horas de inicio de bloque del día. */
    public List<LocalTime> bloquesDelDia() {
        List<LocalTime> bloques = new ArrayList<>();
        for (LocalTime h = apertura; !h.plusMinutes(minutosBloque).isAfter(cierre); h = h.plusMinutes(minutosBloque)) {
            bloques.add(h);
        }
        return bloques;
    }
}
