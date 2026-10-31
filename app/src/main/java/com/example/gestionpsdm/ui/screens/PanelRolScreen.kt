
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.model.RolUsuario
import com.example.gestionpsdm.navigation.Screen

private data class OpcionPanel(
    val nombre: String,
    val ruta: String?
)

@Composable
fun PanelRolScreen(
    rol: RolUsuario,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val titulo: String
    val opciones: List<OpcionPanel>

    when (rol) {
        RolUsuario.ADMINISTRADOR -> {
            titulo = "Panel de administración"
            opciones = listOf(
                OpcionPanel(
                    "Gestionar usuarios",
                    Screen.GestionUsuarios.route
                ),
                OpcionPanel(
                    "Gestionar reservas",
                    Screen.GestionReservas.route
                ),
                OpcionPanel(
                    "Administrar espacios",
                    Screen.GestionEspacios.route
                )
            )
        }


        RolUsuario.TESORERIA -> {
            titulo = "Panel de tesorería"
            opciones = listOf(
                OpcionPanel(
                    "Revisar pagos",
                    Screen.RevisarPagos.route
                ),
                OpcionPanel(
                    "Registrar gastos",
                    Screen.RegistrarGasto.route
                ),
                OpcionPanel(
                    "Consultar ingresos",
                    Screen.ConsultarIngresos.route
                )
            )
        }

        RolUsuario.DIRECTIVA -> {
            titulo = "Panel de directiva"
            opciones = listOf(
                OpcionPanel(
                    "Consultar informe financiero",
                    Screen.InformeFinanciero.route
                )
            )
        }

        RolUsuario.RESIDENTE -> {
            titulo = "Panel del residente"
            opciones = listOf(
                OpcionPanel(
                    "Mis reservas",
                    Screen.MisReservas.route
                ),
                OpcionPanel("Mis reservas", null),
                OpcionPanel(
                    "Información financiera",
                    Screen.Finanzas.route
                )
            )
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineMedium
        )

        for (opcion in opciones) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = opcion.nombre,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Button(
                        onClick = {
                            val ruta = opcion.ruta
                            if (ruta != null) {
                                onNavegar(ruta)
                            }
                        },
                        enabled = opcion.ruta != null
                    ) {
                        Text("Abrir")
                    }
                }
            }
        }
    }
}
