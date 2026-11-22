package cl.sanmarcos.pacientes.service;

import cl.sanmarcos.pacientes.dto.RecetaRequest;
import cl.sanmarcos.pacientes.dto.RecetaResponse;
import cl.sanmarcos.pacientes.exception.ApiException;
import cl.sanmarcos.pacientes.model.Mascota;
import cl.sanmarcos.pacientes.model.Receta;
import cl.sanmarcos.pacientes.repository.RecetaRepository;
import cl.sanmarcos.pacientes.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecetaService {

    private final RecetaRepository repo;
    private final MascotaService mascotaService;

    public RecetaService(RecetaRepository repo, MascotaService mascotaService) {
        this.repo = repo;
        this.mascotaService = mascotaService;
    }

    public List<RecetaResponse> listar(Long mascotaId, UsuarioAutenticado user) {
        mascotaService.cargarConAcceso(mascotaId, user);
        return repo.findByMascotaIdOrderByFechaDescIdDesc(mascotaId).stream().map(RecetaResponse::desde).toList();
    }

    @Transactional
    public RecetaResponse registrar(Long mascotaId, RecetaRequest req, UsuarioAutenticado user) {
        Mascota m = mascotaService.cargarConAcceso(mascotaId, user);
        if (!m.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se pueden registrar recetas en una mascota inactiva");
        }
        Receta r = new Receta();
        r.setMascota(m);
        aplicar(r, req);
        return RecetaResponse.desde(repo.save(r));
    }

    @Transactional
    public RecetaResponse actualizar(Long id, RecetaRequest req) {
        Receta r = buscar(id);
        aplicar(r, req);
        return RecetaResponse.desde(repo.save(r));
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(buscar(id));
    }

    private void aplicar(Receta r, RecetaRequest req) {
        r.setFecha(req.fecha() != null ? req.fecha() : LocalDate.now());
        r.setMedicamento(req.medicamento().trim());
        r.setDosis(req.dosis());
        r.setFrecuencia(req.frecuencia());
        r.setDuracion(req.duracion());
        r.setIndicaciones(req.indicaciones());
        r.setVeterinario(req.veterinario());
    }

    private Receta buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Receta no encontrada con id " + id));
    }
}
