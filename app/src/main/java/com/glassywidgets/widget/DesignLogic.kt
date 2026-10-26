package com.glassywidgets.widget

import androidx.core.graphics.ColorUtils
import com.glassywidgets.R
import com.glassywidgets.data.ColorRole
import com.glassywidgets.data.DayPoint
import com.glassywidgets.data.ElemAlign
import com.glassywidgets.data.HAlign
import com.glassywidgets.data.VAlign
import com.glassywidgets.data.DialStyle
import com.glassywidgets.data.ElementType
import com.glassywidgets.data.FlipAnim
import com.glassywidgets.data.FontOpt
import com.glassywidgets.data.HourPoint
import com.glassywidgets.data.LayoutMode
import com.glassywidgets.data.SystemInfo
import com.glassywidgets.data.WeatherCodes
import com.glassywidgets.data.WeatherData
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.WidgetElement
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.SimpleTimeZone
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.roundToInt

/** Márgenes (dp) que colocan un elemento libre en su posición. */
data class Insets(val start: Float, val top: Float, val end: Float, val bottom: Float)

/** Medidas de cada columna del widget, en dp. */
data class Geometry(val widths: List<Float>, val gap: Float)

/** Lógica compartida entre el widget real (Glance) y la vista previa (Compose). */
object DesignLogic {

    const val COLUMN_GAP = 14f

    /** Elementos de cada columna. */
    fun columns(d: WidgetDesign): List<List<WidgetElement>> =
        if (d.layout == LayoutMode.SPLIT) {
            listOf(d.elements.filter { it.column == 0 }, d.elements.filter { it.column != 0 })
        } else {
            listOf(d.elements)
        }

    fun geometry(d: WidgetDesign, widthDp: Float): Geometry {
        val inner = (widthDp - d.padding * 2f).coerceAtLeast(20f)
        return if (d.layout == LayoutMode.SPLIT) {
            val usable = inner - COLUMN_GAP
            val w1 = usable * d.split.coerceIn(0.25f, 0.75f)
            Geometry(listOf(w1, usable - w1), COLUMN_GAP)
        } else {
            Geometry(listOf(inner), 0f)
        }
    }

    // ---------- Modo libre ----------

    fun innerSize(d: WidgetDesign, widthDp: Float, heightDp: Float): Pair<Float, Float> =
        (widthDp - d.padding * 2f).coerceAtLeast(10f) to (heightDp - d.padding * 2f).coerceAtLeast(10f)

    /** Escala del modo libre: proporcional al tamaño con el que se diseñó. */
    fun freeScale(d: WidgetDesign, widthDp: Float, heightDp: Float): Float =
        minOf(widthDp / d.refW.coerceAtLeast(40f), heightDp / d.refH.coerceAtLeast(40f)).coerceIn(0.35f, 3f)

    private fun axis(pos: Float, anchor: Int, size: Float): Pair<Float, Float> {
        val p = pos.coerceIn(0f, 1f)
        return when (anchor) {
            0 -> (p * size) to 0f
            2 -> 0f to ((1f - p) * size)
            else -> if (p >= 0.5f) ((2f * p - 1f) * size) to 0f else 0f to ((1f - 2f * p) * size)
        }
    }

    /**
     * Para posicionar sin coordenadas absolutas (RemoteViews no las tiene) se usa una caja
     * de tamaño completo, alineada según el ancla, con márgenes asimétricos.
     */
    fun freeInsets(e: WidgetElement, innerW: Float, innerH: Float): Insets {
        val (s, en) = axis(e.x, e.anchorH.ordinal, innerW)
        val (t, b) = axis(e.y, e.anchorV.ordinal, innerH)
        return Insets(s, t, en, b)
    }

