package cl.sanmarcos.pacientes.repository;

import cl.sanmarcos.pacientes.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecetaRepository extends JpaRepository<Receta, Long> {
    List<Receta> findByMascotaIdOrderByFechaDescIdDesc(Long mascotaId);
}
