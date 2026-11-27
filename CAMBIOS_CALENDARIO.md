# Calendario y disponibilidad de reservas

## Funcionamiento

- El residente elige el espacio desde la pantalla habitual y abre un calendario Material 3. Puede navegar entre meses y seleccionar desde la fecha actual. Si el día seleccionado no tiene cupos, el formulario lo indica y deshabilita el envío.
- Cada bloque dura una hora. De domingo a jueves el horario es de 08:00 a 22:00. Los viernes y sábados se extiende hasta las 02:00 del día siguiente. Los bloques de 00:00 a 02:00 aparecen al final del turno que los origina. Si la reserva empieza después de medianoche, Room guarda la fecha real de inicio. El resumen muestra las fechas reales de ambos extremos.
- El calendario agrupa la madrugada con su turno, por lo que deja de ofrecer los bloques de ese turno cuando la fecha operativa pasa a ser anterior a hoy. En una transición de zona horaria, el formulario solo ofrece intervalos que se pueden representar con el formato existente y que duran una hora real.
- El residente toca un bloque para empezar y puede tocar otro para extender el rango. El formulario incluye los bloques intermedios solo si todos están disponibles y son consecutivos. Tocar un bloque de la selección reduce el rango a ese bloque. También se puede limpiar la selección.
- La disponibilidad se vuelve a calcular al cambiar de fecha o de espacio, al volver a la pantalla y cuando Room emite un cambio de reservas o del estado del espacio. El reloj de la pantalla actualiza los bloques vencidos cada minuto. Las fechas se interpretan en la zona horaria del dispositivo.
- La selección se mantiene en el formulario hasta el envío. Room vuelve a comprobar, dentro de una transacción, que el espacio esté habilitado, la hora siga disponible y no haya superposición. Dos intervalos que solo comparten un extremo pueden reservarse. `PENDIENTE` y `APROBADA` ocupan horas. `RECHAZADA` y `CANCELADA` las liberan.
- Al aprobar se excluye la propia solicitud y se crea el pago pendiente. Si hay solicitudes antiguas superpuestas, se conservan sus datos y se impide aprobarlas mientras exista el conflicto. Administración puede rechazar una solicitud para resolverlo.
- Administración puede rechazar definitivamente una reserva aprobada cuando el pago está pendiente, el comprobante fue rechazado o la aprobación antigua no tiene un pago asociado. Si existe un pago, se conserva su registro y su comprobante y se cierra en el estado `RECHAZADO`. Los pagos en revisión o confirmados quedan fuera de esa acción. El rechazo de un comprobante por Tesorería conserva la reserva aprobada y permite reenviarlo.
- La disponibilidad corresponde a la base local de la instalación. El proyecto ya tenía el estado `CANCELADA` pero carecía de una acción de cancelación. La prueba de liberación cambia ese estado directamente en Room.

## Archivos creados

| Archivo | Motivo |
| --- | --- |
| `model/BloqueHorario.kt` | Representa un bloque con inicio, término y disponibilidad. |
| `utils/HorariosReserva.kt` | Centraliza horarios de funcionamiento, fechas reales y comparación de intervalos. |
| `test/.../HorariosReservaTest.kt` | Verifica turnos, medianoche, adyacencia y selección de rangos. |
| `androidTest/.../DisponibilidadReservasTest.kt` | Verifica transacciones de Room, concurrencia, persistencia y transiciones de reserva y pago. |
| `CAMBIOS_CALENDARIO.md` | Documenta la implementación y su verificación. |

## Archivos modificados

Las rutas de Kotlin de producción son relativas a `app/src/main/java/com/example/gestionpsdm`.

| Archivo | Motivo |
| --- | --- |
| `ui/screens/SolicitarReservaScreen.kt` | Integra calendario, bloques, resumen y envío de la solicitud. |
| `viewmodel/ReservaViewModel.kt` | Observa la fecha seleccionada, actualiza la disponibilidad y coordina el guardado. |
| `model/ReservaFormularioUiState.kt` | Conserva los bloques de la fecha y valida la continuidad de la selección. |
| `repository/ReservasRoomRepository.kt` | Calcula disponibilidad y comprueba conflictos al registrar y aprobar. Coordina el cierre de la reserva y del pago por impago en una transacción. |
| `data/local/ReservaDao.kt` | Lee y observa reservas vigentes por espacio y permite actualizar su estado. |
| `data/local/EspacioDao.kt` | Observa si el espacio sigue habilitado. |
| `data/local/PagoDao.kt` | Consulta reservas aprobadas sin pago, cierra pagos impagos y valida que las operaciones de comprobantes correspondan a reservas aprobadas. |
| `viewmodel/GestionReservasViewModel.kt` | Expone las reservas aprobadas elegibles para rechazo por impago. |
| `ui/screens/GestionReservasScreen.kt` | Permite confirmar el rechazo por impago e informa sobre solicitudes incompatibles. |
| `ui/screens/RevisarPagosScreen.kt` | Distingue un rechazo definitivo de reserva del rechazo de un comprobante. |

Room conserva la versión 4. El cálculo usa las tablas y columnas existentes. Las nuevas consultas no requieren migración.

## Compilación y pruebas