    /** Convierte un diseño de columnas en uno libre conservando aproximadamente su aspecto. */
    fun toFree(d: WidgetDesign): WidgetDesign {
        if (d.layout == LayoutMode.FREE) return d
        val w = d.refW
        val h = d.refH
        val (iw, ih) = innerSize(d, w, h)
        val s = scale(d, w, h, null)
        val geo = geometry(d, w)
        val cols = columns(d)
        val placed = mutableMapOf<String, WidgetElement>()
        cols.forEachIndexed { ci, col ->
            if (col.isEmpty()) return@forEachIndexed
            val colX0 = if (ci == 0) 0f else geo.widths[0] + geo.gap
            val colW = geo.widths.getOrElse(ci) { iw }
            val rows = rows(col)
            val heights = rows.map { r -> r.maxOf { rowHeight(it) } * s }
            val total = heights.sum()
            val align = if (ci == 0) d.align else d.align2
            val vAlign = if (ci == 0) d.vAlign else d.vAlign2
            var y = when (vAlign) {
                VAlign.TOP -> 0f
                VAlign.CENTER -> (ih - total) / 2f
                VAlign.BOTTOM -> ih - total
            }
            rows.forEachIndexed { ri, r ->
                val rowW = r.sumOf { (widthOf(it, null) * s).toDouble() }.toFloat() + (r.size - 1) * 8f * s
                val ownAlign = when (r.first().align) {
                    ElemAlign.START -> HAlign.START
                    ElemAlign.CENTER -> HAlign.CENTER
                    ElemAlign.END -> HAlign.END
                    ElemAlign.AUTO -> align
                }
                var x = colX0 + when (ownAlign) {
                    HAlign.START -> 0f
                    HAlign.CENTER -> (colW - rowW) / 2f
                    HAlign.END -> colW - rowW
                }
                val cy = y + heights[ri] / 2f
                r.forEach { e ->
                    val ew = if (e.type.isStretch) colW else widthOf(e, null) * s
                    placed[e.id] = e.copy(
                        x = ((x + if (e.type.isStretch) colW / 2f else 0f) / iw).coerceIn(0f, 1f),
                        y = (cy / ih).coerceIn(0f, 1f),
                        anchorH = if (e.type.isStretch) HAlign.CENTER else HAlign.START,
                        anchorV = VAlign.CENTER,
                        w = if (e.type.isStretch) (colW / iw).coerceIn(0.05f, 1f) else e.w,
                        sameRow = false,
                    )
                    x += ew + 8f * s
                }
                y += heights[ri]
            }
        }
        return d.copy(layout = LayoutMode.FREE, elements = d.elements.map { placed[it.id] ?: it })
    }

    // ---------- Anillos ----------

    fun ringFraction(e: WidgetElement, w: WeatherData?, sys: SystemInfo, now: Long): Float = when (e.ringSource) {
        com.glassywidgets.data.RingSource.BATTERY -> sys.battery / 100f
        com.glassywidgets.data.RingSource.DAY -> dayFraction(now)
        com.glassywidgets.data.RingSource.HUMIDITY -> (w?.humidity ?: 0) / 100f
        com.glassywidgets.data.RingSource.PRECIP -> (w?.precipProb ?: 0) / 100f
    }

    fun ringLabel(e: WidgetElement, w: WeatherData?, sys: SystemInfo, now: Long): String {
        val pct = (ringFraction(e, w, sys, now) * 100).roundToInt()
        val icon = when (e.ringSource) {
            com.glassywidgets.data.RingSource.BATTERY -> if (sys.charging) "⚡" else "🔋"
            com.glassywidgets.data.RingSource.DAY -> "☀"
            com.glassywidgets.data.RingSource.HUMIDITY -> "💧"
            com.glassywidgets.data.RingSource.PRECIP -> "☔"
        }
        return "$icon$pct%"
    }

    /** Agrupa elementos en filas según la opción "misma fila". */
    fun rows(elements: List<WidgetElement>): List<List<WidgetElement>> {
        val out = mutableListOf<MutableList<WidgetElement>>()
        for (e in elements) {
            val joinable = e.sameRow && out.isNotEmpty() && !e.type.isStretch && e.type != ElementType.SPACER &&
                out.last().none { it.type.isStretch || it.type == ElementType.SPACER }
            if (joinable) out.last().add(e) else out.add(mutableListOf(e))
        }
        return out
    }

    fun patterns(e: WidgetElement): Pair<String, String> = when (e.type) {
        ElementType.TIME, ElementType.WORLD_CLOCK -> e.timeFormat.p12 to e.timeFormat.p24
        ElementType.DATE -> e.dateFormat.pattern to e.dateFormat.pattern
        else -> "" to ""
    }

