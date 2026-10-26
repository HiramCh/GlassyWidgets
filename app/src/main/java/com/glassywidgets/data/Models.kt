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

/** Distribución del widget: una columna o partido a la mitad. */
@Serializable
enum class LayoutMode(val label: String) { FREE("Libre"), SINGLE("Una columna"), SPLIT("Dos columnas") }

@Serializable
enum class RingSource(val label: String) { BATTERY("Batería"), DAY("Día"), HUMIDITY("Humedad"), PRECIP("Lluvia") }

@Serializable
enum class HAlign(val label: String) { START("Izquierda"), CENTER("Centro"), END("Derecha") }

@Serializable
enum class VAlign(val label: String) { TOP("Arriba"), CENTER("Centro"), BOTTOM("Abajo") }

/** Alineación propia de un elemento (AUTO = la de su columna). */
@Serializable
enum class ElemAlign(val label: String) { AUTO("Auto"), START("Izquierda"), CENTER("Centro"), END("Derecha") }

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

enum class Category(val label: String) {
    TIME("Hora y fecha"),
    WEATHER("Clima"),
    FORECAST("Pronóstico y animados"),
    SYSTEM("Teléfono"),
    DECOR("Decoración"),
}

@Serializable
enum class ElementType(val label: String, val emoji: String, val category: Category) {
    TIME("Hora", "🕒", Category.TIME),
    DATE("Fecha", "📅", Category.TIME),
    ANALOG_CLOCK("Reloj analógico", "🕰️", Category.TIME),
    WORLD_CLOCK("Hora mundial", "🌍", Category.TIME),
    GREETING("Saludo", "👋", Category.TIME),
    DAY_PROGRESS("Progreso del día", "📊", Category.TIME),
    NEXT_ALARM("Próxima alarma", "⏰", Category.TIME),

    WEATHER_ICON("Icono del clima", "⛅", Category.WEATHER),
    TEMPERATURE("Temperatura", "🌡️", Category.WEATHER),
    CONDITION("Condición", "☁️", Category.WEATHER),
    MIN_MAX("Máx / Mín", "↕️", Category.WEATHER),
    FEELS_LIKE("Sensación", "🤒", Category.WEATHER),
    HUMIDITY("Humedad", "💧", Category.WEATHER),
    WIND("Viento", "🌬️", Category.WEATHER),
    PRECIP("Prob. de lluvia", "☔", Category.WEATHER),
    UV("Índice UV", "🕶️", Category.WEATHER),
    SUNRISE("Amanecer", "🌅", Category.WEATHER),
    SUNSET("Atardecer", "🌇", Category.WEATHER),
    CITY("Ciudad", "📍", Category.WEATHER),

    HOURLY("Pronóstico por horas", "🕐", Category.FORECAST),
    DAILY("Próximos días", "🗓️", Category.FORECAST),
    ROTATOR("Carrusel animado", "🎞️", Category.FORECAST),

    BATTERY("Batería", "🔋", Category.SYSTEM),
    BATTERY_BAR("Barra de batería", "🪫", Category.SYSTEM),
    RING("Anillo de progreso", "⭕", Category.SYSTEM),

    CUSTOM_TEXT("Texto libre", "✏️", Category.DECOR),
    EMOJI("Emoji / sticker", "✨", Category.DECOR),
    DIVIDER("Separador", "➖", Category.DECOR),
    SHAPE("Forma / cápsula", "🔷", Category.DECOR),
    PHOTO("Foto", "🖼️", Category.DECOR),
    SPACER("Espacio", "↕", Category.DECOR),
    ;

    /** Elementos que muestran texto (aceptan tipografía y mayúsculas). */
    val isText: Boolean
        get() = this !in setOf(WEATHER_ICON, EMOJI, ANALOG_CLOCK, DIVIDER, SPACER, DAY_PROGRESS, BATTERY_BAR, HOURLY, DAILY, SHAPE, PHOTO, RING)

    /** Elementos que se estiran a todo el ancho de su columna. */
    val isStretch: Boolean
        get() = this in setOf(HOURLY, DAILY, DAY_PROGRESS, BATTERY_BAR, DIVIDER)

    /** Elementos con ancho y alto propios (en modo libre o columnas). */
    val isBox: Boolean
        get() = this == SHAPE || this == PHOTO

    /** Elementos actualizados por el propio sistema (TextClock). */
    val isLiveClock: Boolean
        get() = this == TIME || this == DATE || this == WORLD_CLOCK
}

/** Tipos que se pueden poner dentro del carrusel animado. */
val ROTATOR_OPTIONS = listOf(
    ElementType.CONDITION, ElementType.TEMPERATURE, ElementType.MIN_MAX, ElementType.FEELS_LIKE,
    ElementType.HUMIDITY, ElementType.WIND, ElementType.PRECIP, ElementType.UV, ElementType.SUNRISE,
    ElementType.SUNSET, ElementType.CITY, ElementType.BATTERY, ElementType.NEXT_ALARM, ElementType.GREETING,
)

@Serializable
enum class TimeFormat(val label: String, val p12: String, val p24: String) {
    AUTO("Automático", "h:mm", "HH:mm"),
    H24("24 h", "HH:mm", "HH:mm"),
    H12("12 h", "h:mm", "h:mm"),
    H12_AMPM("12 h + AM/PM", "h:mm a", "h:mm a"),
    WITH_SECONDS("Con segundos", "h:mm:ss", "HH:mm:ss"),
    HOURS("Solo horas", "hh", "HH"),
    MINUTES("Solo minutos", "mm", "mm"),
    SECONDS("Solo segundos", "ss", "ss"),
}

