package cl.sanmarcos.pacientes.security;

/** Datos del usuario sacados del JWT (este microservicio no consulta la BD de usuarios). */
public record UsuarioAutenticado(Long id, String email, String rol) {
    public boolean esStaff() {
        return "ADMINISTRADOR".equals(rol) || "RECEPCIONISTA".equals(rol) || "VETERINARIO".equals(rol);
    }
    /** Personal que puede escribir en la ficha clínica (consultas y vacunas). */
    public boolean esClinico() {
        return "ADMINISTRADOR".equals(rol) || "VETERINARIO".equals(rol);
    }
    public boolean esDueno() {
        return "DUENO".equals(rol);
    }
}
