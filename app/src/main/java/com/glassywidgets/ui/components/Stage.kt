package com.glassywidgets.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fondos de pantalla simulados para ver los widgets como en el inicio. */
enum class Wallpaper(val label: String, val colors: List<Long>, val glow: List<Long>) {
    AURORA("Aurora", listOf(0xFF1B1036, 0xFF4C1D95, 0xFF0E7490), listOf(0xFF22D3EE, 0xFFA855F7)),
    SUNSET("Atardecer", listOf(0xFF3B0A45, 0xFFB4235A, 0xFFF97316), listOf(0xFFFDBA74, 0xFFF472B6)),
    OCEAN("Océano", listOf(0xFF0B2545, 0xFF13315C, 0xFF1D7874), listOf(0xFF5EEAD4, 0xFF60A5FA)),
    FOREST("Bosque", listOf(0xFF0F2417, 0xFF1F4D2B, 0xFF6B8E23), listOf(0xFFBEF264, 0xFF34D399)),
    NIGHT("Noche", listOf(0xFF05060A, 0xFF0F172A, 0xFF1E1B4B), listOf(0xFF6366F1, 0xFF0EA5E9)),
    PASTEL("Pastel", listOf(0xFFFDE2E4, 0xFFE2ECE9, 0xFFBEE1E6), listOf(0xFFF9A8D4, 0xFF93C5FD)),
}

fun wallpaperAt(i: Int): Wallpaper = Wallpaper.entries[Math.floorMod(i, Wallpaper.entries.size)]

/**
 * "Escenario": un recorte de fondo de pantalla animado donde se coloca la vista previa,
 * para que el efecto cristal se vea como en la pantalla de inicio real.
 */
@Composable
fun WallpaperStage(
    wallpaper: Wallpaper,
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    animated: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val t = rememberInfiniteTransition(label = "stage")
    val a by t.animateFloat(0f, 1f, infiniteRepeatable(tween(9_000, easing = LinearEasing), RepeatMode.Reverse), label = "sa")
    val p = if (animated) a else 0.5f
    Box(
        modifier
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(wallpaper.colors.map { Color(it) })),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val m = size.maxDimension
            val g1 = Color(wallpaper.glow[0])
            val g2 = Color(wallpaper.glow[1])
            val c1 = Offset(size.width * (0.15f + 0.3f * p), size.height * (0.2f + 0.2f * p))
            val c2 = Offset(size.width * (0.9f - 0.25f * p), size.height * (0.85f - 0.2f * p))
            drawCircle(Brush.radialGradient(listOf(g1.copy(alpha = 0.55f), Color.Transparent), c1, m * 0.45f), m * 0.45f, c1)
            drawCircle(Brush.radialGradient(listOf(g2.copy(alpha = 0.5f), Color.Transparent), c2, m * 0.5f), m * 0.5f, c2)
        }
        content()
    }
}
