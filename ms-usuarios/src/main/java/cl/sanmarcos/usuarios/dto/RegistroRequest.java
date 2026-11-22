package cl.sanmarcos.usuarios.dto;

import jakarta.validation.constraints.*;

/** Registro público: siempre crea un usuario con rol DUENO. */
public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 80) String nombre,
        @NotBlank(message = "El apellido es obligatorio") @Size(max = 80) String apellido,
        @NotBlank(message = "El RUT es obligatorio") @Size(max = 14) String rut,
        @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
        @Size(max = 20) String telefono
) {}
