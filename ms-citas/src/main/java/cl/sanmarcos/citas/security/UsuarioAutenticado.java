package cl.sanmarcos.citas.security;

/** Datos del JWT. Se conserva el token para reenviarlo a ms-pacientes. */
public record UsuarioAutenticado(Long id, String email, String rol, String token) {
    public boolean esStaff() {
        return "ADMINISTRADOR".equals(rol) || "RECEPCIONISTA".equals(rol) || "VETERINARIO".equals(rol);
    }
    public boolean esDueno() {
        return "DUENO".equals(rol);
    }
}
