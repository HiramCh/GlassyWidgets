package com.glassywidgets.widget

import androidx.core.graphics.ColorUtils
import com.glassywidgets.R
import com.glassywidgets.data.ColorRole
import com.glassywidgets.data.ElementType
import com.glassywidgets.data.FontOpt
import com.glassywidgets.data.WeatherCodes
import com.glassywidgets.data.WeatherData
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.WidgetElement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/** Lógica compartida entre el widget real (Glance) y la vista previa (Compose). */
object DesignLogic {

    /** Agrupa elementos en filas según la opción "misma fila". */
    fun rows(elements: List<WidgetElement>): List<List<WidgetElement>> {
        val out = mutableListOf<MutableList<WidgetElement>>()
        for (e in elements) {
            val joinable = e.sameRow && out.isNotEmpty() &&
                e.type != ElementType.DIVIDER && e.type != ElementType.SPACER &&
                out.last().none { it.type == ElementType.DIVIDER || it.type == ElementType.SPACER }
            if (joinable) out.last().add(e) else out.add(mutableListOf(e))
        }
        return out
    }

    fun patterns(e: WidgetElement): Pair<String, String> = when (e.type) {
        ElementType.TIME -> e.timeFormat.p12 to e.timeFormat.p24
        ElementType.DATE -> e.dateFormat.pattern to e.dateFormat.pattern
        else -> "" to ""
    }

    fun formatClock(e: WidgetElement, now: Long, is24: Boolean): String {
        val (p12, p24) = patterns(e)
        return SimpleDateFormat(if (is24) p24 else p12, Locale.getDefault()).format(Date(now))
    }

    fun text(e: WidgetElement, w: WeatherData?): String {
        val t = when (e.type) {
            ElementType.WEATHER_ICON -> w?.let { WeatherCodes.emoji(it.code, it.isDay) } ?: "⛅"
            ElementType.TEMPERATURE -> w?.let { "${it.temp.roundToInt()}°" } ?: "--°"
            ElementType.CONDITION -> w?.let { WeatherCodes.description(it.code) } ?: "Sin datos"
            ElementType.MIN_MAX -> w?.let { "↑${it.max.roundToInt()}°  ↓${it.min.roundToInt()}°" } ?: "↑--°  ↓--°"
            ElementType.FEELS_LIKE -> w?.let { "Sensación ${it.feelsLike.roundToInt()}°" } ?: "Sensación --°"
            ElementType.HUMIDITY -> w?.let { "💧 ${it.humidity}%" } ?: "💧 --%"
            ElementType.WIND -> w?.let { "🌬 ${it.wind.roundToInt()} km/h" } ?: "🌬 -- km/h"
            ElementType.CITY -> w?.city ?: "—"
            ElementType.CUSTOM_TEXT -> e.text.ifBlank { "Texto" }
            else -> ""
        }
        return if (e.uppercase) t.uppercase(Locale.getDefault()) else t
    }

    /** ARGB del elemento para estilos no Material You. */
    fun colorArgb(e: WidgetElement, d: WidgetDesign): Int = when (e.colorRole) {
        ColorRole.TEXT -> d.textColor.toInt()
        ColorRole.ACCENT -> d.accentColor.toInt()
        ColorRole.MUTED -> ColorUtils.setAlphaComponent(d.textColor.toInt(), 184)
        ColorRole.CUSTOM -> e.customColor.toInt()
    }

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

    private fun rowHeight(e: WidgetElement): Float = when (e.type) {
        ElementType.DIVIDER -> 9f
        ElementType.SPACER -> e.size.toFloat()
        ElementType.TIME, ElementType.DATE -> e.size * 1.18f
        else -> e.size * 1.32f
    }

    private fun widthOf(e: WidgetElement, w: WeatherData?): Float = when (e.type) {
        ElementType.DIVIDER, ElementType.SPACER -> 0f
        ElementType.WEATHER_ICON -> e.size * 1.25f
        ElementType.TIME -> (if (e.timeFormat.p24.length <= 2) 2 else 5 + if (e.timeFormat.p12.endsWith("a")) 3 else 0) * e.size * 0.6f
        ElementType.DATE -> e.dateFormat.label.length * e.size * 0.55f
        else -> text(e, w).length * e.size * 0.56f
    }

    /** Factor de escala para que el diseño quepa en el tamaño real del widget. */
    fun scale(d: WidgetDesign, widthDp: Float, heightDp: Float, w: WeatherData?): Float {
        if (d.elements.isEmpty()) return 1f
        val rows = rows(d.elements)
        val pad = d.padding * 2f
        val estH = rows.sumOf { r -> r.maxOf { rowHeight(it) }.toDouble() }.toFloat()
        val estW = rows.maxOf { r -> r.sumOf { widthOf(it, w).toDouble() }.toFloat() + (r.size - 1) * 8f }
        val sh = (heightDp - pad) / estH.coerceAtLeast(1f)
        val sw = (widthDp - pad) / estW.coerceAtLeast(1f)
        return minOf(1f, sh, sw).coerceIn(0.35f, 1f)
    }
}
