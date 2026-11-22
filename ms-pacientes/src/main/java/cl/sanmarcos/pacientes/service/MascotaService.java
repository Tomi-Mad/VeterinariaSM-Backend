package cl.sanmarcos.pacientes.service;

import cl.sanmarcos.pacientes.dto.MascotaRequest;
import cl.sanmarcos.pacientes.dto.MascotaResponse;
import cl.sanmarcos.pacientes.exception.ApiException;
import cl.sanmarcos.pacientes.model.Mascota;
import cl.sanmarcos.pacientes.repository.MascotaRepository;
import cl.sanmarcos.pacientes.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MascotaService {

    private final MascotaRepository repo;

    public MascotaService(MascotaRepository repo) {
        this.repo = repo;
    }

    /** Personal: todas (con filtros opcionales). Dueño: solo las suyas. */
    public List<MascotaResponse> listar(UsuarioAutenticado user, String q, Long duenoId) {
        List<Mascota> lista;
        if (user.esDueno()) {
            lista = repo.findByDuenoIdAndActivoTrueOrderByNombreAsc(user.id());
        } else if (duenoId != null) {
            lista = repo.findByDuenoIdAndActivoTrueOrderByNombreAsc(duenoId);
        } else if (q != null && !q.isBlank()) {
            lista = repo.findByActivoTrueAndNombreContainingIgnoreCaseOrderByNombreAsc(q.trim());
        } else {
            lista = repo.findByActivoTrueOrderByNombreAsc();
        }
        return lista.stream().map(MascotaResponse::desde).toList();
    }

    public MascotaResponse obtener(Long id, UsuarioAutenticado user) {
        return MascotaResponse.desde(cargarConAcceso(id, user));
    }

    /**
     * Regla de privacidad central del sistema: un DUENO solo accede a sus propias mascotas.
     * Los demás servicios (consultas, vacunas) pasan por aquí.
     */
    public Mascota cargarConAcceso(Long id, UsuarioAutenticado user) {
        Mascota m = repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Mascota no encontrada con id " + id));
        if (user.esDueno()) {
            if (!m.getDuenoId().equals(user.id()) || !m.isActivo()) {
                throw new ApiException(HttpStatus.FORBIDDEN, "No tienes acceso a los datos de esta mascota");
            }
        }
        return m;
    }

    @Transactional
    public MascotaResponse crear(MascotaRequest req, UsuarioAutenticado user) {
        Long duenoId;
        if (user.esDueno()) {
            duenoId = user.id();                       // un dueño solo puede registrar mascotas propias
        } else {
            if (req.duenoId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Debes indicar el duenoId de la mascota");
            }
            duenoId = req.duenoId();
        }
        Mascota m = new Mascota();
        copiar(m, req);
        m.setDuenoId(duenoId);
        return MascotaResponse.desde(repo.save(m));
    }

    @Transactional
    public MascotaResponse actualizar(Long id, MascotaRequest req) {
        Mascota m = repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Mascota no encontrada con id " + id));
        copiar(m, req);
        if (req.duenoId() != null) {
            m.setDuenoId(req.duenoId());
        }
        return MascotaResponse.desde(repo.save(m));
    }

    /** Borrado lógico: la ficha clínica se conserva. */
    @Transactional
    public void desactivar(Long id) {
        Mascota m = repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Mascota no encontrada con id " + id));
        m.setActivo(false);
        repo.save(m);
    }

    private void copiar(Mascota m, MascotaRequest req) {
        m.setNombre(req.nombre().trim());
        m.setEspecie(req.especie());
        m.setRaza(req.raza());
        m.setSexo(req.sexo());
        m.setFechaNacimiento(req.fechaNacimiento());
        m.setPesoKg(req.pesoKg());
    }
}
