package com.glassywidgets.widget

import android.content.res.ColorStateList
import android.os.Build
import android.text.format.DateFormat
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.glassywidgets.R
import com.glassywidgets.data.BgStyle
import com.glassywidgets.data.ColorRole
import com.glassywidgets.data.ElemAlign
import com.glassywidgets.data.ElementType
import com.glassywidgets.data.HAlign
import com.glassywidgets.data.LayoutMode
import com.glassywidgets.data.SystemInfo
import com.glassywidgets.data.VAlign
import com.glassywidgets.data.WeatherData
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.WidgetElement

private fun HAlign.glance(): Alignment.Horizontal = when (this) {
    HAlign.START -> Alignment.Start
    HAlign.CENTER -> Alignment.CenterHorizontally
    HAlign.END -> Alignment.End
}

private fun VAlign.glance(): Alignment.Vertical = when (this) {
    VAlign.TOP -> Alignment.Top
    VAlign.CENTER -> Alignment.CenterVertically
    VAlign.BOTTOM -> Alignment.Bottom
}

private fun ElemAlign.glance(): Alignment.Horizontal? = when (this) {
    ElemAlign.AUTO -> null
    ElemAlign.START -> Alignment.Start
    ElemAlign.CENTER -> Alignment.CenterHorizontally
    ElemAlign.END -> Alignment.End
}

/** Todo lo que necesita un elemento para dibujarse. */
private class Ctx(
    val design: WidgetDesign,
    val weather: WeatherData?,
    val sys: SystemInfo,
    val now: Long,
    val is24: Boolean,
    val scale: Float,
    val materialYou: Boolean,
)

