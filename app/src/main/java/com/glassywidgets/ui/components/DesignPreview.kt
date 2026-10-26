package com.glassywidgets.ui.components

import android.graphics.Typeface
import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.glassywidgets.data.BgStyle
import com.glassywidgets.data.ColorRole
import com.glassywidgets.data.ElementType
import com.glassywidgets.data.FontOpt
import com.glassywidgets.data.HAlign
import com.glassywidgets.data.WeatherData
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.WidgetElement
import com.glassywidgets.ui.theme.LocalNow
import com.glassywidgets.widget.DesignLogic
import com.glassywidgets.widget.GlassPainter

private val fontCache = mutableMapOf<FontOpt, FontFamily>()
private fun FontOpt.compose(): FontFamily =
    fontCache.getOrPut(this) { FontFamily(Typeface.create(family, Typeface.NORMAL)) }

/**
 * Vista previa en vivo de un diseño. Usa el mismo bitmap de fondo y la misma
 * lógica de escala que el widget real, así que se ve igual en la pantalla de inicio.
 */
@Composable
fun DesignPreview(design: WidgetDesign, weather: WeatherData?, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
        val context = LocalContext.current
        val wDp = maxWidth.value
        val hDp = maxHeight.value
        val materialYou = design.style == BgStyle.MATERIAL_YOU
        val bgKey = design.copy(elements = emptyList(), name = "")
        val bitmap = remember(bgKey, wDp, hDp) {
            if (materialYou) null else GlassPainter.render(context, wDp, hDp, design).asImageBitmap()
        }
        val scale = DesignLogic.scale(design, wDp, hDp, weather)
        val shape = RoundedCornerShape(design.cornerRadius.dp)
        val cs = MaterialTheme.colorScheme
        val hAlign = when (design.align) {
            HAlign.START -> Alignment.Start
            HAlign.CENTER -> Alignment.CenterHorizontally
            HAlign.END -> Alignment.End
        }

        Box(
            Modifier
                .fillMaxSize()
                .clip(shape)
                .then(if (materialYou) Modifier.background(cs.surfaceContainerHigh) else Modifier),
        ) {
            bitmap?.let {
                Image(it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            }
            Column(
                Modifier.fillMaxSize().padding((design.padding * scale).dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = hAlign,
            ) {
                DesignLogic.rows(design.elements).forEach { row ->
                    if (row.size == 1) {
                        PreviewElement(row[0], design, weather, scale, materialYou)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            row.forEachIndexed { i, e ->
                                if (i > 0) Spacer(Modifier.width((8 * scale).dp))
                                PreviewElement(e, design, weather, scale, materialYou)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewElement(e: WidgetElement, d: WidgetDesign, weather: WeatherData?, scale: Float, materialYou: Boolean) {
    val cs = MaterialTheme.colorScheme
    val color = if (materialYou && e.colorRole != ColorRole.CUSTOM) {
        when (e.colorRole) {
            ColorRole.ACCENT -> cs.primary
            ColorRole.MUTED -> cs.onSurfaceVariant
            else -> cs.onSurface
        }
    } else {
        Color(DesignLogic.colorArgb(e, d))
    }
    when (e.type) {
        ElementType.DIVIDER -> Box(
            Modifier.fillMaxWidth().padding(vertical = (4 * scale).dp).height(1.dp)
                .background(if (materialYou) cs.outline else Color(d.textColor.toInt()).copy(alpha = 0.35f)),
        )
        ElementType.SPACER -> Spacer(Modifier.height((e.size * scale).dp))
        else -> {
            val isClock = e.type == ElementType.TIME || e.type == ElementType.DATE
            val text = if (isClock) {
                DesignLogic.formatClock(e, LocalNow.current.value, DateFormat.is24HourFormat(LocalContext.current))
            } else {
                DesignLogic.text(e, weather)
            }
            Text(
                text = text,
                maxLines = 1,
                style = TextStyle(
                    color = color,
                    fontSize = (e.size * scale).sp,
                    fontFamily = e.font.compose(),
                    lineHeight = if (isClock) 1.05.em else 1.25.em,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
                ),
            )
        }
    }
}
