package cl.sanmarcos.citas.model;

import java.util.List;

/**
 * Ciclo de vida de una cita:
 *
 *  PENDIENTE --(recepción confirma)--------------> CONFIRMADA --> ATENDIDA | NO_ASISTIO
 *  PENDIENTE/CONFIRMADA --(recepción propone otro horario)--> HORARIO_PROPUESTO
 *  HORARIO_PROPUESTO --(dueño acepta)------------> CONFIRMADA
 *  PENDIENTE/CONFIRMADA/HORARIO_PROPUESTO --(cancelar)--> CANCELADA
 */
public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    HORARIO_PROPUESTO,
    CANCELADA,
    ATENDIDA,
    NO_ASISTIO;

    /** Estados que ocupan un cupo en la agenda. */
    public static final List<EstadoCita> OCUPAN_CUPO = List.of(PENDIENTE, CONFIRMADA);

    /** Estados en que la cita sigue "viva" y se puede cancelar. */
    public static final List<EstadoCita> ACTIVAS = List.of(PENDIENTE, CONFIRMADA, HORARIO_PROPUESTO);
}
