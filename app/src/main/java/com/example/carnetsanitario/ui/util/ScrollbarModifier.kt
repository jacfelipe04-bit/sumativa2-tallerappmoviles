package com.example.carnetsanitario.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dibuja una barra de desplazamiento vertical sutil en el lateral derecho del contenedor.
 */
fun Modifier.verticalScrollbar(
    state: ScrollState,
    color: Color = Color(0xFF6A8EAE).copy(alpha = 0.55f),
    width: Dp = 4.dp
): Modifier = composed {
    val targetAlpha = if (state.maxValue > 0) 1f else 0f
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 300),
        label = "ScrollbarAlpha"
    )

    drawWithContent {
        drawContent()

        val canScroll = state.maxValue > 0
        if (canScroll && alpha > 0f) {
            val totalHeight = size.height
            val visibleRatio = (totalHeight / (totalHeight + state.maxValue)).coerceIn(0.15f, 1f)
            val thumbHeight = totalHeight * visibleRatio
            val scrollRatio = state.value.toFloat() / state.maxValue.toFloat()
            val thumbOffsetY = (totalHeight - thumbHeight) * scrollRatio

            val barWidthPx = width.toPx()
            val paddingPx = 4.dp.toPx()

            drawRoundRect(
                color = color.copy(alpha = color.alpha * alpha),
                topLeft = Offset(size.width - barWidthPx - paddingPx, thumbOffsetY),
                size = Size(barWidthPx, thumbHeight),
                cornerRadius = CornerRadius(barWidthPx / 2, barWidthPx / 2)
            )
        }
    }
}