- `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --offline` terminó correctamente.
- Pasaron las 6 pruebas unitarias, incluidas 3 nuevas.
- El APK de pruebas instrumentadas compiló y pasaron las 4 pruebas en el emulador Pixel 4 con Android 35 mediante `AndroidJUnitRunner`. Las 3 pruebas nuevas cubren concurrencia, cambios de disponibilidad al rechazar o cancelar, persistencia al reabrir Room, turnos que cruzan medianoche, solicitudes antiguas incompatibles y los estados de comprobantes.
- Queda pendiente recorrer la interfaz por perfil y usar cámara o galería con un comprobante real. La guía siguiente recoge esas verificaciones.

## Guía de verificación manual en Android Studio

1. Como **Residente**, abre un espacio habilitado y navega por el calendario. Comprueba que un día pasado no se puede elegir y que los bloques de hoy cuyo inicio ya pasó aparecen deshabilitados.
2. Elige una fecha futura y toca un bloque. Extiende el rango, redúcelo y límpialo. Prueba un viernes o sábado entre 23:00 y 02:00 y revisa las fechas del resumen. Envía una solicitud y comprueba que aparece pendiente en **Mis reservas**.
3. Vuelve al calendario desde otro residente de la misma instalación. Intenta elegir el bloque ocupado y extender un rango que lo atraviese. Prueba un bloque adyacente, la misma hora en otro espacio habilitado y un espacio deshabilitado. Ocupa un turno completo y comprueba que el formulario indique que ya no quedan horarios para esa fecha. Cierra la app, ábrela y revisa la disponibilidad nuevamente.
4. Como **Administrador**, aprueba una solicitud y rechaza otra en **Gestión de reservas**. Al volver como residente, verifica que la aprobada sigue ocupando sus horas y la rechazada las libera.
5. En una reserva aprobada, sube un comprobante desde **Mis reservas**. Como **Tesorería**, recházalo y verifica que el residente puede reenviarlo y que el horario permanece ocupado. Tras rechazar el comprobante, usa **Rechazar por falta de pago** como administrador y verifica que el horario se libera.
6. Revisa la navegación, el calendario y la disposición de los bloques en el emulador o teléfono. La cancelación se verifica a nivel de Room, ya que la app carece de un botón de cancelación.

## Seguimiento de aceptación y revisión de impagos

El usuario reportó 63 pruebas manuales de aceptación correctas para el calendario, la selección de rangos y el ciclo de reservas. También reportó que Gestión de reservas muestra «Usuario no encontrado» para una cuenta creada desde Administración.

**Causa.** Gestión de usuarios y el inicio de sesión utilizan `UsuariosDemoRepository`. La solicitud guarda en Room el `usuario.id` de la sesión como `idUsuario`. Gestión de reservas y Revisión de pagos buscan ese mismo ID en el mismo repositorio. Las cuentas creadas por Administración existen en una lista en memoria. Tras reiniciar el proceso se reconstruyen las cuatro cuentas de demostración con sus IDs fijos, mientras las reservas siguen en Room. Las nuevas altas usan el mayor ID en memoria más uno, así que tras reiniciar también pueden reutilizar un ID que una reserva antigua ya referencia. Cerrar sesión dentro del mismo proceso conserva la lista de cuentas.

**Resolución pendiente de autorización.** Para resolver la identidad tras un reinicio se requiere persistir las cuentas con IDs estables y evitar reasignar los IDs que ya referencian reservas antiguas. Las reservas cuyo titular desapareció solo guardan su ID. Para reasociarlas se necesita una fuente confiable que identifique al titular. Esta revisión conserva la búsqueda por ID y el esquema actual de Room.

| Archivo modificado en esta revisión | Cambio |
| --- | --- |
| `ui/screens/RevisarPagosScreen.kt` | Muestra las acciones de Tesorería únicamente para comprobantes en revisión de reservas aprobadas. Room ya rechazaba los cambios de pago en reservas que habían dejado de estar aprobadas. |
| `androidTest/.../DisponibilidadReservasTest.kt` | Añade pruebas de la asociación por ID dentro del mismo proceso y de los efectos del rechazo por impago y del estado `CANCELADA` sobre pagos, disponibilidad, ingresos y gastos. |
| `CAMBIOS_CALENDARIO.md` | Registra el hallazgo, el alcance y las verificaciones. |

**Resultados de esta revisión.** Compilaron la app y el APK instrumentado con `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --offline`. Pasaron 6 pruebas unitarias y 6 instrumentadas en el Pixel 4 con Android 35. La prueba de identidad crea una cuenta desde el ViewModel de Administración, inicia sesión, registra una reserva y resuelve al residente por su ID real en el mismo proceso. La pérdida de la cuenta al reiniciar se identificó por inspección del código. La prueba de impago comprueba que otro residente puede tomar el horario liberado, que el repositorio rechaza adjuntar comprobantes o confirmar el pago tras el rechazo por impago, que tampoco se puede confirmar un comprobante en revisión de una reserva en estado `CANCELADA` y que una reserva con pago confirmado sigue bloqueando su horario. Los registros de ingresos y gastos son idénticos antes y después de estas operaciones. Queda pendiente la verificación visual del cambio en Tesorería.
