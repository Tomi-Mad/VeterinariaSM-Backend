# ms-pacientes – Veterinaria San Marcos

Microservicio de **mascotas, ficha clínica, vacunas y reportes** (Spring Boot 3.3 / Java 21 / MySQL).
Valida los JWT emitidos por **ms-usuarios** (mismo `JWT_SECRET`), por eso ms-usuarios debe estar creado/configurado antes.

## Cómo correrlo
1. XAMPP → iniciar MySQL (la base `vet_pacientes` se crea sola).
2. `mvn spring-boot:run` dentro de esta carpeta (puerto **8082**).
3. Swagger: http://localhost:8082/swagger-ui.html
4. Token: hacer login en ms-usuarios (http://localhost:8081) y pegarlo en **Authorize**.

## Endpoints
| Método | Ruta | Quién |
|---|---|---|
| GET | /api/mascotas (?q=, ?duenoId=) | Personal: todas · Dueño: solo las suyas |
| GET | /api/mascotas/{id} | Personal · Dueño (solo si es suya) |
| POST | /api/mascotas | Personal (indica duenoId) · Dueño (se asigna a sí mismo) |
| PUT / DELETE | /api/mascotas/{id} | Personal (DELETE = desactivar) |
| GET | /api/mascotas/{id}/consultas | Personal · Dueño (solo suya) |
| POST | /api/mascotas/{id}/consultas | Veterinario · Administrador |
| PUT | /api/consultas/{id} | Veterinario · Administrador |
| DELETE | /api/consultas/{id} | Solo Administrador |
| GET | /api/mascotas/{id}/vacunas | Personal · Dueño (solo suya) |
| POST | /api/mascotas/{id}/vacunas | Veterinario · Administrador |
| PUT | /api/vacunas/{id} | Veterinario · Administrador |
| DELETE | /api/vacunas/{id} | Solo Administrador |
| GET / POST | /api/mascotas/{id}/recetas | GET: personal y dueño de la mascota · POST: Veterinario · Administrador |
| PUT / DELETE | /api/recetas/{id} | PUT: Veterinario · Administrador · DELETE: solo Administrador |
| GET / POST | /api/mascotas/{id}/examenes | GET: personal y dueño de la mascota · POST: Veterinario · Administrador |
| PUT / DELETE | /api/examenes/{id} | PUT: Veterinario · Administrador · DELETE: solo Administrador |
| GET | /api/vacunas/por-vencer?dias=30 | Personal: todas · Dueño: las suyas |
| GET | /api/reportes/resumen?desde=&hasta= | Administrador · Recepcionista |

"Personal" = Administrador, Recepcionista y Veterinario.
