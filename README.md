# Junta de Vecinos Población Zaror

Aplicación Android desarrollada para **Desarrollo de Aplicaciones Móviles (DSY1105), Duoc UC**, Evaluación Parcial 2 (2026).

**Repositorio:** https://github.com/Newfilim/ProyectoDesarrolloMobile  
**Desarrollador identificado:** Cristopher Guerra Vergara  
**Tecnologías principales:** Kotlin, Jetpack Compose, Material 3, MVVM y Room.

> Proyecto académico con cuentas y movimientos ficticios. No está diseñado para producción ni para gestionar dinero real.

## 1. Objetivo

Apoyar la gestión de reservas de espacios comunitarios y movimientos financieros de la **Junta de Vecinos Población Zaror**. La aplicación diferencia cuatro perfiles de uso: Residente, Administrador, Tesorería y Directiva.

## 2. Funcionalidades por perfil

| Perfil | Funcionalidades |
| --- | --- |
| **Residente** | Inicio de sesión; consulta y filtrado de espacios; solicitud de reservas con fecha y horario; seguimiento de solicitudes; envío y reenvío de comprobantes con cámara o galería; consulta de su estado. |
| **Administrador** | Consulta, creación y edición de cuentas de demostración; habilitación/deshabilitación de usuarios y espacios; aprobación y rechazo de reservas; comprobación de superposición de horarios aprobados. |
| **Tesorería** | Revisión, confirmación y rechazo de comprobantes; registro manual de ingresos y gastos comunitarios. |
| **Directiva** | Consulta de ingresos, gastos, balance y gráficos financieros. |

**Espacios iniciales:** Quincho (25 personas), Sala multiuso (40) y Piscina (60). Su disponibilidad se administra desde la aplicación.

## 3. Flujo de reservas y pagos

1. El residente selecciona un espacio habilitado, una fecha y un horario. Se validan el formulario, las fechas y los horarios permitidos.
2. Se guarda una reserva `PENDIENTE` en Room.
3. Administración la aprueba (`APROBADA`) o rechaza (`RECHAZADA`). Al aprobar, comprueba que no exista otra reserva **aprobada** del mismo espacio con horario superpuesto, incluidos horarios que cruzan medianoche.
4. Una reserva aprobada genera un pago `PENDIENTE` asociado a ella.
5. El residente sube una fotografía del comprobante y su estado pasa a `EN_REVISION`.
6. Tesorería revisa la imagen y cambia el pago a `CONFIRMADO` o `RECHAZADO`. Un comprobante rechazado puede reenviarse.
7. Tesorería **registra manualmente** el monto recibido en la pantalla de ingresos. Confirmar el comprobante **no genera automáticamente un ingreso contable**.

**Estados de reserva:** `PENDIENTE`, `APROBADA`, `RECHAZADA`, `CANCELADA`.  
**Estados de pago:** `PENDIENTE`, `EN_REVISION`, `CONFIRMADO`, `RECHAZADO`.

**Horario permitido:** domingo a jueves, de 08:00 a 22:00; viernes y sábado, desde las 08:00 hasta las 02:00 del día siguiente.

## 4. Arquitectura MVVM y POO

```text
app/src/main/java/com/example/gestionpsdm/
├── model/          Modelos, estados de formularios y enumeraciones
├── data/local/     Entidades, DAO, mappers, Room y migraciones
├── repository/     Acceso y transformación de datos
├── viewmodel/      Validaciones, estado observable y operaciones de UI
├── ui/
│   ├── screens/    Pantallas Jetpack Compose
│   ├── components/ Componentes reutilizables y gráficos
│   └── theme/      Tema, colores y tipografía
├── navigation/     Rutas
├── utils/          Utilidades para imágenes y archivos
└── MainActivity.kt
```

El recorrido de acceso a datos es **Pantalla → ViewModel → Repository → DAO → Room**. Se utilizan `Flow` y `StateFlow` para reflejar cambios en la interfaz.

La programación orientada a objetos se aplica mediante clases de modelo como `Usuario`, `Espacio`, `Reserva`, `Pago`, `Gasto` e `Ingreso`, con atributos, métodos de negocio y enumeraciones (`RolUsuario`, `EstadoReserva`, `EstadoPago`, `CategoriaGasto`). No se crean subclases artificiales para tipos de espacio o rol que comparten estructura y comportamiento.

## 5. Persistencia local

La base **`zaror_database`** utiliza **Room / SQLite**, actualmente en versión **4**, con migraciones `1→2`, `2→3` y `3→4`.

| Tabla | Almacena |
| --- | --- |
| `espacios` | Identificadores, capacidades y disponibilidad |
| `reservas` | Solicitudes, fechas, horarios y aprobación |
| `pagos` | Estados y referencia al comprobante |
| `ingresos` | Movimientos ingresados manualmente |
| `gastos` | Movimientos y categorías de gasto |

Las imágenes de comprobantes se copian al almacenamiento privado de la aplicación para mantener el acceso tras reiniciarla. **DataStore Preferences** conserva la preferencia de filtro de espacios habilitados.