    fun formatClock(e: WidgetElement, now: Long, is24: Boolean): String {
        val (p12, p24) = patterns(e)
        val f = SimpleDateFormat(if (is24) p24 else p12, Locale.getDefault())
        if (e.type == ElementType.WORLD_CLOCK) f.timeZone = TimeZone.getTimeZone(e.timeZone)
        return f.format(Date(now))
    }

    private fun locationZone(w: WeatherData): TimeZone = SimpleTimeZone(w.utcOffset * 1000, "loc")

    private fun fmtEpoch(sec: Long, pattern: String, w: WeatherData): String =
        SimpleDateFormat(pattern, Locale.getDefault()).apply { timeZone = locationZone(w) }.format(Date(sec * 1000))

    private fun hm(is24: Boolean) = if (is24) "HH:mm" else "h:mm a"

    fun greeting(now: Long, name: String): String {
        val h = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
        val base = when (h) {
            in 5..11 -> "Buenos días"
            in 12..18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
        return if (name.isBlank()) base else "$base, ${name.trim()}"
    }

    fun dayFraction(now: Long): Float {
        val c = Calendar.getInstance().apply { timeInMillis = now }
        return (c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)) / 1440f
    }

    fun barFraction(e: WidgetElement, sys: SystemInfo, now: Long): Float = when (e.type) {
        ElementType.BATTERY_BAR -> sys.battery / 100f
        else -> dayFraction(now)
    }

    fun barLabel(e: WidgetElement, sys: SystemInfo, now: Long): String = when (e.type) {
        ElementType.BATTERY_BAR -> "${if (sys.charging) "⚡" else "🔋"} Batería ${sys.battery}%"
        else -> "Día ${(dayFraction(now) * 100).roundToInt()}%"
    }

    fun text(e: WidgetElement, w: WeatherData?, sys: SystemInfo, now: Long, is24: Boolean): String {
        val t = when (e.type) {
            ElementType.WEATHER_ICON -> w?.let { WeatherCodes.emoji(it.code, it.isDay) } ?: "⛅"
            ElementType.TEMPERATURE -> w?.let { "${it.temp.roundToInt()}°" } ?: "--°"
            ElementType.CONDITION -> w?.let { WeatherCodes.description(it.code) } ?: "Sin datos"
            ElementType.MIN_MAX -> w?.let { "↑${it.max.roundToInt()}°  ↓${it.min.roundToInt()}°" } ?: "↑--°  ↓--°"
            ElementType.FEELS_LIKE -> w?.let { "Sensación ${it.feelsLike.roundToInt()}°" } ?: "Sensación --°"
            ElementType.HUMIDITY -> w?.let { "💧 ${it.humidity}%" } ?: "💧 --%"
            ElementType.WIND -> w?.let { "🌬 ${it.wind.roundToInt()} km/h" } ?: "🌬 -- km/h"
            ElementType.PRECIP -> w?.let { "☔ ${it.precipProb}%" } ?: "☔ --%"
            ElementType.UV -> w?.let { "🕶 UV ${it.uvMax.roundToInt()}" } ?: "🕶 UV --"
            ElementType.SUNRISE -> w?.takeIf { it.sunrise > 0 }?.let { "🌅 " + fmtEpoch(it.sunrise, hm(is24), it) } ?: "🌅 --:--"
            ElementType.SUNSET -> w?.takeIf { it.sunset > 0 }?.let { "🌇 " + fmtEpoch(it.sunset, hm(is24), it) } ?: "🌇 --:--"
            ElementType.CITY -> w?.city ?: "—"
            ElementType.GREETING -> greeting(now, e.text)
            ElementType.NEXT_ALARM -> sys.nextAlarm?.let {
                "⏰ " + SimpleDateFormat(if (is24) "EEE HH:mm" else "EEE h:mm a", Locale.getDefault()).format(Date(it))
            } ?: "⏰ Sin alarmas"
            ElementType.BATTERY -> "${if (sys.charging) "⚡" else "🔋"} ${sys.battery}%"
            ElementType.CUSTOM_TEXT -> e.text.ifBlank { "Texto" }
            ElementType.EMOJI -> e.text.ifBlank { "✨" }
            ElementType.DAY_PROGRESS, ElementType.BATTERY_BAR -> barLabel(e, sys, now)
            else -> ""
        }
        return if (e.uppercase && e.type.isText) t.uppercase(Locale.getDefault()) else t
    }

