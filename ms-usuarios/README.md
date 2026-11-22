# ms-usuarios – Veterinaria San Marcos

Microservicio de **usuarios, autenticación JWT y roles** (Spring Boot 3.3 / Java 21 / MySQL).

## Cómo correrlo
1. Abrir XAMPP y **iniciar MySQL** (la base `vet_usuarios` se crea sola).
2. En VS Code instalar la extensión *Extension Pack for Java* y *Spring Boot Extension Pack*.
3. En una terminal dentro de esta carpeta: `mvn spring-boot:run`
4. Swagger: http://localhost:8081/swagger-ui.html

Admin inicial (se crea solo la primera vez): `admin@sanmarcos.cl` / `Admin12345`

## Endpoints
| Método | Ruta | Acceso |
|---|---|---|
| POST | /api/auth/registro | Público (crea rol DUENO) |
| POST | /api/auth/login | Público |
| POST | /api/auth/refresh | Público (con refresh token) |
| GET | /api/auth/me | Cualquier usuario autenticado |
| GET | /api/usuarios/duenos?q= | ADMINISTRADOR, RECEPCIONISTA y VETERINARIO |
| GET/POST | /api/usuarios | Solo ADMINISTRADOR |
| GET/PUT/DELETE | /api/usuarios/{id} | Solo ADMINISTRADOR (DELETE = desactivar) |
| PATCH | /api/usuarios/{id}/activar | Solo ADMINISTRADOR |

## Probar en Swagger
1. `POST /api/auth/login` con el admin → copiar `accessToken`.
2. Botón **Authorize** (arriba a la derecha) → pegar el token → Authorize.
3. Probar `GET /api/usuarios`.

## Variables de entorno (Docker / AWS)
`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (mín. 32 caracteres, igual en todos los microservicios), `CORS_ORIGINS`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.

## Roles
ADMINISTRADOR · RECEPCIONISTA · VETERINARIO · DUENO. El registro público siempre crea DUENO; el personal lo crea el administrador desde el panel.
El RUT se valida (dígito verificador) y no puede repetirse.
