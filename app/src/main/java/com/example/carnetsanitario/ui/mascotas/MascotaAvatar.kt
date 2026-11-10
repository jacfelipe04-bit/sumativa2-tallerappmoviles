package com.example.carnetsanitario.ui.mascotas

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carnetsanitario.data.local.enums.Especie
import com.example.carnetsanitario.ui.theme.CelestePrimario
import com.example.carnetsanitario.ui.theme.RosaAcento

@Composable
fun MascotaAvatar(
    fotoUri: String?,
    especie: Especie,
    tamano: Dp = 54.dp,
    onClick: (() -> Unit)? = null
) {
    var errorCarga by remember(fotoUri) { mutableStateOf(false) }

    // Determina el modelo para Coil: si es Base64 pasa el ByteArray decodificado; si es ruta/Uri lo pasa directo
    val modeloImagen = remember(fotoUri) {
        if (fotoUri.isNullOrBlank()) {
            null
        } else if (fotoUri.length > 500 && !fotoUri.startsWith("content://") && !fotoUri.startsWith("file://")) {
            try {
                Base64.decode(fotoUri, Base64.DEFAULT)
            } catch (_: Exception) {
                null
            }
        } else {
            fotoUri
        }
    }

    val modifier = Modifier
        .size(tamano)
        .clip(CircleShape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(
        modifier = modifier.background(
            if (especie == Especie.CANINO) CelestePrimario.copy(alpha = 0.25f)
            else RosaAcento.copy(alpha = 0.35f)
        ),
        contentAlignment = Alignment.Center
    ) {
        if (modeloImagen != null && !errorCarga) {
            AsyncImage(
                model = modeloImagen,
                contentDescription = "Foto de la mascota",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = {
                    errorCarga = true
                }
            )
        } else {
            Text(
                text = if (especie == Especie.CANINO) "🐶" else "🐱",
                fontSize = (tamano.value * 0.45).sp
            )
        }
    }
}