
package com.example.gestionpsdm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gestionpsdm.model.EstadoReserva
import com.example.gestionpsdm.model.Reserva
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalContext
import com.example.gestionpsdm.utils.crearArchivoComprobante
import com.example.gestionpsdm.utils.crearUriComprobante
import java.io.File


@Composable
fun MisReservasScreen(
    modifier: Modifier = Modifier
) {

    val reservas = listOf(
        Reserva(
            id = 1,
            idUsuario = 1,
            idEspacio = 1,
            fecha = "06/11/2026",
            horaInicio = "19:00",
            horaFin = "22:00",
            estado = EstadoReserva.PENDIENTE
        ),
        Reserva(
            id = 2,
            idUsuario = 1,
            idEspacio = 2,
            fecha = "07/11/2026",
            horaInicio = "10:00",
            horaFin = "14:00",
            estado = EstadoReserva.APROBADA
        )
    )

    var comprobanteUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { resultado ->
        if(resultado != null){
            comprobanteUri = resultado
        }
    }




    val context = LocalContext.current

    var uriPendiente by remember {
        mutableStateOf<Uri?>(null)
    }

    var fotoCompletaUri by remember {
        mutableStateOf<Uri?>(null)
    }
    var archivoPendiente by remember {
        mutableStateOf<File?>(null)
    }
    val camaraCompleta = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito ->

        if (exito) {
            fotoCompletaUri = uriPendiente
        } else {
                archivoPendiente?.delete()
        }

        uriPendiente = null
        archivoPendiente = null
    }





    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Mis reservas",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text("Consulta el estado de tus solicitudes.")
        }
        item {
            Button(
                onClick = {
                val archivo = crearArchivoComprobante(context)
                    archivoPendiente = archivo
                    val uri = crearUriComprobante(context, archivo)
                    uriPendiente = uri
                    camaraCompleta.launch(uri)
                }
            ) {
                Text("Tomar fotografía completa")
            }
            if (fotoCompletaUri != null) {
                AsyncImage(
                    model = fotoCompletaUri,
                    contentDescription = "Fotografia completa del comprobante",
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        item{
            Button(
                onClick = {
                    selectorImagen.launch("image/*")
                }
            ) {
                Text("Seleccionar comprobante")
            }
            if(comprobanteUri != null){
                Text("Imagen seleccionada correctamente")
                AsyncImage(
                    model = comprobanteUri,
                    contentDescription = "Vista previa del comprobante",
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }


        items(reservas) { reserva ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Reserva N.º ${reserva.id}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text("Espacio ID: ${reserva.idEspacio}")
                    Text("Fecha: ${reserva.fecha}")
                    Text("Desde: ${reserva.horaInicio}")
                    Text("Hasta: ${reserva.horaFin}")
                    Text("Estado: ${reserva.estado.name}")
                }
            }
        }
    }
}
