# Junta de Vecinos Población Zaror

Aplicación móvil desarrollada para la asignatura **Desarrollo de Aplicaciones Móviles (DSY1105)** de Duoc UC.

**Desarrollador:** Cristopher Guerra Vergara  
**Evaluación:** Evaluación Parcial 2 — Primera entrega  
**Plataforma:** Android  
**Lenguaje:** Kotlin

## 1. Descripción del proyecto

El proyecto consiste en una aplicación Android destinada a facilitar la administración de los espacios comunitarios, reservas y movimientos financieros de la Junta de Vecinos Población Zaror.

La aplicación permite que los residentes soliciten reservas de espacios comunitarios, que la Administración gestione usuarios y solicitudes, que Tesorería revise comprobantes y registre movimientos financieros, y que Directiva consulte informes sobre los ingresos, gastos y balance de la comunidad.

El proyecto utiliza datos ficticios con fines académicos y de demostración.

## 2. Tecnologías utilizadas

- **Android Studio:** entorno de desarrollo.
- **Kotlin:** lenguaje de programación.
- **Jetpack Compose:** construcción de interfaces.
- **Material Design 3:** componentes visuales.
- **MVVM:** separación entre interfaz, lógica y datos.
- **Navigation Compose:** navegación entre pantallas.
- **StateFlow y MutableStateFlow:** gestión de estados en ViewModels.
- **mutableStateListOf y mutableStateMapOf:** colecciones observables.
- **DataStore Preferences:** almacenamiento de preferencias.
- **Coil:** visualización de imágenes mediante `AsyncImage`.
- **Activity Result Contracts:** integración con cámara y galería.
- **FileProvider:** gestión de URI para fotografías.
- **Corrutinas:** ejecución de operaciones asíncronas y simulación de carga.
- **JUnit:** pruebas unitarias.
- **Git y GitHub:** control de versiones.

## 3. Arquitectura del proyecto

Se utiliza una estructura basada en MVVM (*Model-View-ViewModel*), organizada en los siguientes paquetes:

| Paquete | Responsabilidad |
|---|---|
| `model` | Modelos de datos, enumeraciones y estados de formularios. |
| `repository` | Administración de datos y operaciones sobre colecciones. |
| `viewmodel` | Validaciones, lógica de presentación y gestión de estados. |
| `ui.screens` | Pantallas desarrolladas con Jetpack Compose. |
| `ui.components` | Componentes visuales reutilizables. |
| `ui.theme` | Configuración de estilos y tema visual. |
| `navigation` | Definición de rutas. |
| `utils` | Funciones auxiliares para archivos e imágenes. |

La aplicación utiliza repositorios compartidos para comunicar las operaciones de los distintos perfiles durante la ejecución.

## 4. Perfiles y funcionalidades

### 4.1. Residente

- Iniciar y cerrar sesión.
- Consultar espacios comunitarios.
- Filtrar espacios habilitados.
- Solicitar reservas indicando fecha y horario.
- Consultar únicamente sus propias reservas.
- Visualizar el estado de aprobación de sus solicitudes.
- Adjuntar comprobantes mediante cámara o galería.
- Consultar el estado de revisión de sus comprobantes.
- Reemplazar comprobantes rechazados.

### 4.2. Administrador

- Iniciar y cerrar sesión.
- Consultar las cuentas existentes.
- Crear usuarios con nombre, correo, contraseña y rol.
- Editar nombre y correo de usuarios.
- Habilitar y deshabilitar cuentas.
- Impedir el ingreso de cuentas deshabilitadas.
- Consultar solicitudes de reserva.
- Aprobar o rechazar solicitudes pendientes.

### 4.3. Tesorería

- Iniciar y cerrar sesión.
- Consultar comprobantes enviados por residentes.
- Visualizar fotografías de los comprobantes.
- Confirmar o rechazar comprobantes.
- Registrar gastos comunitarios con descripción, monto, fecha y categoría.
- Registrar ingresos manualmente con descripción, monto y fecha.
- Consultar los ingresos registrados.

### 4.4. Directiva

- Iniciar y cerrar sesión.
- Consultar el informe financiero.
- Visualizar el total de ingresos registrados.
- Visualizar el total de gastos registrados.
- Consultar el balance calculado a partir de ambos totales.

## 5. Espacios comunitarios

La aplicación contempla tres espacios:

1. Quincho.
2. Piscina.
3. Sala multiuso.

Cada espacio contiene un identificador, nombre, capacidad máxima y estado de habilitación.

Los residentes pueden solicitar reservas de espacios habilitados.

## 6. Funcionamiento de las reservas

El proceso implementado es el siguiente:

1. El residente selecciona un espacio habilitado.
2. Ingresa la fecha y el horario.
3. El sistema valida los datos ingresados.
4. Se registra una solicitud con estado `PENDIENTE`.
5. El Administrador revisa la solicitud.
6. El Administrador cambia su estado a `APROBADA` o `RECHAZADA`.
7. Cuando la reserva está aprobada, el residente puede adjuntar un comprobante.
8. Al adjuntar el comprobante, su estado cambia a `EN_REVISION`.
9. Tesorería revisa la fotografía y confirma o rechaza el comprobante.
10. El residente puede consultar el resultado de la revisión.