/** Renderiza cualquier [WidgetDesign] como widget de pantalla de inicio. */
@Composable
fun GlanceDesign(design: WidgetDesign, weather: WeatherData?, sys: SystemInfo, modifier: GlanceModifier = GlanceModifier) {
    val context = LocalContext.current
    val size = LocalSize.current
    val w = size.width.value.coerceAtLeast(40f)
    val h = size.height.value.coerceAtLeast(40f)
    val materialYou = design.style == BgStyle.MATERIAL_YOU
    val c = Ctx(
        design, weather, sys, System.currentTimeMillis(), DateFormat.is24HourFormat(context),
        DesignLogic.scale(design, w, h, weather), materialYou,
    )
    val geo = DesignLogic.geometry(design, w)
    val cols = DesignLogic.columns(design)

    val background = if (materialYou) {
        GlanceModifier.background(GlanceTheme.colors.widgetBackground)
    } else {
        GlanceModifier.background(ImageProvider(GlassPainter.render(context, w, h, design)))
    }

    Box(
        modifier = GlanceModifier.fillMaxSize().then(background).cornerRadius(design.cornerRadius.dp).then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (design.layout == LayoutMode.SINGLE) {
            GColumn(
                cols[0], c, geo.widths[0], design.align, design.vAlign,
                GlanceModifier.fillMaxSize().padding(design.padding.dp),
            )
        } else {
            Row(GlanceModifier.fillMaxSize().padding(design.padding.dp), verticalAlignment = Alignment.CenterVertically) {
                GColumn(cols[0], c, geo.widths[0], design.align, design.vAlign, GlanceModifier.width(geo.widths[0].dp).fillMaxHeight())
                Spacer(GlanceModifier.width((geo.gap / 2f).dp))
                if (design.columnDivider) {
                    Box(GlanceModifier.width(1.dp).fillMaxHeight().background(lineColor(c))) {}
                }
                Spacer(GlanceModifier.width((geo.gap / 2f - 1f).dp))
                GColumn(cols[1], c, geo.widths[1], design.align2, design.vAlign2, GlanceModifier.width(geo.widths[1].dp).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun lineColor(c: Ctx): ColorProvider =
    if (c.materialYou) GlanceTheme.colors.outline
    else ColorProvider(Color(c.design.textColor.toInt()).copy(alpha = 0.30f))

@Composable
private fun GColumn(
    elements: List<WidgetElement>,
    c: Ctx,
    colW: Float,
    hAlign: HAlign,
    vAlign: VAlign,
    modifier: GlanceModifier,
) {
    Column(modifier = modifier, verticalAlignment = vAlign.glance(), horizontalAlignment = hAlign.glance()) {
        DesignLogic.rows(elements).forEach { row ->
            val override = row.first().align.glance()
            if (override == null) {
                GRow(row, c, colW)
            } else {
                Box(GlanceModifier.fillMaxWidth(), contentAlignment = Alignment(override, Alignment.CenterVertically)) {
                    GRow(row, c, colW)
                }
            }
        }
    }
}

@Composable
private fun GRow(row: List<WidgetElement>, c: Ctx, colW: Float) {
    if (row.size == 1) {
        GElement(row[0], c, colW)
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            row.forEachIndexed { i, e ->
                if (i > 0) Spacer(GlanceModifier.width((8 * c.scale).dp))
                GElement(e, c, colW)
            }
        }
    }
}

@Composable
private fun colorFor(e: WidgetElement, c: Ctx): ColorProvider =
    if (c.materialYou && e.colorRole != ColorRole.CUSTOM) {
        when (e.colorRole) {
            ColorRole.ACCENT -> GlanceTheme.colors.primary
            ColorRole.MUTED -> GlanceTheme.colors.onSurfaceVariant
            else -> GlanceTheme.colors.onSurface
        }
    } else {
        ColorProvider(Color(DesignLogic.colorArgb(e, c.design)))
    }

@Composable
private fun mutedColor(c: Ctx): ColorProvider =
    if (c.materialYou) GlanceTheme.colors.onSurfaceVariant
    else ColorProvider(Color(c.design.textColor.toInt()).copy(alpha = 0.72f))

private fun style(color: ColorProvider, sizeSp: Float, e: WidgetElement, center: Boolean = false) = TextStyle(
    color = color,
    fontSize = sizeSp.sp,
    fontFamily = FontFamily(e.font.family),
    textAlign = if (center) TextAlign.Center else null,
)

@Composable
private fun GElement(e: WidgetElement, c: Ctx, colW: Float) {
    val context = LocalContext.current
    val color = colorFor(e, c)
    val argb = color.getColor(context).toArgb()
    val px = e.size * c.scale

    when (e.type) {
        // Reloj y fecha con TextClock: se actualizan solos (incluso los segundos) sin gastar batería.
        ElementType.TIME, ElementType.DATE -> AndroidRemoteViews(clockViews(context.packageName, e, argb, px, null))

        ElementType.WORLD_CLOCK -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(e.zoneLabel, style = style(mutedColor(c), px * 0.62f, e), maxLines = 1)
            Spacer(GlanceModifier.width((6 * c.scale).dp))
            AndroidRemoteViews(clockViews(context.packageName, e, argb, px, e.timeZone))
        }

        // Reloj analógico con manecillas que se mueven solas.
        ElementType.ANALOG_CLOCK -> {
            val rv = RemoteViews(context.packageName, DesignLogic.analogLayout(e.dialStyle, e.showSeconds))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val hands = ColorStateList.valueOf(argb)
                rv.setColorStateList(R.id.analog, "setHourHandTintList", hands)
                rv.setColorStateList(R.id.analog, "setMinuteHandTintList", hands)
                rv.setColorStateList(R.id.analog, "setDialTintList", hands)
                val accent = colorFor(e.copy(colorRole = ColorRole.ACCENT), c).getColor(context).toArgb()
                rv.setColorStateList(R.id.analog, "setSecondHandTintList", ColorStateList.valueOf(accent))
            }
            AndroidRemoteViews(rv, GlanceModifier.size(px.dp))
        }

        // Carrusel animado: rota entre varios datos con transición.
        ElementType.ROTATOR -> {
            val rv = RemoteViews(context.packageName, DesignLogic.flipperLayout(e.flipAnim))
            rv.setInt(R.id.flipper, "setFlipInterval", e.interval.coerceIn(2, 30) * 1000)
            DesignLogic.rotatorTexts(e, c.weather, c.sys, c.now, c.is24).forEach { t ->
                val child = RemoteViews(context.packageName, DesignLogic.textLayout(e.font))
                child.setTextViewText(R.id.text, t)
                child.setTextColor(R.id.text, argb)
                child.setTextViewTextSize(R.id.text, TypedValue.COMPLEX_UNIT_SP, px)
                rv.addView(R.id.flipper, child)
            }
            AndroidRemoteViews(rv)
        }

        ElementType.HOURLY, ElementType.DAILY -> {
            val cells = if (e.type == ElementType.HOURLY) DesignLogic.hourlyCells(e, c.weather, c.now, c.is24)
            else DesignLogic.dailyCells(e, c.weather)
            val cellW = colW / cells.size.coerceAtLeast(1)
            Row(GlanceModifier.width(colW.dp)) {
                cells.forEach { cell ->
                    Column(GlanceModifier.width(cellW.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(cell.label, style = style(mutedColor(c), px * 0.78f, e, center = true), maxLines = 1)
                        Text(cell.icon, style = style(color, px * 1.35f, e, center = true), maxLines = 1)
                        Text(cell.main, style = style(color, px, e, center = true), maxLines = 1)
                        cell.sub?.let { Text(it, style = style(mutedColor(c), px * 0.78f, e, center = true), maxLines = 1) }
                    }
                }
            }
        }

        ElementType.DAY_PROGRESS, ElementType.BATTERY_BAR -> Column {
            if (e.showLabel) {
                Text(DesignLogic.barLabel(e, c.sys, c.now), style = style(mutedColor(c), 12f * c.scale, e), maxLines = 1)
                Spacer(GlanceModifier.height(4.dp))
            }
            val bar = GlassPainter.bar(
                context, colW, px, DesignLogic.barFraction(e, c.sys, c.now),
                argb, ColorUtils.setAlphaComponent(argb, 60),
            )
            Image(
                provider = ImageProvider(bar),
                contentDescription = null,
                modifier = GlanceModifier.width(colW.dp).height(px.dp),
                contentScale = ContentScale.FillBounds,
            )
        }

        ElementType.DIVIDER -> Box(GlanceModifier.fillMaxWidth().padding(vertical = (4 * c.scale).dp)) {
            Box(GlanceModifier.fillMaxWidth().height(1.dp).background(lineColor(c))) {}
        }

        ElementType.SPACER -> Spacer(GlanceModifier.height(px.dp))

        else -> Text(
            text = DesignLogic.text(e, c.weather, c.sys, c.now, c.is24),
            style = style(color, px, e),
            maxLines = 1,
        )
    }
}

private fun clockViews(pkg: String, e: WidgetElement, argb: Int, sizeSp: Float, zone: String?): RemoteViews {
    val (p12, p24) = DesignLogic.patterns(e)
    return RemoteViews(pkg, DesignLogic.clockLayout(e.font)).apply {
        setTextColor(R.id.clock, argb)
        setTextViewTextSize(R.id.clock, TypedValue.COMPLEX_UNIT_SP, sizeSp)
        setCharSequence(R.id.clock, "setFormat12Hour", p12)
        setCharSequence(R.id.clock, "setFormat24Hour", p24)
        if (zone != null) setString(R.id.clock, "setTimeZone", zone)
    }
}