@Serializable
enum class DateFormatOpt(val label: String, val pattern: String) {
    LONG("Lunes 3 de marzo", "EEEE d 'de' MMMM"),
    MEDIUM("lun, 3 mar", "EEE, d MMM"),
    SHORT("03/03/2026", "dd/MM/yyyy"),
    WEEKDAY("Lunes", "EEEE"),
    WEEKDAY_SHORT("lun", "EEE"),
    DAY("3", "d"),
    DAY_MONTH("3 mar", "d MMM"),
    MONTH("Marzo", "MMMM"),
    YEAR("2026", "yyyy"),
}

@Serializable
enum class FlipAnim(val label: String) { FADE("Desvanecer"), SLIDE_UP("Subir"), SLIDE_SIDE("Deslizar") }

@Serializable
enum class DialStyle(val label: String) { GLASS("Esfera de cristal"), MINIMAL("Minimal") }

val WORLD_ZONES = listOf(
    "Ciudad de México" to "America/Mexico_City",
    "Nueva York" to "America/New_York",
    "Los Ángeles" to "America/Los_Angeles",
    "Bogotá" to "America/Bogota",
    "Buenos Aires" to "America/Argentina/Buenos_Aires",
    "Madrid" to "Europe/Madrid",
    "Londres" to "Europe/London",
    "París" to "Europe/Paris",
    "Dubái" to "Asia/Dubai",
    "Tokio" to "Asia/Tokyo",
    "Seúl" to "Asia/Seoul",
    "Sídney" to "Australia/Sydney",
)

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
    /** 0 = columna izquierda, 1 = columna derecha (solo en diseño de dos columnas). */
    val column: Int = 0,
    val align: ElemAlign = ElemAlign.AUTO,
    val timeFormat: TimeFormat = TimeFormat.AUTO,
    val dateFormat: DateFormatOpt = DateFormatOpt.LONG,
    val text: String = "",
    val uppercase: Boolean = false,
    /** Horas o días a mostrar en los pronósticos. */
    val count: Int = 5,
    val timeZone: String = "Europe/Madrid",
    val zoneLabel: String = "Madrid",
    val rotateItems: List<ElementType> = listOf(ElementType.CONDITION, ElementType.FEELS_LIKE, ElementType.HUMIDITY, ElementType.WIND),
    val flipAnim: FlipAnim = FlipAnim.FADE,
    /** Segundos entre cada cambio del carrusel. */
    val interval: Int = 4,
    val showSeconds: Boolean = true,
    val dialStyle: DialStyle = DialStyle.GLASS,
    val showLabel: Boolean = true,
    // ---- Modo libre ----
    /** Posición del punto de anclaje dentro del widget (0–1). */
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val anchorH: HAlign = HAlign.CENTER,
    val anchorV: VAlign = VAlign.CENTER,
    /** Ancho y alto relativos (formas, fotos, barras y pronósticos). */
    val w: Float = 0.5f,
    val h: Float = 0.4f,
    // ---- Formas y fotos ----
    val radius: Int = 20,
    val shapeStyle: BgStyle = BgStyle.GLASS,
    val customColor2: Long = 0xFFE0C3FC,
    val opacity: Float = 0.35f,
    /** Transparencia general del elemento (1 = opaco). */
    val alpha: Float = 1f,
    val ringSource: RingSource = RingSource.BATTERY,
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
    val layout: LayoutMode = LayoutMode.SINGLE,
    /** Ancho de la columna izquierda (0.3 – 0.7). */
    val split: Float = 0.5f,
    val columnDivider: Boolean = true,
    val align: HAlign = HAlign.CENTER,
    val align2: HAlign = HAlign.CENTER,
    val vAlign: VAlign = VAlign.CENTER,
    val vAlign2: VAlign = VAlign.CENTER,
    val textColor: Long = 0xFFFFFFFF,
    val accentColor: Long = 0xFFFFD166,
    val rimLight: Boolean = true,
    val elements: List<WidgetElement> = emptyList(),
    /** Tamaño de referencia (dp) con el que se diseñó en modo libre. */
    val refW: Float = 316f,
    val refH: Float = 150f,
    /** Imán: ajusta a centros y bordes al arrastrar. */
    val snap: Boolean = true,
)

@Serializable
data class AppSettings(
    val cityName: String = "Ciudad de México",
    val lat: Double = 19.4326,
    val lon: Double = -99.1332,
    val fahrenheit: Boolean = false,
    /** true = interfaz Liquid Glass, false = Material You clásico. */
    val appGlass: Boolean = true,
    /** Estilo de los widgets listos. */
    val widgetStyle: BgStyle = BgStyle.GLASS,
    val tint1: Long = 0xFF8EC5FF,
    val tint2: Long = 0xFFE0C3FC,
    val widgetOpacity: Float = 0.35f,
    val widgetCorner: Int = 28,
    /** Fondo de pantalla simulado para las vistas previas. */
    val wallpaper: Int = 0,
)

@Serializable
data class HourPoint(val time: Long, val temp: Double, val code: Int, val isDay: Boolean, val precip: Int = 0)

@Serializable
data class DayPoint(val time: Long, val max: Double, val min: Double, val code: Int)

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
    val precipProb: Int = 0,
    val uvMax: Double = 0.0,
    /** Epoch en segundos. */
    val sunrise: Long = 0,
    val sunset: Long = 0,
    val utcOffset: Int = 0,
    val hourly: List<HourPoint> = emptyList(),
    val daily: List<DayPoint> = emptyList(),
)
