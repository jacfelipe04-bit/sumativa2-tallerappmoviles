package com.example.carnetsanitario.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {
    fun guardarImagenLocalmente(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val carpetaFotos = File(context.filesDir, "fotos_mascotas")
            if (!carpetaFotos.exists()) {
                carpetaFotos.mkdirs()
            }
            val archivoDestino = File(carpetaFotos, "mascota_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(archivoDestino)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            archivoDestino.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}