package cl.sanmarcos.usuarios.dto;

import cl.sanmarcos.usuarios.model.Rol;
import cl.sanmarcos.usuarios.model.Usuario;
import java.time.LocalDateTime;

/** Nunca incluye la contraseña. */
public record UsuarioResponse(
        Long id, String rut, String nombre, String apellido, String email,
        String telefono, Rol rol, boolean activo, LocalDateTime fechaCreacion
) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getRut(), u.getNombre(), u.getApellido(), u.getEmail(),
                u.getTelefono(), u.getRol(), u.isActivo(), u.getFechaCreacion());
    }
}
