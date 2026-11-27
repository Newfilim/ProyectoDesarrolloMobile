# Contrato HTTP

Base `/api`. JSON UTF-8. Las rutas protegidas usan `Authorization: Bearer <token>`. R es Residente, A Administrador, T Tesorería y D Directiva. El propietario se resuelve con el ID autenticado. Los IDs son cadenas hexadecimales de 24 caracteres. Las fechas operativas y contables usan `yyyy-MM-dd`. Los instantes usan UTC ISO 8601.

| Método | Ruta | Rol | Entrada | Éxito |
| --- | --- | --- | --- | --- |
| POST | /api/auth/login | Público | Login | 200 LoginResponse |
| GET | /api/auth/me | R A T D | — | 200 UsuarioResponse |
| GET | /api/usuarios | A | — | 200 UsuarioResponse[] |
| GET | /api/usuarios/{id} | A | — | 200 UsuarioResponse |
| POST | /api/usuarios | A | AltaUsuario | 201 UsuarioResponse |
| PUT | /api/usuarios/{id} | A | EdicionUsuario | 200 UsuarioResponse |
| PATCH | /api/usuarios/{id}/estado | A | EstadoUsuario | 200 UsuarioResponse |
| GET | /api/espacios | R A T D | — | 200 EspacioResponse[] |
| GET | /api/espacios/{id} | R A T D | — | 200 EspacioResponse |
| PATCH | /api/espacios/{id}/estado | A | EstadoEspacio | 200 EspacioResponse |
| GET | /api/espacios/{id}/disponibilidad | R A | fecha (query) | 200 DisponibilidadResponse |
| POST | /api/reservas | R | NuevaReserva | 201 ReservaResponse |
| GET | /api/reservas/mis-reservas | R | — | 200 ReservaResponse[] |
| GET | /api/reservas | A | — | 200 ReservaResponse[] |
| GET | /api/reservas/{id} | R propio o A | — | 200 ReservaResponse |
| PATCH | /api/reservas/{id}/aprobar | A | — | 200 ReservaResponse |
| PATCH | /api/reservas/{id}/rechazar | A | — | 200 ReservaResponse |
| PATCH | /api/reservas/{id}/rechazar-impago | A | — | 200 ReservaResponse |
| GET | /api/pagos | T | — | 200 PagoResponse[] |
| POST | /api/pagos/{reservaId}/comprobante | R propio | multipart archivo | 200 ReservaResponse |
| GET | /api/pagos/{reservaId}/comprobante | R propio o T | — | 200 JPEG o PNG |
| PATCH | /api/pagos/{reservaId}/confirmar | T | — | 200 PagoResponse |
| PATCH | /api/pagos/{reservaId}/rechazar | T | — | 200 PagoResponse |
| GET | /api/ingresos | T D | — | 200 IngresoResponse[] |
| POST | /api/ingresos | T | NuevoIngreso | 201 IngresoResponse |
| GET | /api/gastos | T D | — | 200 GastoResponse[] |
| POST | /api/gastos | T | NuevoGasto | 201 GastoResponse |
| GET | /api/finanzas/resumen | R T D | — | 200 ResumenFinancieroResponse |
| GET | /api/finanzas/gastos-por-categoria | T D | — | 200 GastoPorCategoriaResponse[] |

## Entradas

| DTO | Campos obligatorios |
| --- | --- |
| Login | correo, contrasena |
| AltaUsuario | nombre, correo, contrasena, rol |
| EdicionUsuario | nombre, correo |
| EstadoUsuario | activo (booleano) |
| EstadoEspacio | habilitado (booleano) |
| NuevaReserva | espacioId, fechaOperativa, inicio, fin |
| NuevoIngreso | descripcion, monto (entero CLP positivo), fecha |
| NuevoGasto | descripcion, monto (entero CLP positivo), fecha, categoria |

## Respuestas

| DTO | Campos |
| --- | --- |
| UsuarioResponse | id, nombre, correo, rol, activo |
| LoginResponse | token, tipoToken (Bearer), expiraEn (28800), usuario |
| EspacioResponse | id, nombre, capacidadMaxima, habilitado |
| BloqueHorarioResponse | inicio, fin, disponible |
| DisponibilidadResponse | espacioId, fechaOperativa, habilitado, bloques |
| ReservaResponse | id, usuarioId, nombreUsuario, espacioId, nombreEspacio, fechaOperativa, inicio, fin, estado, pago |
| PagoDetalle | estado, comprobanteUrl |
| PagoResponse | reservaId, usuarioId, nombreUsuario, espacioId, nombreEspacio, fechaOperativa, inicio, fin, estadoReserva, estadoPago, comprobanteUrl |
| IngresoResponse | id, descripcion, monto, fecha |
| GastoResponse | id, descripcion, monto, fecha, categoria |
| ResumenFinancieroResponse | ingresos, gastos, balance |
| GastoPorCategoriaResponse | categoria, total |

`pago` es nulo antes de la aprobación. `comprobanteUrl` es nulo si el archivo aún no se ha adjuntado o si el solicitante carece de acceso. Los listados de reservas ordenan por inicio descendente. Los movimientos ordenan por fecha descendente y los usuarios y espacios por nombre ascendente. El desempate usa ID.

Estados de reserva `PENDIENTE`, `APROBADA`, `RECHAZADA` y `CANCELADA`. Estados de pago `PENDIENTE`, `EN_REVISION`, `CONFIRMADO` y `RECHAZADO`. Categorías `SERVICIOS_BASICOS`, `LIMPIEZA`, `INSUMOS`, `MANTENIMIENTO`, `MEJORAS` y `OTROS`.

## Errores

| HTTP | Códigos |
| --- | --- |
| 400 | DATOS_INVALIDOS |
| 401 | NO_AUTENTICADO |
| 403 | ACCESO_DENEGADO |
| 404 | NO_ENCONTRADO |
| 409 | CORREO_DUPLICADO, ULTIMO_ADMINISTRADOR, ESPACIO_DESHABILITADO, HORARIO_OCUPADO, TRANSICION_INVALIDA |
| 413 | ARCHIVO_GRANDE |
| 415 | FORMATO_NO_ADMITIDO |
| 500 | ERROR_INTERNO |
| 503 | DEPENDENCIA_NO_DISPONIBLE |

```json
{"estado":409,"codigo":"HORARIO_OCUPADO","mensaje":"Uno o más horarios seleccionados ya no están disponibles."}
```

## Ejemplos

```powershell
$base = 'http://127.0.0.1:8080/api'
$cuerpo = @{ correo = 'residente@zaror.cl'; contrasena = $env:ZAROR_DEMO_PASSWORD } | ConvertTo-Json
$sesion = Invoke-RestMethod "$base/auth/login" -Method Post -ContentType 'application/json' -Body $cuerpo
$headers = @{ Authorization = "Bearer $($sesion.token)" }
Invoke-RestMethod "$base/espacios" -Headers $headers
```

Ejemplo de `POST /api/reservas`. Consulta disponibilidad de una fecha vigente, usa el ID del espacio y copia los instantes de los límites disponibles. El fin es exclusivo.

```json
{"espacioId":"00000000000000000000000b","fechaOperativa":"2026-12-18","inicio":"2026-12-18T13:00:00Z","fin":"2026-12-18T15:00:00Z"}
```

Después de la aprobación administrativa, enviar un JPEG o PNG de hasta 10 MiB en el campo multipart `archivo`.

```powershell
curl.exe -H "Authorization: Bearer $($sesion.token)" -F 'archivo=@C:/ruta/imagen.png' "$base/pagos/$reservaId/comprobante"
```
