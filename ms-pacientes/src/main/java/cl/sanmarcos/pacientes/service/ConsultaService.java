package cl.sanmarcos.pacientes.service;

import cl.sanmarcos.pacientes.dto.ConsultaRequest;
import cl.sanmarcos.pacientes.dto.ConsultaResponse;
import cl.sanmarcos.pacientes.exception.ApiException;
import cl.sanmarcos.pacientes.model.Consulta;
import cl.sanmarcos.pacientes.model.Mascota;
import cl.sanmarcos.pacientes.repository.ConsultaRepository;
import cl.sanmarcos.pacientes.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Ficha clínica: historial de consultas de una mascota. */
@Service
@Transactional(readOnly = true)
public class ConsultaService {

    private final ConsultaRepository repo;
    private final MascotaService mascotaService;

    public ConsultaService(ConsultaRepository repo, MascotaService mascotaService) {
        this.repo = repo;
        this.mascotaService = mascotaService;
    }

    public List<ConsultaResponse> historial(Long mascotaId, UsuarioAutenticado user) {
        mascotaService.cargarConAcceso(mascotaId, user);
        return repo.findByMascotaIdOrderByFechaDescIdDesc(mascotaId).stream().map(ConsultaResponse::desde).toList();
    }

    @Transactional
    public ConsultaResponse registrar(Long mascotaId, ConsultaRequest req, UsuarioAutenticado user) {
        Mascota m = mascotaService.cargarConAcceso(mascotaId, user);
        if (!m.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se pueden registrar consultas en una mascota inactiva");
        }
        Consulta c = new Consulta();
        c.setMascota(m);
        aplicar(c, req);
        return ConsultaResponse.desde(repo.save(c));
    }

    @Transactional
    public ConsultaResponse actualizar(Long id, ConsultaRequest req) {
        Consulta c = buscar(id);
        aplicar(c, req);
        return ConsultaResponse.desde(repo.save(c));
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(buscar(id));
    }

    private void aplicar(Consulta c, ConsultaRequest req) {
        LocalDate fecha = req.fecha() != null ? req.fecha() : LocalDate.now();
        if (req.proximoControl() != null && req.proximoControl().isBefore(fecha)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El próximo control no puede ser anterior a la fecha de la consulta");
        }
        c.setFecha(fecha);
        boolean sinMotivo = req.motivo() == null || req.motivo().isBlank();
        boolean sinDiagnostico = req.diagnostico() == null || req.diagnostico().isBlank();
        if (sinMotivo && sinDiagnostico) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debes indicar al menos el motivo o el diagnóstico");
        }
        c.setMotivo(sinMotivo ? null : req.motivo().trim());
        c.setDiagnostico(req.diagnostico());
        c.setTratamiento(req.tratamiento());
        c.setMedicamentos(req.medicamentos());
        c.setVeterinario(req.veterinario());
        c.setProximoControl(req.proximoControl());
    }

    private Consulta buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Consulta no encontrada con id " + id));
    }
}