    /** Textos del carrusel animado. */
    fun rotatorTexts(e: WidgetElement, w: WeatherData?, sys: SystemInfo, now: Long, is24: Boolean): List<String> =
        e.rotateItems.ifEmpty { listOf(ElementType.CONDITION) }
            .map { text(e.copy(type = it, uppercase = e.uppercase), w, sys, now, is24) }

    // ---------- Pronósticos ----------

    data class ForecastCell(val label: String, val icon: String, val main: String, val sub: String?)

    fun hourlyCells(e: WidgetElement, w: WeatherData?, now: Long, is24: Boolean): List<ForecastCell> {
        val n = e.count.coerceIn(3, 8)
        if (w == null || w.hourly.isEmpty()) return List(n) { ForecastCell("--", "⛅", "--°", null) }
        val upcoming: List<HourPoint> = w.hourly.filter { it.time * 1000 >= now - 3_600_000 }.take(n)
        return upcoming.mapIndexed { i, h ->
            ForecastCell(
                label = if (i == 0) "Ahora" else fmtEpoch(h.time, if (is24) "HH:mm" else "h a", w),
                icon = WeatherCodes.emoji(h.code, h.isDay),
                main = "${h.temp.roundToInt()}°",
                sub = if (h.precip >= 20) "${h.precip}%" else null,
            )
        }
    }

    fun dailyCells(e: WidgetElement, w: WeatherData?): List<ForecastCell> {
        val n = e.count.coerceIn(3, 7)
        if (w == null || w.daily.isEmpty()) return List(n) { ForecastCell("--", "⛅", "--°", "--°") }
        return w.daily.take(n).mapIndexed { i, d: DayPoint ->
            val day = if (i == 0) "Hoy" else fmtEpoch(d.time, "EEE", w).trimEnd('.')
                .replaceFirstChar { it.titlecase(Locale.getDefault()) }
            ForecastCell(day, WeatherCodes.emoji(d.code, true), "${d.max.roundToInt()}°", "${d.min.roundToInt()}°")
        }
    }

    // ---------- Colores y recursos ----------

    /** ARGB del elemento para estilos no Material You. */
    fun colorArgb(e: WidgetElement, d: WidgetDesign): Int {
        val base = when (e.colorRole) {
            ColorRole.TEXT -> d.textColor.toInt()
            ColorRole.ACCENT -> d.accentColor.toInt()
            ColorRole.MUTED -> ColorUtils.setAlphaComponent(d.textColor.toInt(), 184)
            ColorRole.CUSTOM -> e.customColor.toInt()
        }
        return withAlpha(base, e.alpha)
    }

    /** Multiplica el alfa de un color ARGB. */
    fun withAlpha(argb: Int, alpha: Float): Int =
        if (alpha >= 0.999f) argb
        else ColorUtils.setAlphaComponent(argb, ((argb ushr 24) * alpha.coerceIn(0f, 1f)).roundToInt())

    fun clockLayout(font: FontOpt): Int = when (font) {
        FontOpt.THIN -> R.layout.tc_thin
        FontOpt.LIGHT -> R.layout.tc_light
        FontOpt.REGULAR -> R.layout.tc_regular
        FontOpt.MEDIUM -> R.layout.tc_medium
        FontOpt.BLACK -> R.layout.tc_black
        FontOpt.CONDENSED -> R.layout.tc_condensed
        FontOpt.SERIF -> R.layout.tc_serif
        FontOpt.MONO -> R.layout.tc_mono
    }

    fun textLayout(font: FontOpt): Int = when (font) {
        FontOpt.THIN -> R.layout.tv_thin
        FontOpt.LIGHT -> R.layout.tv_light
        FontOpt.REGULAR -> R.layout.tv_regular
        FontOpt.MEDIUM -> R.layout.tv_medium
        FontOpt.BLACK -> R.layout.tv_black
        FontOpt.CONDENSED -> R.layout.tv_condensed
        FontOpt.SERIF -> R.layout.tv_serif
        FontOpt.MONO -> R.layout.tv_mono
    }

