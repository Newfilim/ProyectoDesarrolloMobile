package com.example.gestionpsdm.utils

import android.content.Context
import java.io.File
import android.net.Uri
import androidx.core.content.FileProvider

fun crearCarpetaComprobantes(context: Context): File {

    val carpeta = File(context.filesDir, "comprobantes")

    carpeta.mkdirs()

    return carpeta
}

fun crearArchivoComprobante(context: Context): File {

    val carpeta = crearCarpetaComprobantes(context)

    val archivo = File.createTempFile(
        "comprobante_",
        ".jpg",
        carpeta
    )

    return archivo
}

fun crearUriComprobante(
    context: Context,
    archivo: File
): Uri {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )

    return uri
}