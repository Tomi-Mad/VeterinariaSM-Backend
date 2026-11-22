package cl.sanmarcos.citas.service;

import cl.sanmarcos.citas.client.PacientesClient;
import cl.sanmarcos.citas.config.HorarioClinica;
import cl.sanmarcos.citas.dto.*;
import cl.sanmarcos.citas.exception.ApiException;
import cl.sanmarcos.citas.model.Cita;
import cl.sanmarcos.citas.model.EstadoCita;
import cl.sanmarcos.citas.repository.CitaRepository;
import cl.sanmarcos.citas.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Flujo de solicitud de citas (el "pedido" del proyecto):
 * el dueño solicita -> recepción confirma o propone otro horario -> el dueño ve el estado y acepta o cancela.
 */
@Service
@Transactional(readOnly = true)
public class CitaService {

    private final CitaRepository repo;
    private final PacientesClient pacientes;
    private final HorarioClinica horario;

    public CitaService(CitaRepository repo, PacientesClient pacientes, HorarioClinica horario) {
        this.repo = repo;
        this.pacientes = pacientes;
        this.horario = horario;
    }

    // ------------------------------------------------------------------ consultas

    public List<CitaResponse> listar(UsuarioAutenticado user, EstadoCita estado, LocalDate fecha, Long mascotaId) {
        List<Cita> base = user.esDueno()
                ? repo.findByDuenoIdOrderByFechaDescHoraDesc(user.id())
                : repo.findAllByOrderByFechaAscHoraAsc();
        return base.stream()
                .filter(c -> estado == null || c.getEstado() == estado)
                .filter(c -> fecha == null || c.getFecha().equals(fecha))
                .filter(c -> mascotaId == null || c.getMascotaId().equals(mascotaId))
                .map(CitaResponse::desde)
                .toList();
    }

    public CitaResponse obtener(Long id, UsuarioAutenticado user) {
        return CitaResponse.desde(cargarConAcceso(id, user));
    }

