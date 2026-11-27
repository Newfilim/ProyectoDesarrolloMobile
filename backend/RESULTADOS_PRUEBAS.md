# Resultados de pruebas del backend

## Entorno

- Rama `feature/backend`, origen `Newfilim/ProyectoDesarrolloMobile`. El árbol estaba limpio al comenzar.
- Java 21.0.10, Maven 3.9.15 mediante Maven Wrapper 3.3.4 y mongosh 2.14.2.
- La instancia instalada en Windows respondía como MongoDB 9.0.8 standalone en 27017. Windows bloqueó el arranque manual de ese ejecutable. Se dejó intacta y se ejecutó MongoDB Community 9.0.8 en WSL Ubuntu 24.04, con puerto 27187, directorio temporal y replica set de un nodo `rs0`.
- Las pruebas de integración crean bases `zaror_test_<sufijo>` y un directorio temporal de comprobantes. El servidor de pruebas se configuró para inyectar fallos en comandos seleccionados mediante `enableTestCommands`. Ninguna prueba modifica `zaror_db` ni bases de otros proyectos.

## Comandos

Desde `backend/`, con el replica set de pruebas iniciado y primario:

```powershell
$env:ZAROR_TEST_MONGO_URI = 'mongodb://127.0.0.1:27187/zaror_test?replicaSet=rs0'
$env:ZAROR_TEST_FAILPOINTS = 'true'
.\mvnw.cmd -B -ntp clean verify
```

Sin esas variables, `clean verify` compila y ejecuta las pruebas unitarias. Las pruebas con la instancia real de MongoDB se omiten. Los reportes se generan bajo `target/surefire-reports`.

## Resultados

La compilación Java 21 y el empaquetado del JAR Spring Boot se completaron. Pasaron 4 pruebas JUnit, sin fallos, errores ni omisiones en la ejecución con MongoDB.

| Verificación | Evidencia |
| --- | --- |
| Horario | Dos pruebas unitarias cubren días ordinarios, turnos de viernes y sábado, cambios de hora, límites exactos y duración real de bloques. |
| Inicio y persistencia | La prueba de integración verifica seis colecciones, datos dev, reinicio con cuentas editadas, ID estable, reservas persistentes y los 29 métodos de OpenAPI por HTTP en Tomcat. Otra prueba arranca sin perfil dev, comprueba que no hay datos semilla y que los totales y categorías parten vacíos. |
| Seguridad | MockMvc con JWT firmado, firma incorrecta, expiración, emisor o audiencia incorrectos, ausencia de token, credenciales incorrectas, propiedad de recurso, cuenta deshabilitada y permisos de los cuatro perfiles. Dos solicitudes simultáneas para deshabilitar administradores dejan un administrador activo. |
| Reservas | Dos solicitudes concurrentes con rangos parcialmente superpuestos producen 201 y 409. Se verifica el índice único en MongoDB y que solo queden la reserva ganadora y sus tres bloqueos. También se comprueban rangos adyacentes, otro espacio, turno nocturno, datos manipulados y carrera con deshabilitación del espacio. |
| Transacciones | Un fallo inyectado por MongoDB al eliminar los bloqueos de una reserva aprobada devuelve 503. La reserva conserva su estado, el pago y los tres bloqueos. Al repetir sin el fallo, la operación libera los horarios. |
| Comprobantes | MockMvc cubre carga y reenvío PNG/JPEG, contenido inválido, tamaño, archivo vacío, nombre con ruta, autorización, carreras de revisión e impago. Un fallo inyectado al actualizar la referencia conserva el comprobante anterior y retira el nuevo. |
| Finanzas | Se comprobaron los totales iniciales 300000, 160000 y 140000, el orden de categorías, montos inválidos, saldos en cero y negativos. Confirmar un comprobante deja igual la cantidad de ingresos. |
| Alcance del repositorio | La revisión de Git muestra cambios únicamente en `backend/`, con Android y la documentación existente intactos. |

### Prueba HTTP del JAR

Se ejecutó `java -jar target/backend-1.0.0.jar` en 127.0.0.1:8080 con una base de pruebas nueva. Se comprobó OpenAPI, inicio de sesión en tres perfiles, balance inicial, disponibilidad, registro de reserva, aprobación, subida multipart PNG sobre Tomcat y confirmación por Tesorería. Volver a consultar el balance devolvió el valor inicial de 140000.

### Escenarios sin ejecución

Se inyectaron errores de comandos con respuesta conocida. Quedan sin reproducir una pérdida de red durante el commit y una caída simultánea de MongoDB y el filesystem. Android se usó como referencia de lectura. En esta etapa no se realizan pruebas del cliente contra la API.

## Incidencias corregidas durante la verificación

- Se ajustó el auxiliar MockMvc al tipo genérico compartido para pruebas JSON y multipart de Spring MVC 7.
- Se sustituyó una opción de deserialización que Jackson 3 ya no reconoce por una configuración explícita de coerciones.
- Se ajustó el reloj fijo para que el primer bloque del escenario de disponibilidad ya hubiese empezado y el segundo permaneciera disponible.

## Revisión de mantenibilidad

- Se sustituyó el acceso compartido a MongoDB por repositorios de usuarios, espacios, reservas, bloqueos, ingresos y gastos. El repositorio de totales reúne las agregaciones que consultan movimientos. Autenticación, usuarios y espacios tienen servicios separados. Se conservaron el lock compartido, los filtros de actualización y los límites de las transacciones.
- Se mantuvieron las comprobaciones existentes y se repartieron en escenarios de autenticación y usuarios, reservas y pagos, y finanzas y reinicio. Cada prueba de integración carga un contexto con una base de sufijo único y un directorio temporal de archivos.
- Se añadieron escenarios para recuperar colecciones vacías tras una interrupción o un arranque sin perfil dev. Una base con documentos rechaza la recuperación explícita y conserva los registros previos. La [guía local](CONFIGURACION_LOCAL.md#recuperar-la-semilla-de-desarrollo) describe el indicador de recuperación y cuándo usarlo.
- La prueba unitaria del registro de errores comprueba que el evento contenga la operación, el tipo y el código de MongoDB, sin incluir los mensajes de excepción ni adjuntar la traza completa.
- Spring Data no combinó de forma automática los dos límites de la consulta derivada de disponibilidad sobre `inicio`. Se definió una consulta explícita con los dos operadores sobre el mismo campo y se mantuvo el intervalo original.

El 27/11/2026 se ejecutó de nuevo `mvnw.cmd clean verify` con MongoDB Community 9.0.8 en un replica set `rs0` nuevo y aislado en WSL (puerto 27187), con `ZAROR_TEST_FAILPOINTS=true`. El resultado fue **3 pruebas unitarias y 6 de integración aprobadas**, sin fallos, errores ni omisiones.

Se volvieron a ejecutar las carreras de reservas, deshabilitación de espacios y administradores, revisión de pagos e impago. La reserva perdedora quedó sin documentos parciales y el fallo inyectado al borrar bloqueos conservó tanto la reserva aprobada como el pago y los tres bloqueos. También se volvió a comprobar que un fallo de MongoDB durante un reenvío conserva el comprobante anterior. Git confirmó que el trabajo se limitó a `backend/`.
