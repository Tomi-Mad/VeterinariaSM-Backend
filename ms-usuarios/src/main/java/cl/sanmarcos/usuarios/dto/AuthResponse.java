package cl.sanmarcos.usuarios.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tipo,
        long expiraEnSegundos,
        UsuarioResponse usuario
) {}
