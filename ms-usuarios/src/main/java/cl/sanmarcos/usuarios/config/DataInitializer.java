package cl.sanmarcos.usuarios.config;

import cl.sanmarcos.usuarios.model.Rol;
import cl.sanmarcos.usuarios.model.Usuario;
import cl.sanmarcos.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Crea el primer Administrador (si la tabla está vacía) para poder entrar al sistema. */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner crearAdminInicial(UsuarioRepository repo, PasswordEncoder encoder,
                                        @Value("${app.admin.email}") String email,
                                        @Value("${app.admin.password}") String password) {
        return args -> {
            if (repo.count() == 0) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setApellido("Sistema");
                admin.setEmail(email);
                admin.setPassword(encoder.encode(password));
                admin.setRol(Rol.ADMINISTRADOR);
                repo.save(admin);
                System.out.println(">> Administrador inicial creado: " + email);
            }
        };
    }
}
