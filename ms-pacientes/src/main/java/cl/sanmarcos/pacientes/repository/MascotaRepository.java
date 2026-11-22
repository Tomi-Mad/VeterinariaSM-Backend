package cl.sanmarcos.pacientes.repository;

import cl.sanmarcos.pacientes.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByActivoTrueOrderByNombreAsc();
    List<Mascota> findByDuenoIdAndActivoTrueOrderByNombreAsc(Long duenoId);
    List<Mascota> findByActivoTrueAndNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);
    List<Mascota> findByFechaRegistroBetween(LocalDateTime desde, LocalDateTime hasta);
}
