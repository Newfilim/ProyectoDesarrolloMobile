package com.example.gestionpsdm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.gestionpsdm.model.Espacio
import com.example.gestionpsdm.ui.theme.GestionPSDMTheme
import com.example.gestionpsdm.ui.screens.EspaciosScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestionPSDMTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val quincho = Espacio(
                        id = 1,
                        nombre = "Quincho principal",
                        capacidadMaxima = 25,
                        habilitado = true
                    )


                    EspaciosScreen(
                        espacio = quincho,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}