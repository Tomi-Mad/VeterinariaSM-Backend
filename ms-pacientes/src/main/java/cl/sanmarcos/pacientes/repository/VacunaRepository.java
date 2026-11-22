package cl.sanmarcos.pacientes.repository;

import cl.sanmarcos.pacientes.model.Vacuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface VacunaRepository extends JpaRepository<Vacuna, Long> {

    List<Vacuna> findByMascotaIdOrderByFechaAplicacionDescIdDesc(Long mascotaId);

    /** ¿Ya se aplicó una dosis posterior de la misma vacuna? (entonces el aviso ya no corresponde) */
    boolean existsByMascotaIdAndNombreIgnoreCaseAndFechaAplicacionAfter(Long mascotaId, String nombre, LocalDate fecha);

    @Query("select v from Vacuna v where v.mascota.activo = true "
         + "and v.fechaProximaDosis between :desde and :hasta order by v.fechaProximaDosis")
    List<Vacuna> porVencer(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("select v from Vacuna v where v.mascota.activo = true and v.mascota.duenoId = :duenoId "
         + "and v.fechaProximaDosis between :desde and :hasta order by v.fechaProximaDosis")
    List<Vacuna> porVencerDeDueno(@Param("duenoId") Long duenoId,
                                  @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