**Estados de reserva:** `PENDIENTE`, `APROBADA`, `RECHAZADA` y `CANCELADA`.

**Estados de pago:** `PENDIENTE`, `EN_REVISION`, `CONFIRMADO` y `RECHAZADO`.

La aprobación de reservas se realiza manualmente por el Administrador.

## 7. Gestión financiera

Tesorería registra manualmente los movimientos financieros.

### Ingresos

Cada ingreso contiene:

- Identificador.
- Descripción.
- Monto.
- Fecha.

### Gastos

Cada gasto contiene:

- Identificador.
- Descripción.
- Monto.
- Fecha.
- Categoría.

Los gastos e ingresos se almacenan en repositorios observables durante la ejecución.

### Cálculo del balance

La aplicación calcula:

**Balance = Total de ingresos registrados − Total de gastos registrados**

Los cálculos se actualizan a partir de los movimientos ingresados y se presentan en el informe financiero de Directiva.

Los ingresos todavía no están asociados automáticamente a las reservas ni a sus comprobantes. Su registro es independiente y manual.

## 8. Formularios y validaciones

La aplicación incorpora validaciones para los principales formularios.

Entre las validaciones implementadas se encuentran:

- Campos obligatorios.
- Formato de correo electrónico.
- Contraseñas con longitud mínima al crear usuarios.
- Comprobación de correos duplicados.
- Fechas válidas.
- Restricción de fechas pasadas en reservas.
- Restricción de fechas futuras en movimientos financieros.
- Horarios permitidos para reservas.
- Validación de montos numéricos mayores que cero.
- Selección de categorías para gastos.

Los formularios muestran mensajes cuando los datos ingresados no cumplen las condiciones establecidas.

## 9. Recursos nativos de Android

Se implementaron dos recursos nativos:

**Cámara:** permite capturar fotografías para adjuntarlas como comprobantes de pago.

**Galería:** permite seleccionar fotografías existentes en el dispositivo.

Para su integración se utilizan `ActivityResultContracts`, `FileProvider` y URI. Las imágenes se visualizan mediante Coil y `AsyncImage`.

## 10. Estados y animaciones

Se incorporaron componentes para mejorar la interacción:

- `Crossfade`: animación de cambios de estado de pago.
- `AnimatedVisibility`: aparición y desaparición de controles.
- `CircularProgressIndicator`: indicador de carga durante la confirmación simulada de comprobantes.
- `StateFlow`: actualización de formularios.
- Colecciones observables de Compose: actualización de listas y datos.

## 11. Cuentas de demostración

| Perfil | Correo | Contraseña |
|---|---|---|
| Residente | residente@zaror.cl | Demo1234! |
| Administrador | admin@zaror.cl | Demo1234! |
| Tesorería | tesoreria@zaror.cl | Demo1234! |
| Directiva | directiva@zaror.cl | Demo1234! |

Estas cuentas son exclusivamente de demostración y no deben utilizarse como credenciales reales.

El Administrador también puede crear nuevas cuentas durante la ejecución de la aplicación.

## 12. Instrucciones de ejecución

1. Clonar el repositorio desde GitHub.
2. Abrir el proyecto en Android Studio.
3. Configurar un JDK compatible. El proyecto se ha trabajado utilizando JDK 21.
4. Sincronizar las dependencias de Gradle.
5. Seleccionar un emulador Android compatible.
6. Ejecutar la aplicación mediante **Run**.
7. Iniciar sesión con alguna de las cuentas de demostración.

**Emulador utilizado durante el desarrollo:** Google Pixel 4.

## 13. Persistencia y limitaciones actuales

La aplicación utiliza DataStore Preferences para conservar la preferencia de visualización de espacios habilitados.

Los usuarios, reservas, comprobantes, ingresos y gastos utilizan principalmente almacenamiento temporal en memoria. Por lo tanto, estos datos pueden perderse cuando finaliza el proceso de la aplicación.

Otras limitaciones de esta entrega:

- La disponibilidad de horarios todavía no contempla una comprobación completa de superposición entre reservas.
- La aprobación de reservas es manual.
- Los ingresos se registran manualmente y no se vinculan con reservas.
- Los informes representan los movimientos registrados en la aplicación, no necesariamente la situación contable real de la comunidad.
- El sistema de autenticación utiliza cuentas y contraseñas de demostración y no está preparado para producción.

## 14. Mejoras futuras

- Implementar persistencia local de los datos de negocio mediante una base de datos.
- Validar automáticamente la disponibilidad de horarios y evitar reservas superpuestas.
- Asociar ingresos con reservas y comprobantes de pago.
- Automatizar determinadas operaciones de gestión de reservas.
- Implementar un control más completo de plazos y vencimientos de pagos.
- Mejorar los controles de autenticación y seguridad.
- Ampliar las pruebas automatizadas.
- Continuar mejorando las interfaces y la experiencia de usuario.

## 15. Control de versiones

El proyecto se desarrolla utilizando Git y GitHub, con commits que documentan los avances de implementación y corrección.

Repositorio:

https://github.com/Newfilim/ProyectoDesarrolloMobile

---

**Proyecto académico — Desarrollo de Aplicaciones Móviles, Duoc UC — 2026.**