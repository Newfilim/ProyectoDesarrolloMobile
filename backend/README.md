# Backend Zaror

API REST local para usuarios, espacios, reservas, comprobantes y finanzas de la Junta de Vecinos Población Zaror. Proyecto Maven independiente con Java 21, Spring Boot 4.0.10, Lombok y MongoDB. La integración Android queda para una etapa posterior.

## Arranque

1. Sigue la [configuración local](CONFIGURACION_LOCAL.md) para iniciar MongoDB con el replica set `rs0` y definir las variables.
2. Desde `backend/`, ejecuta `.\mvnw.cmd spring-boot:run`.
3. Abre [Swagger UI](http://127.0.0.1:8080/swagger-ui/index.html) en perfil `dev`. Inicia sesión con una de las cuentas de desarrollo y usa el botón **Authorize** con el token devuelto.
4. Consulta el [contrato HTTP](CONTRATO_API.md) y los [resultados de pruebas](RESULTADOS_PRUEBAS.md).

La aplicación comprueba la base, las colecciones, los índices y la capacidad de escritura transaccional al iniciar. El perfil `dev` carga la semilla en una base nueva. La cuenta de inicio utiliza la contraseña configurada localmente.

Si una base ya tiene colecciones vacías, consulta la [recuperación explícita de la semilla](CONFIGURACION_LOCAL.md#recuperar-la-semilla-de-desarrollo). Los arranques normales preservan los datos existentes.

## Decisiones técnicas

- Autenticación, usuarios y espacios tienen servicios separados. Las consultas viven en repositorios por documento. Se usa `MongoRepository` para bloqueos y movimientos financieros, y `MongoTemplate` dentro de repositorios para escrituras condicionales, operaciones que devuelven el documento actualizado y agregaciones. El servicio de reservas conserva los límites de las transacciones y el lock compartido.

- Springdoc 3.0.3 pertenece a la serie 3.0.x que la matriz oficial asigna a Spring Boot 4.0.x. Se fija la versión en el POM y el resto se gestiona con el BOM de Boot. [Matriz de Springdoc](https://springdoc.org/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot), [publicación de Spring Boot](https://spring.io/blog/2026/11/20/spring-boot-4-0-10-available-now/).
- Las reservas usan bloques de 60 minutos con límites exactos de America/Santiago. En cambios de hora se omiten los bloques con límites ambiguos o inexistentes, o con duración distinta de 60 minutos reales. Los turnos nocturnos conservan su fecha operativa.
- La reserva y sus bloqueos se escriben en una transacción. El índice único protege el horario. El lock de la instancia coordina cambios de cuentas o espacios con operaciones críticas sobre reservas hasta que MongoDB devuelve el resultado del commit. Un error transitorio etiquetado permite hasta tres intentos. Un resultado incierto de commit devuelve 503 y no repite la reserva. Consulta el listado para reconciliar el resultado antes de reintentar.
- Los comprobantes están fuera de las colecciones y se sirven desde rutas autenticadas. MongoDB y el filesystem no comparten una transacción. Se limpia un archivo nuevo si la escritura condicional definitivamente no se aplicó. Ante un resultado incierto se comprueba la referencia y se conserva el archivo si no es seguro retirarlo. Una caída puede dejar archivos huérfanos. Una limpieza operacional debe comparar los nombres con las referencias de MongoDB.
- El JWT expira a las ocho horas. En cada solicitud se verifica el rol y si la cuenta actual sigue activa. El cierre de sesión consiste en descartar el token del cliente. Un token emitido conserva vigencia hasta expirar mientras la cuenta permanezca activa. Para acceso externo al equipo local se requiere HTTPS.
