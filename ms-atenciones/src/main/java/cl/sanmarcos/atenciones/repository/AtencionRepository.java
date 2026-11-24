package cl.sanmarcos.atenciones.repository;

import cl.sanmarcos.atenciones.model.Atencion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {

    /** Historial completo ordenado desde la atención más reciente */
    List<Atencion> findAllByOrderByFechaDescCreadoEnDesc();

    /** Historial filtrado para un dueño específico (solo sus mascotas) */
    List<Atencion> findByDuenoIdOrderByFechaDescCreadoEnDesc(Long duenoId);

    /** Rango de fechas para el endpoint /api/atenciones/reporte */
    List<Atencion> findByFechaBetween(LocalDate desde, LocalDate hasta);

}
