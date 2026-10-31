package com.example.gestionpsdm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            GestionPSDMTheme {

                val navController = rememberNavController()



                val quincho = Espacio(1, "Quincho Principal", 25, true)
                val sala = Espacio(2, "Sala multiuso", 40, true)
                val sede = Espacio(id=3, "Sede comunitaria", 60, true)
                val espacios = listOf<Espacio>(quincho, sala, sede)
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable(Screen.Home.route) {
                            HomeScreen(
                                onVerEspacios = {
                                    navController.navigate(Screen.Espacios.route)
                                } ,
                                onVerFinanzas = {
                                    navController.navigate(Screen.Finanzas.route)
                                }
                            )
                        }

                        composable(Screen.Espacios.route) {
                            EspaciosScreen(
                                espacios = espacios,
                                onSolicitarReserva = {
                                navController.navigate(Screen.SolicitarReserva.route)
                                    }
                                )
                            }

                        composable(Screen.Finanzas.route){
                            FinanzasScreen()
                        }

                        composable(Screen.SolicitarReserva.route){

                            val reservaViewModel: ReservaViewModel = viewModel()

                            SolicitarReservaScreen(
                                viewModel = reservaViewModel)
                        }
                    }
                }
            }
        }
    }
}