
package com.example.gestionpsdm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.gestionpsdm.navigation.Screen
import com.example.gestionpsdm.model.RolUsuario
import com.example.gestionpsdm.repository.UsuariosDemoRepository
import com.example.gestionpsdm.repository.EspaciosDemoRepository

import com.example.gestionpsdm.ui.screens.EspaciosScreen
import com.example.gestionpsdm.ui.screens.HomeScreen
import com.example.gestionpsdm.ui.screens.FinanzasScreen
import com.example.gestionpsdm.ui.screens.SolicitarReservaScreen
import com.example.gestionpsdm.ui.screens.LoginScreen
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

import com.example.gestionpsdm.ui.theme.GestionPSDMTheme
import com.example.gestionpsdm.viewmodel.ReservaViewModel
import com.example.gestionpsdm.viewmodel.LoginViewModel
import com.example.gestionpsdm.viewmodel.GastoViewModel
import com.example.gestionpsdm.viewmodel.PreferenciasViewModel
import com.example.gestionpsdm.viewmodel.ComprobantesViewModel
import androidx.lifecycle.lifecycleScope
import com.example.gestionpsdm.data.local.ZarorDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.gestionpsdm.repository.GastosRoomRepository
import com.example.gestionpsdm.viewmodel.GastoViewModelFactory
import com.example.gestionpsdm.repository.IngresosRoomRepository
import com.example.gestionpsdm.viewmodel.IngresoViewModel
import com.example.gestionpsdm.viewmodel.IngresoViewModelFactory
import com.example.gestionpsdm.viewmodel.FinanzasViewModel
import com.example.gestionpsdm.viewmodel.FinanzasViewModelFactory
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Crear o abrir la base de datos local
        lifecycleScope.launch(Dispatchers.IO) {

            val database = ZarorDatabase.obtenerDatabase(
                applicationContext
            )

            // Abrir SQLite para ejecutar la inicialización
            database.openHelper.writableDatabase
        }

        setContent {

        GestionPSDMTheme {

                var idUsuarioActual by rememberSaveable {
                    mutableStateOf<Int?>(null)
                }

                val usuarioActual = UsuariosDemoRepository.obtenerUsuarios()
                    .find { it.id == idUsuarioActual }

                val navController = rememberNavController()

                val entradaActual by
                navController.currentBackStackEntryAsState()

                val rutaActual = entradaActual
                    ?.destination
                    ?.route ?: Screen.Login.route

                val comprobantesViewModel:
                        ComprobantesViewModel = viewModel()

                val espacios = EspaciosDemoRepository.espacios

                // Pantalla principal correspondiente a cada perfil.
                val rutaInicio = if (
                    usuarioActual?.rol == RolUsuario.RESIDENTE
                ) {
                    Screen.Home.route
                } else {
                    "${Screen.PanelRol.route}/${usuarioActual?.rol?.name}"
                }

                val estaEnInicio =
                    rutaActual == Screen.Home.route ||
                            rutaActual.startsWith(
                                "${Screen.PanelRol.route}/"
                            )

                val tituloPantalla = when {

                    rutaActual == Screen.Home.route ->
                        "Inicio"

                    rutaActual.startsWith(
                        "${Screen.PanelRol.route}/"
                    ) -> "Panel principal"

                    rutaActual == Screen.Espacios.route ->
                        "Espacios"

                    rutaActual == Screen.MisReservas.route ->
                        "Mis reservas"

                    rutaActual.startsWith(
                        "${Screen.SolicitarReserva.route}/"
                    ) -> "Solicitar reserva"

                    rutaActual == Screen.Finanzas.route ->
                        "Finanzas"

                    rutaActual == Screen.GestionUsuarios.route ->
                        "Usuarios"

                    rutaActual == Screen.GestionReservas.route ->
                        "Reservas"

                    rutaActual == Screen.GestionEspacios.route ->
                        "Gestionar espacios"

                    rutaActual == Screen.InformeFinanciero.route ->
                        "Informe financiero"

                    rutaActual == Screen.RevisarPagos.route ->
                        "Revisar pagos"

                    rutaActual == Screen.RegistrarGasto.route ->
                        "Registrar gasto"

                    rutaActual == Screen.ConsultarIngresos.route ->
                        "Ingresos"

                    rutaActual == Screen.Tesoreria.route ->
                        "Tesorería"

                    else -> "Junta de Vecinos"
                }

                // Cerrar sesión y limpiar la navegación anterior.
                fun cerrarSesion() {
                    idUsuarioActual = null

                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,

                    topBar = {

                        if (
                            usuarioActual != null &&
                            rutaActual != Screen.Login.route
                        ) {

                            TopAppBar(
                                title = {
                                    Text(
                                        text = tituloPantalla,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },

                                navigationIcon = {

                                    if (!estaEnInicio) {

                                        TextButton(
                                            onClick = {
                                                navController.popBackStack()
                                            },
                                            modifier = Modifier.semantics {
                                                contentDescription =
                                                    "Volver a la pantalla anterior"
                                            }
                                        ) {
                                            Text("←")
                                        }
                                    }
                                },

                                actions = {

                                    if (!estaEnInicio) {

                                        TextButton(
                                            onClick = {

                                                val volvio =
                                                    navController.popBackStack(
                                                        rutaInicio,
                                                        false
                                                    )

                                                if (!volvio) {
                                                    navController.navigate(
                                                        rutaInicio
                                                    ) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Inicio")
                                        }
                                    }

                                    TextButton(
                                        onClick = {
                                            cerrarSesion()
                                        }
                                    ) {
                                        Text("Salir")
                                    }
                                },

                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor =
                                        MaterialTheme.colorScheme.surface,

                                    titleContentColor =
                                        MaterialTheme.colorScheme.onSurface,

                                    navigationIconContentColor =
                                        MaterialTheme.colorScheme.primary,

                                    actionIconContentColor =
                                        MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable(Screen.Login.route) {

                            val loginViewModel:
                                    LoginViewModel = viewModel()

                            LoginScreen(
                                viewModel = loginViewModel,
                                onInicioCorrecto = { usuario ->

                                    idUsuarioActual = usuario.id

                                    if (
                                        usuario.rol == RolUsuario.RESIDENTE
                                    ) {
                                        navController.navigate(
                                            Screen.Home.route
                                        ) {
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

                            val nombreRol =
                                entrada.arguments?.getString("rol")

                            val rol = when (nombreRol) {
                                "ADMINISTRADOR" ->
                                    RolUsuario.ADMINISTRADOR

                                "TESORERIA" ->
                                    RolUsuario.TESORERIA

                                "DIRECTIVA" ->
                                    RolUsuario.DIRECTIVA

                                else ->
                                    RolUsuario.RESIDENTE
                            }

                            PanelRolScreen(
                                rol = rol,
                                onNavegar = { ruta ->
                                    navController.navigate(ruta)
                                },
                                onCerrarSesion = {
                                    cerrarSesion()
                                }
                            )
                        }

                        composable(Screen.Home.route) {

                            HomeScreen(
                                onVerEspacios = {
                                    navController.navigate(
                                        Screen.Espacios.route
                                    )
                                },

                                onVerFinanzas = {
                                    navController.navigate(
                                        Screen.Finanzas.route
                                    )
                                },

                                onVerMisReservas = {
                                    navController.navigate(
                                        Screen.MisReservas.route
                                    )
                                },

                                onCerrarSesion = {
                                    cerrarSesion()
                                }
                            )
                        }

                        composable(Screen.Espacios.route) {

                            val preferenciasViewModel:
                                    PreferenciasViewModel = viewModel()

                            EspaciosScreen(
                                preferenciasViewModel =
                                    preferenciasViewModel,

                                espacios = espacios,

                                onSolicitarReserva = { idEspacio ->

                                    navController.navigate(
                                        "${Screen.SolicitarReserva.route}/${idEspacio}"
                                    )
                                }
                            )
                        }


                        composable(Screen.Finanzas.route) {

                            val database = ZarorDatabase.obtenerDatabase(
                                applicationContext
                            )

                            val gastosRepository = GastosRoomRepository(
                                database.gastoDao()
                            )

                            val ingresosRepository = IngresosRoomRepository(
                                database.ingresoDao()
                            )

                            val finanzasViewModel: FinanzasViewModel = viewModel(
                                factory = FinanzasViewModelFactory(
                                    gastosRepository = gastosRepository,
                                    ingresosRepository = ingresosRepository
                                )
                            )

                            FinanzasScreen(
                                viewModel = finanzasViewModel
                            )
                        }


                        composable(
                            route =
                                "${Screen.SolicitarReserva.route}/{idEspacio}"
                        ) { entrada ->

                            val idEspacio = entrada.arguments
                                ?.getString("idEspacio")
                                ?.toIntOrNull()

                            val reservaViewModel:
                                    ReservaViewModel = viewModel()

                            val usuario = usuarioActual

                            if (
                                idEspacio != null &&
                                usuario != null
                            ) {

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

                            if (
                                usuario != null &&
                                usuario.rol == RolUsuario.RESIDENTE
                            ) {
                                MisReservasScreen(
                                    comprobantesViewModel =
                                        comprobantesViewModel,
                                    idUsuario = usuario.id
                                )
                            } else {
                                Text(
                                    "No se encontró una sesión de residente activa."
                                )
                            }
                        }

                        composable(Screen.GestionEspacios.route) {

                            GestionEspaciosScreen(
                                espacios = espacios,

                                onCambiarEstado = { id, habilitado ->

                                    EspaciosDemoRepository
                                        .cambiarEstadoEspacio(
                                            id,
                                            habilitado
                                        )
                                }
                            )
                        }


                        composable(Screen.InformeFinanciero.route) {

                            val database = ZarorDatabase.obtenerDatabase(
                                applicationContext
                            )

                            val gastosRepository = GastosRoomRepository(
                                database.gastoDao()
                            )

                            val ingresosRepository = IngresosRoomRepository(
                                database.ingresoDao()
                            )

                            val finanzasViewModel: FinanzasViewModel = viewModel(
                                factory = FinanzasViewModelFactory(
                                    gastosRepository = gastosRepository,
                                    ingresosRepository = ingresosRepository
                                )
                            )

                            InformeFinancieroScreen(
                                viewModel = finanzasViewModel
                            )
                        }


                        composable(Screen.RevisarPagos.route) {

                            RevisarPagosScreen(
                                comprobantesViewModel =
                                    comprobantesViewModel,
                                espacios = espacios
                            )
                        }


                        composable(Screen.RegistrarGasto.route) {

                            val database = ZarorDatabase.obtenerDatabase(
                                applicationContext
                            )

                            val repository = GastosRoomRepository(
                                database.gastoDao()
                            )

                            val gastoViewModel: GastoViewModel = viewModel(
                                factory = GastoViewModelFactory(repository)
                            )

                            RegistrarGastoScreen(
                                viewModel = gastoViewModel
                            )
                        }



                        composable(Screen.ConsultarIngresos.route) {

                            val database = ZarorDatabase.obtenerDatabase(
                                applicationContext
                            )

                            val repository = IngresosRoomRepository(
                                database.ingresoDao()
                            )

                            val ingresoViewModel: IngresoViewModel = viewModel(
                                factory = IngresoViewModelFactory(repository)
                            )

                            ConsultarIngresosScreen(
                                viewModel = ingresoViewModel
                            )
                        }

                    }
                }
            }
        }
    }
}
