# Guía de pruebas (Swagger o Postman)

Antes: XAMPP con MySQL encendido y los 3 microservicios corriendo (8081, 8082, 8083).
Para usar el token en Swagger: botón **Authorize** → pegar solo el `accessToken`.

## 1. Seguridad y roles (ms-usuarios, 8081)
1. `POST /api/auth/login` con `admin@sanmarcos.cl` / `Admin12345` → guardar `accessToken` (ADMIN).
2. `POST /api/usuarios` (con token admin) → crear una recepcionista (`rol: RECEPCIONISTA`) y un veterinario (`rol: VETERINARIO`). Usar RUT válidos, ej. `12345678-5`, `11111111-1`, `7654321-6`.
3. `POST /api/auth/registro` (sin token) → registrar un dueño. Verificar que `rol` sea `DUENO`.
4. Con el token del dueño: `GET /api/usuarios` → debe dar **403**. Sin token → **401**.
5. `POST /api/auth/refresh` con el `refreshToken` → entrega tokens nuevos.
6. `DELETE /api/usuarios/{id}` (desactivar) y luego intentar login con ese usuario → **403**.

## 2. Pacientes (ms-pacientes, 8082)
1. Token de recepcionista: `GET http://localhost:8081/api/usuarios/duenos` → ver el `id` del dueño.
2. `POST /api/mascotas` con `duenoId` → crea la mascota.
3. Token de veterinario: `POST /api/mascotas/{id}/consultas`, `/vacunas` (con `fechaProximaDosis` en ~10 días), `/recetas`, `/examenes`.
4. Token de recepcionista: intentar `POST /api/mascotas/{id}/consultas` → **403** (solo veterinario/admin).
5. Token del dueño: `GET /api/mascotas` → solo las suyas; `GET /api/mascotas/{id de otro}` → **403**; `GET /api/vacunas/por-vencer` → aviso de la vacuna.
6. Token de recepcionista/admin: `GET /api/reportes/resumen`.

## 3. Citas (ms-citas, 8083)
1. Token dueño: `GET /api/citas/disponibilidad?fecha=<día hábil futuro>`.
2. Token dueño: `POST /api/citas` `{mascotaId, fecha, hora:"10:00", motivo}` → estado **PENDIENTE**.
3. Token recepcionista: `PATCH /api/citas/{id}/confirmar` → **CONFIRMADA**. O `proponer-horario` → **HORARIO_PROPUESTO**, y con token dueño `aceptar-horario`.
4. Errores esperados: hora fuera de horario (400), domingo (400), mascota ya con cita ese día (409), mascota de otro dueño (403), 4.º cita en el mismo bloque (409).
5. `GET /api/citas/reporte` (recepcionista/admin).

## 4. Verificar en XAMPP (phpMyAdmin)
Deben existir las bases `vet_usuarios`, `vet_pacientes` y `vet_citas`.
Las contraseñas en `usuarios.password` deben verse como hash (`$2a$10$...`), nunca en texto plano.

## 5. Docker (opcional, para AWS)
`docker compose up --build` en la raíz (MySQL queda en el puerto 3307 para no chocar con XAMPP).
