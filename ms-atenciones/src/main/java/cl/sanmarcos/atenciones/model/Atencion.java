package cl.sanmarcos.atenciones.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "atenciones")
public class Atencion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long mascotaId;

    @Column(nullable = false)
    private String mascotaNombre;

    @Column(nullable = false)
    private Long duenoId;

    @Column(nullable = false)
    private Long veterinarioId;

    @Column(nullable = false)
    private String veterinarioEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAtencion tipo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 250)
    private String motivo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // Campos específicos opcionales (para Vacunas, Exámenes, Diagnósticos y Recetas)
    @Column(length = 150)
    private String productoOExamen; // Nombre de vacuna, tipo de examen o diagnóstico principal

    @Column(length = 150)
    private String dosisOResultado; // Dosis aplicada, resultado de examen o posología de receta

    private LocalDate proximaFecha; // Fecha de refuerzo (vacuna) o próximo control

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
        if (this.fecha == null) {
            this.fecha = LocalDate.now();
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMascotaId() { return mascotaId; }
    public void setMascotaId(Long mascotaId) { this.mascotaId = mascotaId; }

    public String getMascotaNombre() { return mascotaNombre; }
    public void setMascotaNombre(String mascotaNombre) { this.mascotaNombre = mascotaNombre; }

    public Long getDuenoId() { return duenoId; }
    public void setDuenoId(Long duenoId) { this.duenoId = duenoId; }

    public Long getVeterinarioId() { return veterinarioId; }
    public void setVeterinarioId(Long veterinarioId) { this.veterinarioId = veterinarioId; }

    public String getVeterinarioEmail() { return veterinarioEmail; }
    public void setVeterinarioEmail(String veterinarioEmail) { this.veterinarioEmail = veterinarioEmail; }

    public TipoAtencion getTipo() { return tipo; }
    public void setTipo(TipoAtencion tipo) { this.tipo = tipo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getProductoOExamen() { return productoOExamen; }
    public void setProductoOExamen(String productoOExamen) { this.productoOExamen = productoOExamen; }

    public String getDosisOResultado() { return dosisOResultado; }
    public void setDosisOResultado(String dosisOResultado) { this.dosisOResultado = dosisOResultado; }

    public LocalDate getProximaFecha() { return proximaFecha; }
    public void setProximaFecha(LocalDate proximaFecha) { this.proximaFecha = proximaFecha; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
