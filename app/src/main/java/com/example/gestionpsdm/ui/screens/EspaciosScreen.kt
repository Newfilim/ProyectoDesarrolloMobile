package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.R
import com.example.gestionpsdm.model.Espacio
import androidx.compose.material3.Switch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.gestionpsdm.viewmodel.PreferenciasViewModel

@Composable
fun EspaciosScreen(
    onSolicitarReserva: () -> Unit,
    espacios: List<Espacio>,
    preferenciasViewModel: PreferenciasViewModel,
    modifier: Modifier = Modifier
) {
    val mostrarSoloHabilitados by preferenciasViewModel.soloHabilitados.collectAsState()
    val espaciosVisibles = if (mostrarSoloHabilitados){
        espacios.filter{ espacio ->
            espacio.habilitado
            }
        }else{
            espacios
        }

    LazyColumn(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    )
    {
        item {
            Text("Espacios comunitarios")
        }
        item{
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text("Mostrar solo habilitados")
                Switch(

                    checked = mostrarSoloHabilitados,
                    onCheckedChange = {nuevoValor ->
                        preferenciasViewModel.cambiarSoloHabilitados(nuevoValor)
                    }
                )
            }

        }

        items(espaciosVisibles){
                espacio ->
                TarjetaEspacio(onSolicitarReserva, espacio)
            }
        }

    }


@Composable
fun TarjetaEspacio(
    onSolicitarReserva: () -> Unit,
    espacio: Espacio){
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Image(
                painter = painterResource(R.drawable.quincho),
                contentDescription = "Imagen del quincho",
                modifier = Modifier.fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Text(espacio.nombre)

            Text("Capacidad máxima: ${espacio.capacidadMaxima} personas")
            Button(
                onClick = onSolicitarReserva,
                enabled = espacio.habilitado
            ) {
                Text("Solicitar reserva")
            }
        }
    }
}