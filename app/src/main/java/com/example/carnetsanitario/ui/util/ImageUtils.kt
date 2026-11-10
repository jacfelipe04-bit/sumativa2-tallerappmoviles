package com.example.carnetsanitario.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageUtils {

    /**
     * Convierte y comprime la foto seleccionada (Uri) a String Base64.
     * Mantiene una resolución máxima de 500px para que pese menos de ~150-200 KB en Firestore.
     */
    fun uriABase64(context: Context, uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null

        // Si ya es un Base64 largo que venía de la nube, no lo re-procesa
        if (uriString.length > 500 && !uriString.startsWith("content://") && !uriString.startsWith("file://")) {
            return uriString
        }

        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmapOriginal = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmapOriginal == null) return null

            val maxDimension = 500
            val ancho = bitmapOriginal.width
            val alto = bitmapOriginal.height
            val ratio = if (ancho > alto) maxDimension.toFloat() / ancho else maxDimension.toFloat() / alto

            val bitmapFinal = if (ratio < 1.0f) {
                Bitmap.createScaledBitmap(
                    bitmapOriginal,
                    (ancho * ratio).toInt(),
                    (alto * ratio).toInt(),
                    true
                )
            } else {
                bitmapOriginal
            }

            val stream = ByteArrayOutputStream()
            bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            val bytes = stream.toByteArray()

            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte el String (sea Base64 o Uri) a Bitmap para la UI.
     */
    fun decodificarFoto(context: Context, fotoUri: String?): Bitmap? {
        if (fotoUri.isNullOrBlank()) return null

        // Caso 1: Cadena Base64
        if (fotoUri.length > 500 && !fotoUri.startsWith("content://") && !fotoUri.startsWith("file://")) {
            return try {
                val bytes = Base64.decode(fotoUri, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) {
                null
            }
        }

        // Caso 2: Uri local tradicional
        return try {
            val uri = Uri.parse(fotoUri)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}