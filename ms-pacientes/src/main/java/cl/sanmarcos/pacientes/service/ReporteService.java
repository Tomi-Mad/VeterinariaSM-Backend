package cl.sanmarcos.pacientes.service;

import cl.sanmarcos.pacientes.dto.ReporteResponse;
import cl.sanmarcos.pacientes.exception.ApiException;
import cl.sanmarcos.pacientes.repository.ConsultaRepository;
import cl.sanmarcos.pacientes.repository.MascotaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reporte por período: pacientes nuevos y atenciones, agrupados por mes. */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final MascotaRepository mascotas;
    private final ConsultaRepository consultas;

    public ReporteService(MascotaRepository mascotas, ConsultaRepository consultas) {
        this.mascotas = mascotas;
        this.consultas = consultas;
    }

    public ReporteResponse resumen(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate ini = desde != null ? desde : fin.minusMonths(5).withDayOfMonth(1);
        if (ini.isAfter(fin)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "'desde' no puede ser posterior a 'hasta'");
        }

        Map<YearMonth, Long> nuevos = new HashMap<>();
        mascotas.findByFechaRegistroBetween(ini.atStartOfDay(), fin.plusDays(1).atStartOfDay())
                .forEach(m -> nuevos.merge(YearMonth.from(m.getFechaRegistro()), 1L, Long::sum));

        Map<YearMonth, Long> atenciones = new HashMap<>();
        consultas.findByFechaBetween(ini, fin)
                .forEach(c -> atenciones.merge(YearMonth.from(c.getFecha()), 1L, Long::sum));

        List<ReporteResponse.Mes> porMes = new ArrayList<>();
        for (YearMonth ym = YearMonth.from(ini); !ym.isAfter(YearMonth.from(fin)); ym = ym.plusMonths(1)) {
            porMes.add(new ReporteResponse.Mes(ym.toString(), nuevos.getOrDefault(ym, 0L), atenciones.getOrDefault(ym, 0L)));
        }
        long totalNuevos = porMes.stream().mapToLong(ReporteResponse.Mes::pacientesNuevos).sum();
        long totalAtenciones = porMes.stream().mapToLong(ReporteResponse.Mes::atenciones).sum();
        return new ReporteResponse(ini, fin, totalNuevos, totalAtenciones, porMes);
    }
}