**Excepción importante:** las cuentas de demostración y las modificaciones realizadas en Gestión de usuarios se mantienen en memoria (`UsuariosDemoRepository`); no persisten en Room al finalizar el proceso de la aplicación.

Las instalaciones nuevas reciben espacios y algunos movimientos financieros **ficticios** de ejemplo. No equivalen a dinero real recibido o gastado.

## 6. Formularios, interacción y animaciones

- Formularios de acceso, gestión de usuarios, reservas, ingresos y gastos con validaciones y mensajes de error.
- Comprobación de campos obligatorios, correos, contraseñas, fechas y horas, montos y categorías según el formulario.
- Navegación entre pantallas por rol, tarjetas, controles de estado y retroalimentación visual.
- **`AnimatedVisibility`** y **`Crossfade`** en la presentación de controles y estados; indicadores de procesamiento para acciones asíncronas.
- Gráficos de ingresos, gastos por categoría y balance financiero.

## 7. Recursos nativos Android

- **Cámara:** `ActivityResultContracts.TakePicture()` y `FileProvider` para capturar comprobantes.
- **Galería / selector de imágenes:** `ActivityResultContracts.GetContent()` para seleccionar fotografías existentes.
- **Coil (`AsyncImage`)** muestra las imágenes en la aplicación. Los comprobantes se copian a archivos privados para conservarlos.

## 8. Herramientas y bibliotecas

- **Android Studio**, Kotlin y Gradle.
- **Jetpack Compose**, Material Design 3 y Navigation Compose.
- **ViewModel**, corrutinas, `Flow`, `StateFlow` y MVVM.
- **Room**, SQLite y KSP.
- **DataStore Preferences**, Coil y Activity Result Contracts.
- **JUnit**, Git y GitHub.

Las versiones específicas se declaran en `gradle/libs.versions.toml` y `app/build.gradle.kts`.

## 9. Cuentas de demostración

| Perfil | Correo | Contraseña |
| --- | --- | --- |
| Residente | `residente@zaror.cl` | `Demo1234!` |
| Administrador | `admin@zaror.cl` | `Demo1234!` |
| Tesorería | `tesoreria@zaror.cl` | `Demo1234!` |
| Directiva | `directiva@zaror.cl` | `Demo1234!` |

Estas credenciales están incluidas **solo para demostración académica**.

## 10. Instalación y ejecución

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/Newfilim/ProyectoDesarrolloMobile.git
   ```

2. Abrir la carpeta `ProyectoDesarrolloMobile` en **Android Studio**.
3. Seleccionar un JDK compatible con Gradle; durante el desarrollo se utilizó **JDK 21** para sincronizar.
4. Ejecutar **Gradle Sync** y esperar la descarga de dependencias.
5. Iniciar un emulador o dispositivo Android compatible (**minSdk 24**); durante el desarrollo se utilizó un **Pixel 4 virtual**.
6. Ejecutar la configuración **app** con **Run** e iniciar sesión con una cuenta de demostración.

Para comprobar la persistencia, cerrar y reabrir la aplicación **sin desinstalarla ni borrar sus datos**.

## 11. Pruebas

El proyecto contiene pruebas locales con **JUnit** para modelos y se ha comprobado manualmente el flujo de navegación entre los cuatro roles.

Recorrido de verificación:

1. Administrador: habilitar/deshabilitar espacios y comprobar el estado tras reiniciar.
2. Residente: crear una reserva válida y comprobar errores de fecha y horario.
3. Administrador: aprobar una reserva e intentar aprobar otra con horario superpuesto.
4. Residente: subir un comprobante desde galería y desde cámara.
5. Tesorería: rechazar, recibir un nuevo comprobante y confirmarlo.
6. Tesorería: registrar ingresos y gastos manualmente.
7. Directiva: consultar los totales y gráficos; reiniciar para comprobar la persistencia.

## 12. Alcance y limitaciones

- Aplicación **local** de demostración: no existe backend ni sincronización entre dispositivos.
- Autenticación y perfiles mediante cuentas ficticias en memoria; no constituyen un sistema seguro de usuarios para producción.
- Los ingresos se registran manualmente; confirmar un comprobante no acredita automáticamente un ingreso monetario.
- Los conflictos de horario se comprueban al **aprobar** reservas. Una solicitud pendiente no bloquea por sí sola la franja horaria.
- No está implementado el vencimiento automático de pagos a los tres días ni una tarifa obligatoria por espacio.
- Los informes muestran movimientos registrados en la aplicación, incluidos datos iniciales ficticios.

## 13. Control de versiones y planificación

Repositorio con historial de cambios: **https://github.com/Newfilim/ProyectoDesarrolloMobile**.

La planificación del equipo y su distribución de tareas se presentan mediante Trello o la herramienta colaborativa utilizada. **El enlace específico del tablero debe añadirse cuando esté disponible**; no está incluido en la copia revisada.

---

**Proyecto académico — Duoc UC, 2026.**
