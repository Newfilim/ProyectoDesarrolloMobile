# Configuración local en Windows

Requiere Java 21, MongoDB con soporte de transacciones y `mongosh`. El Maven Wrapper descarga Maven 3.9.15. Comprueba el entorno con `java -version`, `mongod --version` y `mongosh --version`. Si mongod está fuera de PATH, usa la ruta completa del binario instalado.

## Replica set

Desde `backend/` usa un directorio privado y nuevo. El ejemplo utiliza 27187 para evitar interferir con instalaciones existentes. Mantén esta consola abierta.

```powershell
$datos = Join-Path (Get-Location).Path '.local/mongo-rs0'
New-Item -ItemType Directory -Path $datos -Force | Out-Null
mongod --dbpath "$datos" --port 27187 --bind_ip 127.0.0.1 --replSet rs0
```

En otra consola, ejecuta `rs.initiate` una sola vez para ese directorio de datos. Espera a que `isWritablePrimary` sea `true`.

```powershell
mongosh 'mongodb://127.0.0.1:27187/admin?directConnection=true' --eval 'rs.initiate({_id:''rs0'',members:[{_id:0,host:''127.0.0.1:27187''}]})'
mongosh 'mongodb://127.0.0.1:27187/admin?directConnection=true' --eval 'printjson(db.hello())'
```

El puerto 27017 también sirve si está libre o ya pertenece a tu replica set de Zaror. Usa el mismo puerto en la URI y en el miembro del replica set. No reconfigures ni detengas una instancia de otro proyecto. Si Windows bloquea el binario, pide habilitar la instalación o usa un replica set local autorizado.

## Variables y arranque

Desde `backend/` define valores de sesión en PowerShell. Conserva la clave generada en un almacén privado si quieres mantener tokens entre reinicios. Usa un directorio de comprobantes con permisos de lectura/escritura exclusivos de tu usuario.

```powershell
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:MONGODB_URI = 'mongodb://127.0.0.1:27187/zaror_db?replicaSet=rs0'
$env:UPLOAD_DIR = Join-Path (Get-Location).Path '.local/comprobantes'
$clave = New-Object byte[] 32
$generador = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $generador.GetBytes($clave) } finally { $generador.Dispose() }
$env:JWT_SECRET = [Convert]::ToBase64String($clave)
$entrada = Read-Host 'Contraseña de las cuentas de desarrollo' -AsSecureString
$env:ZAROR_DEMO_PASSWORD = [System.Net.NetworkCredential]::new('', $entrada).Password
.\mvnw.cmd spring-boot:run
```

El valor de `ZAROR_DEMO_PASSWORD` debe tener al menos 8 caracteres y hasta 72 bytes UTF-8. Se usa al inicializar una base nueva en `dev`. Los reinicios preservan las cuentas y sus hashes. Sin `dev`, Swagger y la semilla quedan desactivados. El servidor escucha en 127.0.0.1:8080. [Swagger](http://127.0.0.1:8080/swagger-ui/index.html) y [OpenAPI](http://127.0.0.1:8080/v3/api-docs).

## Recuperar la semilla de desarrollo

Si un arranque anterior creó las colecciones y se interrumpió antes de insertar datos, o si la base se creó inicialmente sin `dev`, Zaror puede encontrar colecciones vacías. Como ese estado también es compatible con un vaciado intencional, el arranque normal conserva la base vacía. Si confirmas que es una base de desarrollo que debe recibir los ejemplos, detén otras instancias de Zaror, comprueba que `MONGODB_URI` apunta a esa base y ejecuta una vez con `dev`, `ZAROR_DEMO_PASSWORD` y el indicador temporal.

```powershell
$env:ZAROR_INICIALIZAR_BASE_VACIA = 'true'
try { .\mvnw.cmd spring-boot:run }
finally { Remove-Item Env:ZAROR_INICIALIZAR_BASE_VACIA -ErrorAction SilentlyContinue }
```

Zaror se niega a recuperar la semilla si encuentra documentos o colecciones ajenas al modelo. Para una base con datos parciales, revisa una copia confiable o usa una base de desarrollo separada. No borres las colecciones para forzar la semilla. El indicador debe quedar desactivado para los arranques siguientes.

El registro de diagnósticos indica la operación, el tipo de excepción, su origen en código y, si está disponible, el código de MongoDB. Las respuestas HTTP conservan los mensajes públicos. Mantén los logs de los drivers en su nivel estándar para evitar trazas de documentos y consultas.

## Pruebas

```powershell
$env:ZAROR_TEST_MONGO_URI = 'mongodb://127.0.0.1:27187/zaror_test?replicaSet=rs0'
.\mvnw.cmd clean verify
```

La suite usa una base nueva con prefijo `zaror_test_` y un directorio temporal para comprobantes. Sin la variable se ejecutan las pruebas unitarias y se omite la suite con MongoDB. Los nombres ajenos a Zaror quedan excluidos de la inicialización. Si MongoDB no conecta, verifica el puerto, el nombre del replica set y que el miembro sea primario. Si falla un índice único, revisa por separado cualquier dato duplicado previo. El arranque no borra datos ni índices.

Para incluir las pruebas de fallo controlado, inicia un replica set exclusivo para pruebas con `--setParameter enableTestCommands=1` y define `$env:ZAROR_TEST_FAILPOINTS = 'true'` antes de ejecutar Maven. Las pruebas activan y desactivan el fallo en ese servidor. Usa esta opción únicamente con un nodo aislado.
