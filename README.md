# Veterinaria San Marcos – Backend

Backend en **microservicios Spring Boot** (Java 21, Maven, MySQL, JWT, Swagger, Docker).
Cada microservicio es independiente, tiene su propio `application.properties`, su propia base de datos y responde siempre JSON (incluidos los errores).

| Microservicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| ms-usuarios | 8081 | vet_usuarios | Registro, login, JWT (access + refresh), roles y panel de administración |
| ms-pacientes | 8082 | vet_pacientes | Mascotas, ficha clínica (consultas), vacunas, recetas, exámenes y reportes |
| ms-citas | 8083 | vet_citas | Solicitud de citas: solicitar → confirmar / proponer horario → aceptar / cancelar |

## Seguridad (RBAC)
Roles: `ADMINISTRADOR`, `RECEPCIONISTA`, `VETERINARIO`, `DUENO`.
- ms-usuarios emite el JWT; ms-pacientes y ms-citas lo validan con el **mismo `JWT_SECRET`**.
- El registro público siempre crea `DUENO`; el personal lo crea el administrador.
- Un dueño solo accede a sus propias mascotas, fichas y citas.
- ms-citas consulta a ms-pacientes por REST (reenviando el token) para validar la mascota.

## Cómo levantarlo (desarrollo, con XAMPP)
1. Iniciar MySQL en XAMPP (las bases se crean solas).
2. En cada carpeta, en este orden: `mvn spring-boot:run` → `ms-usuarios`, `ms-pacientes`, `ms-citas`.
3. Swagger: http://localhost:8081/swagger-ui.html · :8082 · :8083
4. Admin inicial: `admin@sanmarcos.cl` / `Admin12345` (cambiar en producción con `ADMIN_PASSWORD`).

## Con Docker
`docker compose up --build` (usa MySQL propio en el puerto 3307). Variables: `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_PASSWORD`, `CORS_ORIGINS`.

## Más información
- `docs/PRUEBAS.md`: guía paso a paso para probar todo.
- Cada microservicio tiene su propio README con la tabla de endpoints.
