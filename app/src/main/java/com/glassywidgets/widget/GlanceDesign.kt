package com.glassywidgets.widget

import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.glassywidgets.R
import com.glassywidgets.data.BgStyle
import com.glassywidgets.data.ColorRole
import com.glassywidgets.data.ElementType
import com.glassywidgets.data.HAlign
import com.glassywidgets.data.WeatherData
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.WidgetElement

/** Renderiza cualquier [WidgetDesign] como widget de pantalla de inicio. */
@Composable
fun GlanceDesign(design: WidgetDesign, weather: WeatherData?, modifier: GlanceModifier = GlanceModifier) {
    val context = LocalContext.current
    val size = LocalSize.current
    val w = size.width.value.coerceAtLeast(40f)
    val h = size.height.value.coerceAtLeast(40f)
    val scale = DesignLogic.scale(design, w, h, weather)
    val materialYou = design.style == BgStyle.MATERIAL_YOU

    val background = if (materialYou) {
        GlanceModifier.background(GlanceTheme.colors.widgetBackground)
    } else {
        GlanceModifier.background(ImageProvider(GlassPainter.render(context, w, h, design)))
    }
    val hAlign = when (design.align) {
        HAlign.START -> Alignment.Start
        HAlign.CENTER -> Alignment.CenterHorizontally
        HAlign.END -> Alignment.End
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .then(background)
            .cornerRadius(design.cornerRadius.dp)
            .then(modifier)
            .padding((design.padding * scale).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = hAlign,
    ) {
        DesignLogic.rows(design.elements).forEach { row ->
            if (row.size == 1) {
                GlanceElement(row[0], design, weather, scale, materialYou)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    row.forEachIndexed { i, e ->
                        if (i > 0) Spacer(GlanceModifier.width((8 * scale).dp))
                        GlanceElement(e, design, weather, scale, materialYou)
                    }
                }
            }
        }
    }
}

@Composable
private fun colorFor(e: WidgetElement, d: WidgetDesign, materialYou: Boolean): ColorProvider =
    if (materialYou && e.colorRole != ColorRole.CUSTOM) {
        when (e.colorRole) {
            ColorRole.ACCENT -> GlanceTheme.colors.primary
            ColorRole.MUTED -> GlanceTheme.colors.onSurfaceVariant
            else -> GlanceTheme.colors.onSurface
        }
    } else {
        ColorProvider(Color(DesignLogic.colorArgb(e, d)))
    }

@Composable
private fun GlanceElement(e: WidgetElement, d: WidgetDesign, weather: WeatherData?, scale: Float, materialYou: Boolean) {
    val context = LocalContext.current
    val color = colorFor(e, d, materialYou)
    when (e.type) {
        // Hora y fecha usan TextClock: se actualizan solas cada minuto sin gastar batería.
        ElementType.TIME, ElementType.DATE -> {
            val (p12, p24) = DesignLogic.patterns(e)
            val rv = RemoteViews(context.packageName, DesignLogic.clockLayout(e.font)).apply {
                setTextColor(R.id.clock, color.getColor(context).toArgb())
                setTextViewTextSize(R.id.clock, TypedValue.COMPLEX_UNIT_SP, e.size * scale)
                setCharSequence(R.id.clock, "setFormat12Hour", p12)
                setCharSequence(R.id.clock, "setFormat24Hour", p24)
            }
            AndroidRemoteViews(rv)
        }
        ElementType.DIVIDER -> {
            val line = if (materialYou) GlanceTheme.colors.outline
            else ColorProvider(Color(d.textColor.toInt()).copy(alpha = 0.35f))
            Box(GlanceModifier.fillMaxWidth().padding(vertical = (4 * scale).dp)) {
                Box(GlanceModifier.fillMaxWidth().height(1.dp).background(line)) {}
            }
        }
        ElementType.SPACER -> Spacer(GlanceModifier.height((e.size * scale).dp))
        else -> Text(
            text = DesignLogic.text(e, weather),
            style = TextStyle(
                color = color,
                fontSize = (e.size * scale).sp,
                fontFamily = FontFamily(e.font.family),
            ),
            maxLines = 1,
        )
    }
}
