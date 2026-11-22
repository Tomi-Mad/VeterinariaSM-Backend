package cl.sanmarcos.usuarios.service;

import cl.sanmarcos.usuarios.dto.*;
import cl.sanmarcos.usuarios.exception.ApiException;
import cl.sanmarcos.usuarios.model.Rol;
import cl.sanmarcos.usuarios.model.Usuario;
import cl.sanmarcos.usuarios.repository.UsuarioRepository;
import cl.sanmarcos.usuarios.util.RutUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Lógica del panel de administración: crear, editar, desactivar y asignar roles. */
@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public List<UsuarioResponse> listar() {
        return repo.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    /** Dueños activos, con filtro opcional por nombre/apellido/email. Lo usa la recepción para registrar mascotas. */
    public List<UsuarioResponse> listarDuenos(String q) {
        String filtro = q == null ? "" : q.trim().toLowerCase();
        return repo.findByRolAndActivoTrue(Rol.DUENO).stream()
                .filter(u -> filtro.isEmpty()
                        || u.getNombre().toLowerCase().contains(filtro)
                        || u.getApellido().toLowerCase().contains(filtro)
                        || u.getEmail().contains(filtro)
                        || (u.getRut() != null && u.getRut().contains(filtro.replace(".", ""))))
                .map(UsuarioResponse::desde).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest req) {
        String email = req.email().trim().toLowerCase();
        if (repo.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }
        String rut = RutUtil.normalizar(req.rut());
        if (!RutUtil.esValido(rut)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El RUT ingresado no es válido");
        }
        if (repo.existsByRut(rut)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese RUT");
        }
        Usuario u = new Usuario();
        u.setRut(rut);
        u.setNombre(req.nombre().trim());
        u.setApellido(req.apellido().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password()));
        u.setTelefono(req.telefono());
        u.setRol(req.rol());
        return UsuarioResponse.desde(repo.save(u));
    }

    @Transactional
    public UsuarioResponse editar(Long id, UsuarioEditarRequest req, String emailAdminActual) {
        Usuario u = buscar(id);
        String email = req.email().trim().toLowerCase();

        if (!email.equals(u.getEmail()) && repo.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }
        // Regla de negocio: un admin no puede quitarse a sí mismo el rol (evita dejar el sistema sin admin).
        if (u.getEmail().equals(emailAdminActual) && req.rol() != Rol.ADMINISTRADOR) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes quitarte tu propio rol de Administrador");
        }
        if (req.rut() != null && !req.rut().isBlank()) {
            String rut = RutUtil.normalizar(req.rut());
            if (!RutUtil.esValido(rut)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "El RUT ingresado no es válido");
            }
            if (repo.existsByRutAndIdNot(rut, id)) {
                throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese RUT");
            }
            u.setRut(rut);
        }
        u.setNombre(req.nombre().trim());
        u.setApellido(req.apellido().trim());
        u.setEmail(email);
        u.setTelefono(req.telefono());
        u.setRol(req.rol());
        if (req.password() != null && !req.password().isBlank()) {
            u.setPassword(encoder.encode(req.password()));
        }
        return UsuarioResponse.desde(repo.save(u));
    }

    /** "Eliminar" = desactivar (borrado lógico): se conserva el historial. */
    @Transactional
    public UsuarioResponse desactivar(Long id, String emailAdminActual) {
        Usuario u = buscar(id);
        if (u.getEmail().equals(emailAdminActual)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propia cuenta");
        }
        u.setActivo(false);
        return UsuarioResponse.desde(repo.save(u));
    }

    @Transactional
    public UsuarioResponse activar(Long id) {
        Usuario u = buscar(id);
        u.setActivo(true);
        return UsuarioResponse.desde(repo.save(u));
    }

    private Usuario buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado con id " + id));
    }
}
