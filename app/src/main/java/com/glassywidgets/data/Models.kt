package com.glassywidgets.data

import kotlinx.serialization.Serializable
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString().substring(0, 8)

/** Estilo de fondo del widget. */
@Serializable
enum class BgStyle(val label: String) {
    GLASS("Cristal"),
    DARK_GLASS("Cristal oscuro"),
    MATERIAL_YOU("Material You"),
    SOLID("Sólido"),
    GRADIENT("Degradado"),
}

@Serializable
enum class HAlign(val label: String) { START("Izquierda"), CENTER("Centro"), END("Derecha") }

/** Tipografías del sistema (funcionan tanto en la app como en RemoteViews). */
@Serializable
enum class FontOpt(val label: String, val family: String) {
    THIN("Fina", "sans-serif-thin"),
    LIGHT("Ligera", "sans-serif-light"),
    REGULAR("Normal", "sans-serif"),
    MEDIUM("Media", "sans-serif-medium"),
    BLACK("Gruesa", "sans-serif-black"),
    CONDENSED("Condensada", "sans-serif-condensed"),
    SERIF("Serif", "serif"),
    MONO("Mono", "monospace"),
}

@Serializable
enum class ColorRole(val label: String) { TEXT("Texto"), ACCENT("Acento"), MUTED("Suave"), CUSTOM("Propio") }

@Serializable
enum class ElementType(val label: String, val emoji: String) {
    TIME("Hora", "🕒"),
    DATE("Fecha", "📅"),
    WEATHER_ICON("Icono del clima", "⛅"),
    TEMPERATURE("Temperatura", "🌡️"),
    CONDITION("Condición", "☁️"),
    MIN_MAX("Máx / Mín", "↕️"),
    FEELS_LIKE("Sensación", "🤒"),
    HUMIDITY("Humedad", "💧"),
    WIND("Viento", "🌬️"),
    CITY("Ciudad", "📍"),
    CUSTOM_TEXT("Texto libre", "✏️"),
    DIVIDER("Separador", "➖"),
    SPACER("Espacio", "↕"),
}

@Serializable
enum class TimeFormat(val label: String, val p12: String, val p24: String) {
    AUTO("Automático", "h:mm", "HH:mm"),
    H24("24 h", "HH:mm", "HH:mm"),
    H12("12 h", "h:mm", "h:mm"),
    H12_AMPM("12 h + AM/PM", "h:mm a", "h:mm a"),
    HOURS("Solo horas", "hh", "HH"),
    MINUTES("Solo minutos", "mm", "mm"),
}

@Serializable
enum class DateFormatOpt(val label: String, val pattern: String) {
    LONG("Lunes 3 de marzo", "EEEE d 'de' MMMM"),
    MEDIUM("lun, 3 mar", "EEE, d MMM"),
    SHORT("03/03/2026", "dd/MM/yyyy"),
    WEEKDAY("Lunes", "EEEE"),
    DAY("3", "d"),
    DAY_MONTH("3 mar", "d MMM"),
    MONTH("Marzo", "MMMM"),
}

@Serializable
data class WidgetElement(
    val id: String = newId(),
    val type: ElementType,
    val size: Int = 16,
    val font: FontOpt = FontOpt.REGULAR,
    val colorRole: ColorRole = ColorRole.TEXT,
    val customColor: Long = 0xFFFFFFFF,
    /** Si es true, se coloca a la derecha del elemento anterior (misma fila). */
    val sameRow: Boolean = false,
    val timeFormat: TimeFormat = TimeFormat.AUTO,
    val dateFormat: DateFormatOpt = DateFormatOpt.LONG,
    val text: String = "",
    val uppercase: Boolean = false,
)

@Serializable
data class WidgetDesign(
    val id: String = newId(),
    val name: String = "Nuevo diseño",
    val style: BgStyle = BgStyle.GLASS,
    val color1: Long = 0xFF8EC5FF,
    val color2: Long = 0xFFE0C3FC,
    val opacity: Float = 0.35f,
    val cornerRadius: Int = 28,
    val padding: Int = 16,
    val align: HAlign = HAlign.CENTER,
    val textColor: Long = 0xFFFFFFFF,
    val accentColor: Long = 0xFFFFD166,
    val rimLight: Boolean = true,
    val elements: List<WidgetElement> = emptyList(),
)

@Serializable
data class AppSettings(
    val cityName: String = "Ciudad de México",
    val lat: Double = 19.4326,
    val lon: Double = -99.1332,
    val fahrenheit: Boolean = false,
    /** true = interfaz Liquid Glass, false = Material You clásico. */
    val appGlass: Boolean = true,
    /** Estilo de los widgets listos (reloj, clima, combinado). */
    val widgetStyle: BgStyle = BgStyle.GLASS,
    val tint1: Long = 0xFF8EC5FF,
    val tint2: Long = 0xFFE0C3FC,
    val widgetOpacity: Float = 0.35f,
    val widgetCorner: Int = 28,
)

@Serializable
data class WeatherData(
    val temp: Double,
    val feelsLike: Double,
    val humidity: Int,
    val wind: Double,
    val code: Int,
    val isDay: Boolean,
    val max: Double,
    val min: Double,
    val city: String,
    val fahrenheit: Boolean,
    val updatedAt: Long,
)