    /** Cupos libres por bloque para un día (para que el frontend muestre los horarios disponibles). */
    public DisponibilidadResponse disponibilidad(LocalDate fecha) {
        if (fecha.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La clínica no atiende los domingos");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha no puede ser pasada");
        }
        Map<LocalTime, Long> ocupadosPorHora = repo.findByFechaAndEstadoIn(fecha, EstadoCita.OCUPAN_CUPO).stream()
                .collect(Collectors.groupingBy(Cita::getHora, Collectors.counting()));
        LocalDateTime ahora = LocalDateTime.now();
        List<DisponibilidadResponse.Bloque> bloques = new ArrayList<>();
        for (LocalTime h : horario.bloquesDelDia()) {
            if (!LocalDateTime.of(fecha, h).isAfter(ahora)) continue;      // bloques que ya pasaron hoy
            int ocupados = ocupadosPorHora.getOrDefault(h, 0L).intValue();
            bloques.add(new DisponibilidadResponse.Bloque(h, ocupados, Math.max(0, horario.capacidad() - ocupados)));
        }
        return new DisponibilidadResponse(fecha, horario.capacidad(), bloques);
    }

    // ------------------------------------------------------------------ solicitud

    /**
     * Dueño: la cita queda PENDIENTE hasta que recepción la confirme.
     * Personal (ej. recepción atendiendo por teléfono): queda CONFIRMADA directamente.
     */
    @Transactional
    public CitaResponse solicitar(CitaRequest req, UsuarioAutenticado user) {
        horario.validarBloque(req.fecha(), req.hora());

        // Comunicación con ms-pacientes: verifica que la mascota exista y que el usuario tenga acceso.
        PacientesClient.MascotaInfo mascota = pacientes.obtenerMascota(req.mascotaId(), user.token());
        if (!mascota.activo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La mascota está inactiva");
        }
        if (user.esDueno() && !mascota.duenoId().equals(user.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes solicitar citas para mascotas de otro dueño");
        }
        validarSinDuplicadoEseDia(mascota.id(), req.fecha(), -1L);
        validarCupo(req.fecha(), req.hora());

        Cita c = new Cita();
        c.setMascotaId(mascota.id());
        c.setMascotaNombre(mascota.nombre());
        c.setDuenoId(mascota.duenoId());
        c.setFecha(req.fecha());
        c.setHora(req.hora());
        c.setMotivo(req.motivo().trim());
        c.setEstado(user.esDueno() ? EstadoCita.PENDIENTE : EstadoCita.CONFIRMADA);
        c.setCreadaPorId(user.id());
        return CitaResponse.desde(repo.save(c));
    }

    // ------------------------------------------------------------------ acciones de recepción

    @Transactional
    public CitaResponse confirmar(Long id) {
        Cita c = buscar(id);
        exigirEstado(c, EstadoCita.PENDIENTE);
        exigirFutura(c);
        c.setEstado(EstadoCita.CONFIRMADA);
        c.setMensaje(null);
        return CitaResponse.desde(repo.save(c));
    }

    /** Recepción no puede dar la hora pedida: propone otra. El dueño debe aceptarla o cancelar. */
    @Transactional
    public CitaResponse proponerHorario(Long id, ProponerHorarioRequest req) {
        Cita c = buscar(id);
        if (c.getEstado() != EstadoCita.PENDIENTE && c.getEstado() != EstadoCita.CONFIRMADA) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Solo se puede proponer otro horario a citas PENDIENTES o CONFIRMADAS (estado actual: " + c.getEstado() + ")");
        }
        horario.validarBloque(req.fecha(), req.hora());
        if (req.fecha().equals(c.getFecha()) && req.hora().equals(c.getHora())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El horario propuesto es el mismo que el actual");
        }
        validarSinDuplicadoEseDia(c.getMascotaId(), req.fecha(), c.getId());
        validarCupo(req.fecha(), req.hora());

        c.setFechaPropuesta(req.fecha());
        c.setHoraPropuesta(req.hora());
        c.setMensaje(req.mensaje());
        c.setEstado(EstadoCita.HORARIO_PROPUESTO);   // libera el cupo original
        return CitaResponse.desde(repo.save(c));
    }

    @Transactional
    public CitaResponse atender(Long id) {
        Cita c = buscar(id);
        exigirEstado(c, EstadoCita.CONFIRMADA);
        c.setEstado(EstadoCita.ATENDIDA);
        return CitaResponse.desde(repo.save(c));
    }

    @Transactional
    public CitaResponse marcarNoAsistio(Long id) {
        Cita c = buscar(id);
        exigirEstado(c, EstadoCita.CONFIRMADA);
        if (c.inicio().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se puede marcar inasistencia antes de la hora de la cita");
        }
        c.setEstado(EstadoCita.NO_ASISTIO);
        return CitaResponse.desde(repo.save(c));
    }

    // ------------------------------------------------------------------ acciones del dueño (o personal)

    @Transactional
    public CitaResponse aceptarHorario(Long id, UsuarioAutenticado user) {
        Cita c = cargarConAcceso(id, user);
        exigirEstado(c, EstadoCita.HORARIO_PROPUESTO);
        // Se revalida: pudo pasar el tiempo o llenarse el cupo desde que se propuso.
        horario.validarBloque(c.getFechaPropuesta(), c.getHoraPropuesta());
        validarSinDuplicadoEseDia(c.getMascotaId(), c.getFechaPropuesta(), c.getId());
        validarCupo(c.getFechaPropuesta(), c.getHoraPropuesta());

        c.setFecha(c.getFechaPropuesta());
        c.setHora(c.getHoraPropuesta());
        c.setFechaPropuesta(null);
        c.setHoraPropuesta(null);
        c.setEstado(EstadoCita.CONFIRMADA);
        return CitaResponse.desde(repo.save(c));
    }

    @Transactional
    public CitaResponse cancelar(Long id, CancelarRequest req, UsuarioAutenticado user) {
        Cita c = cargarConAcceso(id, user);
        if (!EstadoCita.ACTIVAS.contains(c.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "La cita ya está " + c.getEstado() + " y no se puede cancelar");
        }
        if (user.esDueno() && !c.inicio().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se puede cancelar una cita que ya comenzó o pasó");
        }
        c.setEstado(EstadoCita.CANCELADA);
        c.setFechaPropuesta(null);
        c.setHoraPropuesta(null);
        if (req != null && req.motivo() != null && !req.motivo().isBlank()) {
            c.setMensaje(req.motivo().trim());
        }
        return CitaResponse.desde(repo.save(c));
    }

    // ------------------------------------------------------------------ reporte

    public ReporteCitasResponse reporte(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate ini = desde != null ? desde : fin.minusDays(30);
        if (ini.isAfter(fin)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "'desde' no puede ser posterior a 'hasta'");
        }
        List<Cita> citas = repo.findByFechaBetween(ini, fin);
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoCita e : EstadoCita.values()) {
            porEstado.put(e.name(), citas.stream().filter(c -> c.getEstado() == e).count());
        }
        long atendidas = porEstado.get(EstadoCita.ATENDIDA.name());
        long noAsistio = porEstado.get(EstadoCita.NO_ASISTIO.name());
        double tasa = (atendidas + noAsistio) == 0 ? 0.0
                : Math.round(noAsistio * 1000.0 / (atendidas + noAsistio)) / 10.0;
        return new ReporteCitasResponse(ini, fin, citas.size(), porEstado, tasa);
    }

    // ------------------------------------------------------------------ helpers

    private Cita buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cita no encontrada con id " + id));
    }

    /** Regla de privacidad: un dueño solo accede a sus propias citas. */
    private Cita cargarConAcceso(Long id, UsuarioAutenticado user) {
        Cita c = buscar(id);
        if (user.esDueno() && !c.getDuenoId().equals(user.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes acceso a esta cita");
        }
        return c;
    }

    private void exigirEstado(Cita c, EstadoCita esperado) {
        if (c.getEstado() != esperado) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "La cita debe estar " + esperado + " para esta acción (estado actual: " + c.getEstado() + ")");
        }
    }

    private void exigirFutura(Cita c) {
        if (!c.inicio().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La hora de la cita ya pasó");
        }
    }

    private void validarCupo(LocalDate fecha, LocalTime hora) {
        long ocupados = repo.countByFechaAndHoraAndEstadoIn(fecha, hora, EstadoCita.OCUPAN_CUPO);
        if (ocupados >= horario.capacidad()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ese horario ya no tiene cupos disponibles");
        }
    }

    private void validarSinDuplicadoEseDia(Long mascotaId, LocalDate fecha, Long idExcluido) {
        if (repo.existsByMascotaIdAndFechaAndEstadoInAndIdNot(mascotaId, fecha, EstadoCita.ACTIVAS, idExcluido)) {
            throw new ApiException(HttpStatus.CONFLICT, "La mascota ya tiene una cita activa ese día");
        }
    }
}
