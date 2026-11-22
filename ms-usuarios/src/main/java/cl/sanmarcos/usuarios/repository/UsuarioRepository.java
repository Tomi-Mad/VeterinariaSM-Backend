package cl.sanmarcos.usuarios.repository;

import cl.sanmarcos.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import cl.sanmarcos.usuarios.model.Rol;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRut(String rut);
    boolean existsByRutAndIdNot(String rut, Long id);
    List<Usuario> findByRolAndActivoTrue(Rol rol);
}
