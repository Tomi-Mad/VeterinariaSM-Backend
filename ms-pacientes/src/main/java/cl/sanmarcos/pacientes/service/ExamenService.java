package cl.sanmarcos.pacientes.service;

import cl.sanmarcos.pacientes.dto.ExamenRequest;
import cl.sanmarcos.pacientes.dto.ExamenResponse;
import cl.sanmarcos.pacientes.exception.ApiException;
import cl.sanmarcos.pacientes.model.Examen;
import cl.sanmarcos.pacientes.model.Mascota;
import cl.sanmarcos.pacientes.repository.ExamenRepository;
import cl.sanmarcos.pacientes.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExamenService {

    private final ExamenRepository repo;
    private final MascotaService mascotaService;

    public ExamenService(ExamenRepository repo, MascotaService mascotaService) {
        this.repo = repo;
        this.mascotaService = mascotaService;
    }

    public List<ExamenResponse> listar(Long mascotaId, UsuarioAutenticado user) {
        mascotaService.cargarConAcceso(mascotaId, user);
        return repo.findByMascotaIdOrderByFechaDescIdDesc(mascotaId).stream().map(ExamenResponse::desde).toList();
    }

    @Transactional
    public ExamenResponse registrar(Long mascotaId, ExamenRequest req, UsuarioAutenticado user) {
        Mascota m = mascotaService.cargarConAcceso(mascotaId, user);
        if (!m.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se pueden registrar exámenes en una mascota inactiva");
        }
        Examen e = new Examen();
        e.setMascota(m);
        aplicar(e, req);
        return ExamenResponse.desde(repo.save(e));
    }

    @Transactional
    public ExamenResponse actualizar(Long id, ExamenRequest req) {
        Examen e = buscar(id);
        aplicar(e, req);
        return ExamenResponse.desde(repo.save(e));
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(buscar(id));
    }

    private void aplicar(Examen e, ExamenRequest req) {
        e.setFecha(req.fecha() != null ? req.fecha() : LocalDate.now());
        e.setTipoExamen(req.tipoExamen().trim());
        e.setMotivo(req.motivo());
        e.setResultados(req.resultados());
        e.setVeterinario(req.veterinario());
        e.setObservaciones(req.observaciones());
    }

    private Examen buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Examen no encontrado con id " + id));
    }
}