    fun flipperLayout(anim: FlipAnim): Int = when (anim) {
        FlipAnim.FADE -> R.layout.flip_fade
        FlipAnim.SLIDE_UP -> R.layout.flip_up
        FlipAnim.SLIDE_SIDE -> R.layout.flip_side
    }

    fun analogLayout(style: DialStyle, seconds: Boolean): Int = when (style) {
        DialStyle.GLASS -> if (seconds) R.layout.analog_glass_sec else R.layout.analog_glass
        DialStyle.MINIMAL -> if (seconds) R.layout.analog_min_sec else R.layout.analog_min
    }

    // ---------- Escala automática ----------

    private fun rowHeight(e: WidgetElement): Float = when (e.type) {
        ElementType.SHAPE, ElementType.PHOTO -> e.h * 120f
        ElementType.RING -> e.size + 4f
        ElementType.DIVIDER -> 9f
        ElementType.SPACER -> e.size.toFloat()
        ElementType.ANALOG_CLOCK -> e.size + 4f
        ElementType.HOURLY, ElementType.DAILY -> e.size * 4.1f
        ElementType.DAY_PROGRESS, ElementType.BATTERY_BAR -> e.size + 6f + if (e.showLabel) 18f else 0f
        ElementType.TIME, ElementType.DATE, ElementType.WORLD_CLOCK -> e.size * 1.18f
        else -> e.size * 1.32f
    }

    private fun widthOf(e: WidgetElement, w: WeatherData?): Float = when (e.type) {
        ElementType.SHAPE, ElementType.PHOTO -> 0f
        ElementType.RING -> e.size.toFloat()
        ElementType.DIVIDER, ElementType.SPACER -> 0f
        ElementType.DAY_PROGRESS, ElementType.BATTERY_BAR -> 40f
        ElementType.HOURLY, ElementType.DAILY -> e.count * e.size * 2.3f
        ElementType.ANALOG_CLOCK -> e.size.toFloat()
        ElementType.WEATHER_ICON -> e.size * 1.25f
        ElementType.EMOJI -> e.text.ifBlank { "✨" }.length.coerceAtMost(4) * e.size * 0.75f
        ElementType.TIME -> e.timeFormat.p24.length * e.size * 0.6f + if (e.timeFormat.p12.endsWith("a")) e.size * 1.6f else 0f
        ElementType.DATE -> e.dateFormat.label.length * e.size * 0.55f
        ElementType.WORLD_CLOCK -> (e.zoneLabel.length * 0.7f + 5) * e.size * 0.56f
        ElementType.ROTATOR -> (e.rotateItems.maxOfOrNull { text(e.copy(type = it), w, SystemInfo.PREVIEW, 0, true).length } ?: 8) * e.size * 0.56f
        else -> text(e, w, SystemInfo.PREVIEW, System.currentTimeMillis(), true).length * e.size * 0.56f
    }

    /** Factor de escala para que el diseño quepa en el tamaño real del widget. */
    fun scale(d: WidgetDesign, widthDp: Float, heightDp: Float, w: WeatherData?): Float {
        if (d.layout == LayoutMode.FREE) return freeScale(d, widthDp, heightDp)
        val geo = geometry(d, widthDp)
        val innerH = heightDp - d.padding * 2f
        var s = 1f
        columns(d).forEachIndexed { i, col ->
            if (col.isEmpty()) return@forEachIndexed
            val rows = rows(col)
            val estH = rows.sumOf { r -> r.maxOf { rowHeight(it) }.toDouble() }.toFloat()
            val estW = rows.maxOf { r -> r.sumOf { widthOf(it, w).toDouble() }.toFloat() + (r.size - 1) * 8f }
            val colW = geo.widths.getOrElse(i) { geo.widths.first() }
            s = minOf(s, innerH / max(estH, 1f), colW / max(estW, 1f))
        }
        return s.coerceIn(0.3f, 1f)
    }
}
