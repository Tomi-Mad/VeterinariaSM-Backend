package cl.sanmarcos.citas.repository;

import cl.sanmarcos.citas.model.Cita;
import cl.sanmarcos.citas.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByDuenoIdOrderByFechaDescHoraDesc(Long duenoId);

    List<Cita> findAllByOrderByFechaAscHoraAsc();

    long countByFechaAndHoraAndEstadoIn(LocalDate fecha, LocalTime hora, Collection<EstadoCita> estados);

    List<Cita> findByFechaAndEstadoIn(LocalDate fecha, Collection<EstadoCita> estados);

    /** ¿La mascota ya tiene otra cita activa ese día? */
    boolean existsByMascotaIdAndFechaAndEstadoInAndIdNot(Long mascotaId, LocalDate fecha,
                                                         Collection<EstadoCita> estados, Long idExcluido);

    List<Cita> findByFechaBetween(LocalDate desde, LocalDate hasta);
}
