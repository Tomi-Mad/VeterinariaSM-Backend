package cl.sanmarcos.pacientes.service;

import cl.sanmarcos.pacientes.dto.VacunaRequest;
import cl.sanmarcos.pacientes.dto.VacunaResponse;
import cl.sanmarcos.pacientes.exception.ApiException;
import cl.sanmarcos.pacientes.model.Mascota;
import cl.sanmarcos.pacientes.model.Vacuna;
import cl.sanmarcos.pacientes.repository.VacunaRepository;
import cl.sanmarcos.pacientes.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Historial de vacunación digital y aviso de próximos vencimientos. */
@Service
@Transactional(readOnly = true)
public class VacunaService {

    private final VacunaRepository repo;
    private final MascotaService mascotaService;

    public VacunaService(VacunaRepository repo, MascotaService mascotaService) {
        this.repo = repo;
        this.mascotaService = mascotaService;
    }

    public List<VacunaResponse> listar(Long mascotaId, UsuarioAutenticado user) {
        mascotaService.cargarConAcceso(mascotaId, user);
        return repo.findByMascotaIdOrderByFechaAplicacionDescIdDesc(mascotaId).stream().map(VacunaResponse::desde).toList();
    }

    @Transactional
    public VacunaResponse registrar(Long mascotaId, VacunaRequest req, UsuarioAutenticado user) {
        Mascota m = mascotaService.cargarConAcceso(mascotaId, user);
        if (!m.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se pueden registrar vacunas en una mascota inactiva");
        }
        Vacuna v = new Vacuna();
        v.setMascota(m);
        aplicar(v, req);
        return VacunaResponse.desde(repo.save(v));
    }

    @Transactional
    public VacunaResponse actualizar(Long id, VacunaRequest req) {
        Vacuna v = buscar(id);
        aplicar(v, req);
        return VacunaResponse.desde(repo.save(v));
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(buscar(id));
    }

    /**
     * Vacunas cuya próxima dosis vence en los próximos N días (por defecto 30).
     * Personal: de todas las mascotas. Dueño: solo de las suyas.
     * Se omiten las que ya fueron renovadas (existe una aplicación posterior de la misma vacuna).
     */
    public List<VacunaResponse> porVencer(Integer dias, UsuarioAutenticado user) {
        int d = dias == null ? 30 : dias;
        if (d < 1 || d > 365) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El parámetro 'dias' debe estar entre 1 y 365");
        }
        LocalDate hoy = LocalDate.now();
        List<Vacuna> candidatas = user.esDueno()
                ? repo.porVencerDeDueno(user.id(), hoy, hoy.plusDays(d))
                : repo.porVencer(hoy, hoy.plusDays(d));
        return candidatas.stream()
                .filter(v -> !repo.existsByMascotaIdAndNombreIgnoreCaseAndFechaAplicacionAfter(
                        v.getMascota().getId(), v.getNombre(), v.getFechaAplicacion()))
                .map(VacunaResponse::desde)
                .toList();
    }

    private void aplicar(Vacuna v, VacunaRequest req) {
        if (req.fechaProximaDosis() != null && !req.fechaProximaDosis().isAfter(req.fechaAplicacion())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La próxima dosis debe ser posterior a la fecha de aplicación");
        }
        v.setNombre(req.nombre().trim());
        v.setFechaAplicacion(req.fechaAplicacion());
        v.setFechaProximaDosis(req.fechaProximaDosis());
        v.setDosis(req.dosis());
        v.setVeterinario(req.veterinario());
        v.setLote(req.lote());
        v.setObservaciones(req.observaciones());
    }

    private Vacuna buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vacuna no encontrada con id " + id));
    }
}
