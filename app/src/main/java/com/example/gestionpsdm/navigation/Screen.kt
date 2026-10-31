package com.example.gestionpsdm.navigation

sealed class Screen(val route: String) {

    data object Home : Screen("home_page")
    data object Espacios : Screen("espacios_page")

    data object Finanzas : Screen("finanzas_page")

    data object SolicitarReserva : Screen("solicitar_reserva_page")

    data object Login: Screen("login_page")

    data object PanelRol: Screen("panel_rol_page")

    data object GestionUsuarios : Screen("gestion_usuarios_page")

    data object GestionReservas : Screen("gestion_reservas_page")

    data object Tesoreria : Screen("tesoreria_page")

    data object MisReservas : Screen("mis_reservas_page")

    data object GestionEspacios : Screen("gestion_espacios_page")

    data object InformeFinanciero : Screen("informe_financiero_page")

    data object RevisarPagos : Screen("revisar_pagos_page")

    data object RegistrarGasto : Screen("registrar_gasto_page")

    data object ConsultarIngresos : Screen("consultar_ingresos_page")


}