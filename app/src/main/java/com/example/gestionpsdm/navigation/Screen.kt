package com.example.gestionpsdm.navigation

sealed class Screen(val route: String) {

    data object Home : Screen("home_page")
    data object Espacios : Screen("espacios_page")

    data object Finanzas : Screen("finanzas_page")

    data object SolicitarReserva : Screen("solicitar_reserva_page")
}