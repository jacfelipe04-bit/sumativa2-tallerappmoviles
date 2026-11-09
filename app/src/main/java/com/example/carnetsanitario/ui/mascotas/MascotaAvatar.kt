package com.example.carnetsanitario.ui.mascotas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        if (!fotoUri.isNullOrBlank()) {
            AsyncImage(
                model = fotoUri,
                contentDescription = "Foto de la mascota",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = if (especie == Especie.CANINO) "🐶" else "🐱",
                fontSize = (tamano.value * 0.45).sp
            )
        }
    }
}