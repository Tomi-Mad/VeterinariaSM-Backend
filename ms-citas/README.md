# ms-citas – Veterinaria San Marcos

Microservicio de **solicitud y gestión de citas** (el "pedido" del proyecto). Spring Boot 3.3 / Java 21 / MySQL, puerto **8083**, base `vet_citas`.
Valida los JWT de **ms-usuarios** y consulta a **ms-pacientes** (REST, reenviando el token) para verificar que la mascota exista y sea del dueño.

## Flujo
```
Dueño solicita ──> PENDIENTE ──(recepción confirma)──> CONFIRMADA ──> ATENDIDA | NO_ASISTIO
                      │                                     │
                      └──(recepción propone otro horario)───┴──> HORARIO_PROPUESTO ──(dueño acepta)──> CONFIRMADA
Cualquier estado activo ──(cancelar)──> CANCELADA
```
Si la cita la crea el personal (ej. por teléfono) queda directamente CONFIRMADA.

## Endpoints
| Método | Ruta | Quién |
|---|---|---|
| POST | /api/citas | Dueño (→ PENDIENTE) · Personal (→ CONFIRMADA) |
| GET | /api/citas (?estado= ?fecha= ?mascotaId=) | Personal: todas · Dueño: solo las suyas |
| GET | /api/citas/{id} | Personal · Dueño (solo suya) |
| GET | /api/citas/disponibilidad?fecha= | Cualquier usuario autenticado |
| GET | /api/citas/reporte?desde=&hasta= | Administrador · Recepcionista |
| PATCH | /api/citas/{id}/confirmar | Administrador · Recepcionista |
| PATCH | /api/citas/{id}/proponer-horario | Administrador · Recepcionista |
| PATCH | /api/citas/{id}/aceptar-horario | Dueño de la cita (o personal) |
| PATCH | /api/citas/{id}/cancelar | Dueño de la cita · Personal |
| PATCH | /api/citas/{id}/atender | Administrador · Recepcionista · Veterinario |
| PATCH | /api/citas/{id}/no-asistio | Administrador · Recepcionista |

## Reglas de negocio
- Horario de atención lunes a sábado, 09:00–18:00, bloques de 30 min (configurable en `application.properties`).
- Fecha y hora futuras, máximo 90 días de anticipación.
- Cupos por bloque = 3 (los 3 veterinarios). Las citas PENDIENTE y CONFIRMADA ocupan cupo.
- Una mascota no puede tener dos citas activas el mismo día.
- El dueño solo puede pedir citas para sus mascotas y ver/cancelar las suyas; no puede cancelar una cita que ya comenzó.
- "No asistió" solo se puede marcar después de la hora de la cita. El reporte calcula la **tasa de inasistencia** (el caso parte de ~20 %).

## Cómo correrlo
1. XAMPP → MySQL. 2. Levantar ms-usuarios y ms-pacientes. 3. `mvn spring-boot:run` aquí.
4. Swagger: http://localhost:8083/swagger-ui.html
