package cl.sanmarcos.citas.dto;

import jakarta.validation.constraints.Size;

public record CancelarRequest(@Size(max = 500) String motivo) {}
