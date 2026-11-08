
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.model.RolUsuario
import com.example.gestionpsdm.navigation.Screen

private data class OpcionPanel(
    val nombre: String,
    val descripcion: String,
    val textoBoton: String,
    val ruta: String
)

@Composable
fun PanelRolScreen(
    rol: RolUsuario,
    onNavegar: (String) -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {

    val titulo: String
    val descripcionPanel: String
    val opciones: List<OpcionPanel>

    when (rol) {

        RolUsuario.ADMINISTRADOR -> {
            titulo = "Panel de administración"
            descripcionPanel = "Administra las cuentas, reservas y espacios de la comunidad."

            opciones = listOf(
                OpcionPanel(
                    nombre = "Gestión de usuarios",
                    descripcion = "Crea usuarios, edita sus datos y habilita o deshabilita cuentas.",
                    textoBoton = "Gestionar usuarios",
                    ruta = Screen.GestionUsuarios.route
                ),
                OpcionPanel(
                    nombre = "Gestión de reservas",
                    descripcion = "Revisa las solicitudes de los residentes y decide cuáles aprobar o rechazar.",
                    textoBoton = "Revisar reservas",
                    ruta = Screen.GestionReservas.route
                ),
                OpcionPanel(
                    nombre = "Espacios comunitarios",
                    descripcion = "Controla cuáles espacios están habilitados para recibir solicitudes.",
                    textoBoton = "Administrar espacios",
                    ruta = Screen.GestionEspacios.route
                )
            )
        }

        RolUsuario.TESORERIA -> {
            titulo = "Panel de tesorería"
            descripcionPanel = "Revisa comprobantes y registra los movimientos financieros."

            opciones = listOf(
                OpcionPanel(
                    nombre = "Comprobantes de pago",
                    descripcion = "Revisa las fotografías enviadas por residentes y confirma o rechaza los pagos.",
                    textoBoton = "Revisar comprobantes",
                    ruta = Screen.RevisarPagos.route
                ),
                OpcionPanel(
                    nombre = "Registro de gastos",
                    descripcion = "Registra los gastos de limpieza, mantenimiento e insumos de la comunidad.",
                    textoBoton = "Registrar gastos",
                    ruta = Screen.RegistrarGasto.route
                ),
                OpcionPanel(
                    nombre = "Registro de ingresos",
                    descripcion = "Registra manualmente los ingresos y consulta el total acumulado.",
                    textoBoton = "Ver ingresos",
                    ruta = Screen.ConsultarIngresos.route
                )
            )
        }

        RolUsuario.DIRECTIVA -> {
            titulo = "Panel de directiva"
            descripcionPanel = "Consulta el resumen financiero de la comunidad."

            opciones = listOf(
                OpcionPanel(
                    nombre = "Informe financiero",
                    descripcion = "Revisa los ingresos, los gastos y el balance calculado.",
                    textoBoton = "Consultar informe",
                    ruta = Screen.InformeFinanciero.route
                )
            )
        }

        RolUsuario.RESIDENTE -> {
            titulo = "Panel del residente"
            descripcionPanel = "Consulta tus reservas y la información comunitaria."

            opciones = listOf(
                OpcionPanel(
                    nombre = "Mis reservas",
                    descripcion = "Consulta las solicitudes que has realizado.",
                    textoBoton = "Ver mis reservas",
                    ruta = Screen.MisReservas.route
                ),
                OpcionPanel(
                    nombre = "Finanzas comunitarias",
                    descripcion = "Consulta la información financiera disponible.",
                    textoBoton = "Ver finanzas",
                    ruta = Screen.Finanzas.route
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = descripcionPanel,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        for (opcion in opciones) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Text(
                        text = opcion.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = opcion.descripcion,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            onNavegar(opcion.ruta)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "${opcion.textoBoton}  →",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}


