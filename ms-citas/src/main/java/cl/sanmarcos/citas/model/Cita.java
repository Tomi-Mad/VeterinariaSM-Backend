package cl.sanmarcos.citas.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "citas", indexes = {
        @Index(name = "idx_cita_fecha_hora", columnList = "fecha,hora"),
        @Index(name = "idx_cita_dueno", columnList = "duenoId")
})
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Referencias lógicas a ms-pacientes / ms-usuarios (cada microservicio tiene su propia BD). */
    @Column(nullable = false)
    private Long mascotaId;

    /** Copia del nombre para mostrar la cita sin consultar a otro servicio. */
    @Column(nullable = false, length = 80)
    private String mascotaNombre;

    @Column(nullable = false)
    private Long duenoId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    /** Nuevo horario sugerido por recepción (solo cuando estado = HORARIO_PROPUESTO). */
    private LocalDate fechaPropuesta;
    private LocalTime horaPropuesta;

    /** Mensaje de recepción (propuesta) o motivo de cancelación. */
    @Column(length = 500)
    private String mensaje;

    @Column(nullable = false)
    private Long creadaPorId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    @PreUpdate
    void alActualizar() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public LocalDateTime inicio() {
        return LocalDateTime.of(fecha, hora);
    }

    public Long getId() { return id; }
    public Long getMascotaId() { return mascotaId; }
    public void setMascotaId(Long mascotaId) { this.mascotaId = mascotaId; }
    public String getMascotaNombre() { return mascotaNombre; }
    public void setMascotaNombre(String mascotaNombre) { this.mascotaNombre = mascotaNombre; }
    public Long getDuenoId() { return duenoId; }
    public void setDuenoId(Long duenoId) { this.duenoId = duenoId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
    public LocalDate getFechaPropuesta() { return fechaPropuesta; }
    public void setFechaPropuesta(LocalDate fechaPropuesta) { this.fechaPropuesta = fechaPropuesta; }
    public LocalTime getHoraPropuesta() { return horaPropuesta; }
    public void setHoraPropuesta(LocalTime horaPropuesta) { this.horaPropuesta = horaPropuesta; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public Long getCreadaPorId() { return creadaPorId; }
    public void setCreadaPorId(Long creadaPorId) { this.creadaPorId = creadaPorId; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
}
