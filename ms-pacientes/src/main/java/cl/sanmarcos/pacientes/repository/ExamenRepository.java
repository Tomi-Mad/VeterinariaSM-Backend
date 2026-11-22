package cl.sanmarcos.pacientes.repository;

import cl.sanmarcos.pacientes.model.Examen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamenRepository extends JpaRepository<Examen, Long> {
    List<Examen> findByMascotaIdOrderByFechaDescIdDesc(Long mascotaId);
}
