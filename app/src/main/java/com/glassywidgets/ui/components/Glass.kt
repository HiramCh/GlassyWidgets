package com.glassywidgets.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.glassywidgets.ui.theme.LocalGlassMode

/**
 * Fondo animado con "gotas" de color difuminadas. Es lo que da vida al efecto
 * cristal: las tarjetas translúcidas dejan ver estos colores moviéndose detrás.
 */
@Composable
fun LiquidBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val glass = LocalGlassMode.current
    val cs = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Box(modifier.fillMaxSize().background(cs.background)) {
        if (glass) {
            val t = rememberInfiniteTransition(label = "blobs")
            val a by t.animateFloat(0f, 1f, infiniteRepeatable(tween(14_000, easing = LinearEasing), RepeatMode.Reverse), label = "a")
            val b by t.animateFloat(0f, 1f, infiniteRepeatable(tween(19_000, easing = LinearEasing), RepeatMode.Reverse), label = "b")
            val base = if (dark) listOf(Color(0xFF0B1020), Color(0xFF17142B)) else listOf(Color(0xFFEFF3FF), Color(0xFFFBF0FF))
            val alpha = if (dark) 0.50f else 0.42f
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.verticalGradient(base))
                val m = size.minDimension
                blob(cs.primary, Offset(size.width * (0.10f + 0.55f * a), size.height * (0.10f + 0.18f * b)), m * 0.85f, alpha)
                blob(cs.tertiary, Offset(size.width * (0.95f - 0.50f * b), size.height * (0.42f + 0.16f * a)), m * 0.75f, alpha)
                blob(cs.secondary, Offset(size.width * (0.25f + 0.40f * b), size.height * (0.85f - 0.20f * a)), m * 0.80f, alpha)
            }
        }
        content()
    }
}

private fun DrawScope.blob(color: Color, center: Offset, radius: Float, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(listOf(color.copy(alpha = alpha), Color.Transparent), center, radius),
        radius = radius,
        center = center,
    )
}

/** Tarjeta de cristal (o tarjeta Material normal si el modo cristal está apagado). */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val glass = LocalGlassMode.current
    val dark = isSystemInDarkTheme()
    val cs = MaterialTheme.colorScheme
    val click = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    if (glass) {
        val fill = if (dark) {
            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.05f)))
        } else {
            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.72f), Color.White.copy(alpha = 0.36f)))
        }
        val rim = Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (dark) 0.45f else 0.95f),
                Color.White.copy(alpha = 0.06f),
                Color.White.copy(alpha = if (dark) 0.25f else 0.65f),
            )
        )
        Column(
            modifier.clip(shape).background(fill).border(1.dp, rim, shape).then(click).padding(contentPadding),
            content = content,
        )
    } else {
        Column(
            modifier.clip(shape).background(cs.surfaceContainerLow).then(click).padding(contentPadding),
            content = content,
        )
    }
}

/** Barra de navegación flotante tipo píldora. */
@Composable
fun GlassNavBar(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val items = listOf(Icons.Filled.Home to "Inicio", Icons.Filled.Create to "Crear", Icons.Filled.Settings to "Ajustes")
    GlassCard(modifier, shape = RoundedCornerShape(50), contentPadding = PaddingValues(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEachIndexed { i, (icon, label) ->
                val sel = i == selected
                val bg by animateColorAsState(if (sel) cs.primary else Color.Transparent, label = "navBg")
                val fg by animateColorAsState(if (sel) cs.onPrimary else cs.onSurface, label = "navFg")
                Row(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(bg)
                        .clickable { onSelect(i) }
                        .animateContentSize()
                        .padding(horizontal = if (sel) 18.dp else 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icon, contentDescription = label, tint = fg)
                    if (sel) {
                        Spacer(Modifier.width(8.dp))
                        Text(label, color = fg, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.padding(top = 12.dp, bottom = 4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}
