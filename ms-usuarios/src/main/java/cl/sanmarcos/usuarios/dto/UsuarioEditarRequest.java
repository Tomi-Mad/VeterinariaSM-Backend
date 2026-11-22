package cl.sanmarcos.usuarios.dto;

import cl.sanmarcos.usuarios.model.Rol;
import jakarta.validation.constraints.*;

/** Edición por el Administrador. La contraseña es opcional: si viene vacía no se cambia. */
public record UsuarioEditarRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
        @NotBlank(message = "El apellido es obligatorio") @Size(max = 80) String apellido,
        @Size(max = 14) String rut,
        @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") String email,
        @Size(max = 20) String telefono,
        @NotNull(message = "El rol es obligatorio") Rol rol,
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password
) {}
