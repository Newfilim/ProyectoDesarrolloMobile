package com.example.gestionpsdm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestionpsdm.model.Espacio
import com.example.gestionpsdm.navigation.Screen
import com.example.gestionpsdm.ui.screens.EspaciosScreen
import com.example.gestionpsdm.ui.screens.HomeScreen
import com.example.gestionpsdm.ui.screens.FinanzasScreen
import com.example.gestionpsdm.ui.screens.SolicitarReservaScreen
import com.example.gestionpsdm.ui.theme.GestionPSDMTheme
import com.example.gestionpsdm.viewmodel.ReservaViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestionpsdm.ui.screens.LoginScreen
import com.example.gestionpsdm.model.RolUsuario
import com.example.gestionpsdm.model.Usuario
import com.example.gestionpsdm.ui.screens.PanelRolScreen
import com.example.gestionpsdm.ui.screens.GestionUsuariosScreen
import com.example.gestionpsdm.ui.screens.GestionReservasScreen
import com.example.gestionpsdm.ui.screens.TesoreriaScreen
import com.example.gestionpsdm.ui.screens.MisReservasScreen
import com.example.gestionpsdm.ui.screens.GestionEspaciosScreen
import com.example.gestionpsdm.ui.screens.InformeFinancieroScreen
import com.example.gestionpsdm.ui.screens.RevisarPagosScreen
import com.example.gestionpsdm.ui.screens.RegistrarGastoScreen
import com.example.gestionpsdm.ui.screens.ConsultarIngresosScreen
import com.example.gestionpsdm.viewmodel.LoginViewModel
import com.example.gestionpsdm.viewmodel.GastoViewModel
import com.example.gestionpsdm.viewmodel.PreferenciasViewModel
import com.example.gestionpsdm.viewmodel.ComprobantesViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.gestionpsdm.repository.UsuariosDemoRepository

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            GestionPSDMTheme {


                var idUsuarioActual by rememberSaveable {
                    mutableStateOf<Int?>(null)
                }

                val usuarioActual = UsuariosDemoRepository.obtenerUsuarios()
                    .find { it.id == idUsuarioActual }


                val navController = rememberNavController()

                val comprobantesViewModel: ComprobantesViewModel = viewModel()




                val quincho = Espacio(1, "Quincho", 25, true)
                val sala = Espacio(2, "Sala multiuso", 40, false)
                val piscina = Espacio(3, "Piscina", 60, true)
                val espacios = listOf<Espacio>(quincho, sala, piscina)
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable(Screen.Login.route) {

                            val loginViewModel: LoginViewModel = viewModel()

                            LoginScreen(
                                viewModel = loginViewModel,
                                onInicioCorrecto = { usuario ->

                                    idUsuarioActual = usuario.id

                                    if (usuario.rol == RolUsuario.RESIDENTE) {

                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Login.route) {
                                                inclusive = true
                                            }
                                        }

                                    } else {

                                        navController.navigate(
                                            "${Screen.PanelRol.route}/${usuario.rol.name}"
                                        ) {
                                            popUpTo(Screen.Login.route) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            )
                        }


                        composable(
                            route = "${Screen.PanelRol.route}/{rol}"
                        ) { entrada ->

                            val nombreRol = entrada.arguments?.getString("rol")

                            val rol = when (nombreRol) {
                                "ADMINISTRADOR" -> RolUsuario.ADMINISTRADOR
                                "TESORERIA" -> RolUsuario.TESORERIA
                                "DIRECTIVA" -> RolUsuario.DIRECTIVA
                                else -> RolUsuario.RESIDENTE
                            }

                            PanelRolScreen(
                                rol = rol,
                                onNavegar = { ruta ->
                                    navController.navigate(ruta)
                                },
                                onCerrarSesion = {
                                    idUsuarioActual = null
                                    navController.navigate(Screen.Login.route){
                                        popUpTo(navController.graph.id){
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(
                                onVerEspacios = {
                                    navController.navigate(Screen.Espacios.route)
                                } ,
                                onVerFinanzas = {
                                    navController.navigate(Screen.Finanzas.route)
                                },
                                onVerMisReservas = {
                                    navController.navigate(Screen.MisReservas.route)
                                },
                                onCerrarSesion = {
                                    idUsuarioActual = null
                                    navController.navigate(Screen.Login.route){
                                        popUpTo(navController.graph.id){
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(Screen.Espacios.route) {
                            val preferenciasViewModel: PreferenciasViewModel = viewModel()

                            EspaciosScreen(
                                preferenciasViewModel = preferenciasViewModel,
                                espacios = espacios,
                                onSolicitarReserva = { idEspacio ->
                                navController.navigate("${Screen.SolicitarReserva.route}/${idEspacio}")
                                    }
                                )
                            }

                        composable(Screen.Finanzas.route){
                            FinanzasScreen()
                        }

                        composable(
                            route= "${Screen.SolicitarReserva.route}/{idEspacio}"
                        ){ entrada ->

                            val idEspacio = entrada.arguments
                                ?.getString("idEspacio")
                                ?.toIntOrNull()
                            val reservaViewModel: ReservaViewModel = viewModel()

                            val usuario = usuarioActual
                            if(idEspacio != null && usuario != null){
                                SolicitarReservaScreen(
                                    reservaViewModel,
                                    modifier = Modifier.fillMaxWidth(),
                                    usuario.id,
                                    idEspacio
                                )
                            }
                        }

                        composable(Screen.GestionUsuarios.route) {
                            GestionUsuariosScreen()
                        }

                        composable(Screen.GestionReservas.route) {
                            GestionReservasScreen()
                        }

                        composable(Screen.Tesoreria.route) {
                            TesoreriaScreen()
                        }



                        composable(Screen.MisReservas.route) {

                            val usuario = usuarioActual

                            if (usuario != null &&
                                usuario.rol == RolUsuario.RESIDENTE
                            ) {
                                MisReservasScreen(
                                    comprobantesViewModel = comprobantesViewModel,
                                    idUsuario = usuario.id
                                )
                            } else {
                                Text("No se encontró una sesión de residente activa.")
                            }
                        }



                        composable(Screen.GestionEspacios.route) {
                            GestionEspaciosScreen(
                                espacios = espacios
                            )
                        }

                        composable(Screen.InformeFinanciero.route) {
                            InformeFinancieroScreen()
                        }

                        composable(Screen.RevisarPagos.route) {
                            RevisarPagosScreen(
                                comprobantesViewModel = comprobantesViewModel,
                                espacios = espacios
                            )
                        }

                        composable(Screen.RegistrarGasto.route) {

                            val gastoViewModel: GastoViewModel = viewModel()

                            RegistrarGastoScreen(
                                viewModel = gastoViewModel
                            )
                        }

                        composable(Screen.ConsultarIngresos.route) {
                            ConsultarIngresosScreen()
                        }
                    }
                }
            }
        }
    }
}