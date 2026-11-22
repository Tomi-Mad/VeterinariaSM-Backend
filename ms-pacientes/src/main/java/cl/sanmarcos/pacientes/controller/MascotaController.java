package cl.sanmarcos.pacientes.controller;

import cl.sanmarcos.pacientes.dto.*;
import cl.sanmarcos.pacientes.security.UsuarioAutenticado;
import cl.sanmarcos.pacientes.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@Tag(name = "Mascotas", description = "Fichas de pacientes. El dueño solo ve las suyas.")
public class MascotaController {

    private final MascotaService mascotas;
    private final ConsultaService consultas;
    private final VacunaService vacunas;
    private final RecetaService recetas;
    private final ExamenService examenes;

    public MascotaController(MascotaService mascotas, ConsultaService consultas, VacunaService vacunas,
                             RecetaService recetas, ExamenService examenes) {
        this.mascotas = mascotas;
        this.consultas = consultas;
        this.vacunas = vacunas;
        this.recetas = recetas;
        this.examenes = examenes;
    }

    // ---------- Mascotas ----------
    @GetMapping
    @Operation(summary = "Lista mascotas (personal: todas, con ?q=nombre o ?duenoId=; dueño: solo las suyas)")
    public List<MascotaResponse> listar(@AuthenticationPrincipal UsuarioAutenticado user,
                                        @RequestParam(required = false) String q,
                                        @RequestParam(required = false) Long duenoId) {
        return mascotas.listar(user, q, duenoId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una mascota")
    public MascotaResponse obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado user) {
        return mascotas.obtener(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una mascota (el dueño solo puede registrar las suyas)")
    public MascotaResponse crear(@Valid @RequestBody MascotaRequest req, @AuthenticationPrincipal UsuarioAutenticado user) {
        return mascotas.crear(req, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita una mascota (personal)")
    public MascotaResponse actualizar(@PathVariable Long id, @Valid @RequestBody MascotaRequest req) {
        return mascotas.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactiva una mascota (borrado lógico, personal)")
    public void desactivar(@PathVariable Long id) {
        mascotas.desactivar(id);
    }

    // ---------- Historial clínico ----------
    @GetMapping("/{id}/consultas")
    @Operation(summary = "Historial clínico de la mascota (más reciente primero)")
    public List<ConsultaResponse> historial(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado user) {
        return consultas.historial(id, user);
    }

    @PostMapping("/{id}/consultas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una consulta en la ficha (personal)")
    public ConsultaResponse registrarConsulta(@PathVariable Long id, @Valid @RequestBody ConsultaRequest req,
                                              @AuthenticationPrincipal UsuarioAutenticado user) {
        return consultas.registrar(id, req, user);
    }

    // ---------- Vacunas ----------
    @GetMapping("/{id}/vacunas")
    @Operation(summary = "Historial de vacunación de la mascota")
    public List<VacunaResponse> listarVacunas(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado user) {
        return vacunas.listar(id, user);
    }

    @PostMapping("/{id}/vacunas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una vacuna aplicada (personal)")
    public VacunaResponse registrarVacuna(@PathVariable Long id, @Valid @RequestBody VacunaRequest req,
                                          @AuthenticationPrincipal UsuarioAutenticado user) {
        return vacunas.registrar(id, req, user);
    }

    // ---------- Recetas ----------
    @GetMapping("/{id}/recetas")
    @Operation(summary = "Recetas de la mascota (el dueño puede consultar los medicamentos recetados)")
    public List<RecetaResponse> listarRecetas(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado user) {
        return recetas.listar(id, user);
    }

    @PostMapping("/{id}/recetas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Emite una receta (Veterinario / Administrador)")
    public RecetaResponse registrarReceta(@PathVariable Long id, @Valid @RequestBody RecetaRequest req,
                                          @AuthenticationPrincipal UsuarioAutenticado user) {
        return recetas.registrar(id, req, user);
    }

    // ---------- Exámenes ----------
    @GetMapping("/{id}/examenes")
    @Operation(summary = "Exámenes de la mascota")
    public List<ExamenResponse> listarExamenes(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado user) {
        return examenes.listar(id, user);
    }

    @PostMapping("/{id}/examenes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un examen (Veterinario / Administrador)")
    public ExamenResponse registrarExamen(@PathVariable Long id, @Valid @RequestBody ExamenRequest req,
                                          @AuthenticationPrincipal UsuarioAutenticado user) {
        return examenes.registrar(id, req, user);
    }
}
