package cl.sanmarcos.atenciones.service;

import cl.sanmarcos.atenciones.client.PacientesClient;
import cl.sanmarcos.atenciones.dto.AtencionRequest;
import cl.sanmarcos.atenciones.dto.AtencionResponse;
import cl.sanmarcos.atenciones.dto.ReporteAtencionesResponse;
import cl.sanmarcos.atenciones.exception.ApiException;
import cl.sanmarcos.atenciones.model.Atencion;
import cl.sanmarcos.atenciones.model.TipoAtencion;
import cl.sanmarcos.atenciones.repository.AtencionRepository;
import cl.sanmarcos.atenciones.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AtencionService {

    private final AtencionRepository repo;
    private final PacientesClient pacientes;

    public AtencionService(AtencionRepository repo, PacientesClient pacientes) {
        this.repo = repo;
        this.pacientes = pacientes;
    }

    // ------------------------------------------------------------------ consultas

    public List<AtencionResponse> listar(UsuarioAutenticado user, TipoAtencion tipo, Long mascotaId, LocalDate fecha) {
        List<Atencion> base = user.esDueno()
                ? repo.findByDuenoIdOrderByFechaDescCreadoEnDesc(user.id())
                : repo.findAllByOrderByFechaDescCreadoEnDesc();

        return base.stream()
                .filter(a -> tipo == null || a.getTipo() == tipo)
                .filter(a -> mascotaId == null || a.getMascotaId().equals(mascotaId))
                .filter(a -> fecha == null || a.getFecha().equals(fecha))
                .map(AtencionResponse::desde)
                .toList();
    }

    public AtencionResponse obtener(Long id, UsuarioAutenticado user) {
        return AtencionResponse.desde(cargarConAcceso(id, user));
    }

    // ------------------------------------------------------------------ escritura (Veterinario / Admin)

    @Transactional
    public AtencionResponse crear(AtencionRequest req, UsuarioAutenticado user) {
        PacientesClient.MascotaInfo mascota = pacientes.obtenerMascota(req.mascotaId(), user.token());
        if (!mascota.activo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se puede registrar atención para una mascota inactiva");
        }

        Atencion a = new Atencion();
        a.setMascotaId(mascota.id());
        a.setMascotaNombre(mascota.nombre());
        a.setDuenoId(mascota.duenoId());
        a.setVeterinarioId(user.id());
        a.setVeterinarioEmail(user.email());
        mapearCampos(a, req);

        return AtencionResponse.desde(repo.save(a));
    }

    @Transactional
    public AtencionResponse actualizar(Long id, AtencionRequest req, UsuarioAutenticado user) {
        Atencion a = buscar(id);

        // Si cambiaron el ID de la mascota, se revalida en ms-pacientes
        if (!a.getMascotaId().equals(req.mascotaId())) {
            PacientesClient.MascotaInfo mascota = pacientes.obtenerMascota(req.mascotaId(), user.token());
            if (!mascota.activo()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "La mascota indicada está inactiva");
            }
            a.setMascotaId(mascota.id());
            a.setMascotaNombre(mascota.nombre());
            a.setDuenoId(mascota.duenoId());
        }

        mapearCampos(a, req);
        return AtencionResponse.desde(repo.save(a));
    }

    @Transactional
    public void eliminar(Long id) {
        Atencion a = buscar(id);
        repo.delete(a);
    }

    // ------------------------------------------------------------------ reporte

    public ReporteAtencionesResponse reporte(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate ini = desde != null ? desde : fin.minusDays(30);
        if (ini.isAfter(fin)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "'desde' no puede ser posterior a 'hasta'");
        }

        List<Atencion> atenciones = repo.findByFechaBetween(ini, fin);
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (TipoAtencion t : TipoAtencion.values()) {
            porTipo.put(t.name(), atenciones.stream().filter(a -> a.getTipo() == t).count());
        }

        return new ReporteAtencionesResponse(ini, fin, atenciones.size(), porTipo);
    }

    // ------------------------------------------------------------------ helpers

    private void mapearCampos(Atencion a, AtencionRequest req) {
        a.setTipo(req.tipo());
        a.setFecha(req.fecha() != null ? req.fecha() : LocalDate.now());
        a.setMotivo(req.motivo().trim());
        a.setDescripcion(req.descripcion() != null ? req.descripcion().trim() : null);
        a.setProductoOExamen(req.productoOExamen() != null ? req.productoOExamen().trim() : null);
        a.setDosisOResultado(req.dosisOResultado() != null ? req.dosisOResultado().trim() : null);
        a.setProximaFecha(req.proximaFecha());
    }

    private Atencion buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Atención no encontrada con id " + id));
    }

    private Atencion cargarConAcceso(Long id, UsuarioAutenticado user) {
        Atencion a = buscar(id);
        if (user.esDueno() && !a.getDuenoId().equals(user.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes acceso a esta atención clínica");
        }
        return a;
    }
}
