package com.glassywidgets.data

/** Diseños de los widgets listos y plantillas de inicio para el creador. */
object Templates {

    private fun el(type: ElementType, size: Int, block: WidgetElement.() -> WidgetElement = { this }) =
        WidgetElement(type = type, size = size).block()

    /** Elemento posicionado libremente. */
    private fun at(
        type: ElementType, size: Int, x: Float, y: Float,
        h: HAlign = HAlign.CENTER, v: VAlign = VAlign.CENTER,
        block: WidgetElement.() -> WidgetElement = { this },
    ) = WidgetElement(type = type, size = size, x = x, y = y, anchorH = h, anchorV = v).block()

    // ---------- Widgets listos (su estilo lo controla Ajustes) ----------

    val builtinClock = WidgetDesign(
        id = "builtin_clock",
        name = "Reloj",
        elements = listOf(
            el(ElementType.TIME, 60) { copy(font = FontOpt.THIN) },
            el(ElementType.DATE, 15) { copy(font = FontOpt.MEDIUM, colorRole = ColorRole.MUTED) },
        ),
    )

    val builtinAnalog = WidgetDesign(
        id = "builtin_analog",
        name = "Analógico",
        layout = LayoutMode.SPLIT,
        split = 0.45f,
        columnDivider = false,
        align2 = HAlign.START,
        elements = listOf(
            el(ElementType.ANALOG_CLOCK, 110),
            el(ElementType.DATE, 14) { copy(column = 1, font = FontOpt.MEDIUM, colorRole = ColorRole.ACCENT, dateFormat = DateFormatOpt.WEEKDAY) },
            el(ElementType.DATE, 34) { copy(column = 1, font = FontOpt.LIGHT, dateFormat = DateFormatOpt.DAY_MONTH) },
            el(ElementType.WEATHER_ICON, 16) { copy(column = 1) },
            el(ElementType.TEMPERATURE, 16) { copy(column = 1, sameRow = true, font = FontOpt.MEDIUM) },
        ),
    )

    val builtinWeather = WidgetDesign(
        id = "builtin_weather",
        name = "Clima",
        elements = listOf(
            el(ElementType.CITY, 12) { copy(font = FontOpt.MEDIUM, colorRole = ColorRole.MUTED, uppercase = true) },
            el(ElementType.WEATHER_ICON, 34),
            el(ElementType.TEMPERATURE, 44) { copy(font = FontOpt.LIGHT, sameRow = true) },
            el(ElementType.CONDITION, 14) { copy(font = FontOpt.MEDIUM) },
            el(ElementType.MIN_MAX, 13) { copy(colorRole = ColorRole.MUTED) },
        ),
    )

    val builtinForecast = WidgetDesign(
        id = "builtin_forecast",
        name = "Pronóstico",
        padding = 14,
        elements = listOf(
            el(ElementType.CITY, 13) { copy(font = FontOpt.MEDIUM, align = ElemAlign.START) },
            el(ElementType.TEMPERATURE, 13) { copy(font = FontOpt.MEDIUM, sameRow = true, colorRole = ColorRole.ACCENT) },
            el(ElementType.CONDITION, 13) { copy(sameRow = true, colorRole = ColorRole.MUTED) },
            el(ElementType.SPACER, 6),
            el(ElementType.HOURLY, 13) { copy(count = 6) },
        ),
    )

    val builtinCombined = WidgetDesign(
        id = "builtin_combined",
        name = "Reloj + Clima",
        layout = LayoutMode.SPLIT,
        split = 0.56f,
        align = HAlign.START,
        align2 = HAlign.CENTER,
        elements = listOf(
            el(ElementType.TIME, 52) { copy(font = FontOpt.THIN) },
            el(ElementType.DATE, 14) { copy(font = FontOpt.MEDIUM, colorRole = ColorRole.MUTED, dateFormat = DateFormatOpt.MEDIUM) },
            el(ElementType.WEATHER_ICON, 30) { copy(column = 1) },
            el(ElementType.TEMPERATURE, 26) { copy(column = 1, font = FontOpt.LIGHT) },
            el(ElementType.ROTATOR, 12) { copy(column = 1, colorRole = ColorRole.MUTED, rotateItems = listOf(ElementType.CONDITION, ElementType.MIN_MAX, ElementType.HUMIDITY, ElementType.WIND)) },
        ),
    )

    // ---------- Plantillas para "Crear" (ids fijos y estables) ----------

    val starterDesigns: List<WidgetDesign> = listOf(
        // Inspirado en "At a Glance" de Google Pixel
        WidgetDesign(
            id = "tpl_glance",
            name = "Pixel · De un vistazo",
            layout = LayoutMode.FREE,
            style = BgStyle.SOLID,
            opacity = 0f,
            elements = listOf(
                at(ElementType.DATE, 24, 0f, 0.36f, HAlign.START) { copy(dateFormat = DateFormatOpt.MEDIUM) },
                at(ElementType.WEATHER_ICON, 18, 0f, 0.74f, HAlign.START),
                at(ElementType.TEMPERATURE, 17, 0.1f, 0.74f, HAlign.START) { copy(font = FontOpt.MEDIUM) },
                at(ElementType.CONDITION, 14, 0.25f, 0.74f, HAlign.START) { copy(colorRole = ColorRole.MUTED) },
            ),
        ),
        // Inspirado en los widgets de Nothing Phone
        WidgetDesign(
            id = "tpl_nothing",
            name = "Nothing · Punto rojo",
            layout = LayoutMode.FREE,
            style = BgStyle.SOLID,
            color1 = 0xFF0D0D0D,
            opacity = 0.92f,
            cornerRadius = 26,
            textColor = 0xFFEDEDED,
            accentColor = 0xFFD71921,
            elements = listOf(
                at(ElementType.TIME, 54, 0f, 0f, HAlign.START, VAlign.TOP) { copy(font = FontOpt.MONO, timeFormat = TimeFormat.H24) },
                at(ElementType.DATE, 13, 0f, 1f, HAlign.START, VAlign.BOTTOM) { copy(font = FontOpt.MONO, colorRole = ColorRole.MUTED, dateFormat = DateFormatOpt.MEDIUM) },
                at(ElementType.RING, 60, 1f, 0f, HAlign.END, VAlign.TOP) { copy(font = FontOpt.MONO, colorRole = ColorRole.ACCENT) },
                at(ElementType.CUSTOM_TEXT, 10, 1f, 1f, HAlign.END, VAlign.BOTTOM) { copy(text = "● ● ●", colorRole = ColorRole.ACCENT) },
            ),
        ),
        // Inspirado en el widget del clima de iPhone
        WidgetDesign(
            id = "tpl_ios",
            name = "iOS · Clima",
            layout = LayoutMode.FREE,
            style = BgStyle.GRADIENT,
            color1 = 0xFF2F80ED,
            color2 = 0xFF56CCF2,
            opacity = 1f,
            cornerRadius = 26,
            padding = 14,
            elements = listOf(
                at(ElementType.CITY, 15, 0f, 0f, HAlign.START, VAlign.TOP) { copy(font = FontOpt.MEDIUM) },
                at(ElementType.TEMPERATURE, 44, 0f, 0.15f, HAlign.START, VAlign.TOP) { copy(font = FontOpt.THIN) },
                at(ElementType.WEATHER_ICON, 18, 1f, 0f, HAlign.END, VAlign.TOP),
                at(ElementType.CONDITION, 12, 1f, 0.22f, HAlign.END, VAlign.TOP) { copy(font = FontOpt.MEDIUM) },
                at(ElementType.MIN_MAX, 12, 1f, 0.36f, HAlign.END, VAlign.TOP),
                at(ElementType.HOURLY, 11, 0.5f, 1f, HAlign.CENTER, VAlign.BOTTOM) { copy(count = 6, w = 1f) },
            ),
        ),
        WidgetDesign(
            id = "tpl_capsules",
            name = "Cápsulas de cristal",
            layout = LayoutMode.FREE,
            padding = 14,
            elements = listOf(
                at(ElementType.SHAPE, 0, 0.5f, 0.36f) { copy(w = 1f, h = 0.6f, radius = 100, customColor = 0xFFFFFFFF, customColor2 = 0xFFFFFFFF, opacity = 0.18f) },
                at(ElementType.SHAPE, 0, 0f, 0.92f, HAlign.START) { copy(w = 0.48f, h = 0.26f, radius = 100, customColor = 0xFFFFFFFF, customColor2 = 0xFFFFFFFF, opacity = 0.18f) },
                at(ElementType.SHAPE, 0, 1f, 0.92f, HAlign.END) { copy(w = 0.48f, h = 0.26f, radius = 100, customColor = 0xFFFFFFFF, customColor2 = 0xFFFFFFFF, opacity = 0.18f) },
                at(ElementType.TIME, 46, 0.5f, 0.36f) { copy(font = FontOpt.LIGHT) },
                at(ElementType.ROTATOR, 12, 0.24f, 0.92f) { copy(font = FontOpt.MEDIUM, rotateItems = listOf(ElementType.TEMPERATURE, ElementType.CONDITION, ElementType.HUMIDITY), flipAnim = FlipAnim.SLIDE_UP) },
                at(ElementType.DATE, 12, 0.76f, 0.92f) { copy(font = FontOpt.MEDIUM, dateFormat = DateFormatOpt.MEDIUM) },
            ),
        ),
        WidgetDesign(
            id = "tpl_rings",
            name = "Anillos de actividad",
            layout = LayoutMode.FREE,
            style = BgStyle.DARK_GLASS,
            color1 = 0xFF1E3A8A,
            color2 = 0xFF7C3AED,
            opacity = 0.6f,
            elements = listOf(
                at(ElementType.RING, 64, 0.16f, 0.4f) { copy(ringSource = RingSource.BATTERY, colorRole = ColorRole.CUSTOM, customColor = 0xFF34D399) },
                at(ElementType.RING, 64, 0.5f, 0.4f) { copy(ringSource = RingSource.DAY, colorRole = ColorRole.CUSTOM, customColor = 0xFFFBBF24) },
                at(ElementType.RING, 64, 0.84f, 0.4f) { copy(ringSource = RingSource.HUMIDITY, colorRole = ColorRole.CUSTOM, customColor = 0xFF60A5FA) },
                at(ElementType.CUSTOM_TEXT, 11, 0.16f, 0.95f) { copy(text = "Batería", colorRole = ColorRole.MUTED) },
                at(ElementType.CUSTOM_TEXT, 11, 0.5f, 0.95f) { copy(text = "Día", colorRole = ColorRole.MUTED) },
                at(ElementType.CUSTOM_TEXT, 11, 0.84f, 0.95f) { copy(text = "Humedad", colorRole = ColorRole.MUTED) },
            ),
        ),
        WidgetDesign(
            id = "tpl_bigclock",
            name = "Reloj gigante",
            layout = LayoutMode.FREE,
            style = BgStyle.MATERIAL_YOU,
            cornerRadius = 36,
            elements = listOf(
                at(ElementType.TIME, 96, 0f, 0.5f, HAlign.START) { copy(font = FontOpt.BLACK, colorRole = ColorRole.ACCENT, timeFormat = TimeFormat.HOURS) },
                at(ElementType.TIME, 96, 0.44f, 0.5f, HAlign.START) { copy(font = FontOpt.BLACK, timeFormat = TimeFormat.MINUTES, alpha = 0.45f) },
                at(ElementType.DATE, 14, 1f, 0f, HAlign.END, VAlign.TOP) { copy(font = FontOpt.MEDIUM, dateFormat = DateFormatOpt.WEEKDAY) },
                at(ElementType.DATE, 13, 1f, 0.2f, HAlign.END, VAlign.TOP) { copy(colorRole = ColorRole.MUTED, dateFormat = DateFormatOpt.DAY_MONTH) },
                at(ElementType.TEMPERATURE, 15, 1f, 1f, HAlign.END, VAlign.BOTTOM) { copy(font = FontOpt.MEDIUM) },
                at(ElementType.WEATHER_ICON, 15, 0.86f, 1f, HAlign.END, VAlign.BOTTOM),
            ),
        ),
        WidgetDesign(
            id = "tpl_poster",
            name = "Póster con foto",
            layout = LayoutMode.FREE,
            padding = 12,
            elements = listOf(
                at(ElementType.PHOTO, 0, 0f, 0.5f, HAlign.START) { copy(w = 0.42f, h = 1f, radius = 20, customColor = 0xFFFF9A9E, customColor2 = 0xFFFAD0C4) },
                at(ElementType.TIME, 40, 0.48f, 0.3f, HAlign.START) { copy(font = FontOpt.LIGHT) },
                at(ElementType.DATE, 13, 0.48f, 0.6f, HAlign.START) { copy(colorRole = ColorRole.MUTED, dateFormat = DateFormatOpt.LONG) },
                at(ElementType.ROTATOR, 12, 0.48f, 0.85f, HAlign.START) { copy(colorRole = ColorRole.ACCENT, flipAnim = FlipAnim.SLIDE_SIDE) },
            ),
        ),
        WidgetDesign(
            id = "tpl_line",
            name = "Minimal con línea",
            layout = LayoutMode.FREE,
            opacity = 0.22f,
            elements = listOf(
                at(ElementType.TIME, 46, 0f, 0.5f, HAlign.START) { copy(font = FontOpt.THIN) },
                at(ElementType.SHAPE, 0, 0.5f, 0.5f) { copy(shapeStyle = BgStyle.SOLID, w = 0.008f, h = 0.7f, radius = 2, customColor = 0xFFFFFFFF, opacity = 0.6f) },
                at(ElementType.DATE, 16, 0.56f, 0.28f, HAlign.START) { copy(font = FontOpt.MEDIUM, dateFormat = DateFormatOpt.WEEKDAY) },
                at(ElementType.DATE, 13, 0.56f, 0.52f, HAlign.START) { copy(colorRole = ColorRole.MUTED, dateFormat = DateFormatOpt.DAY_MONTH) },
                at(ElementType.WEATHER_ICON, 14, 0.56f, 0.78f, HAlign.START),
                at(ElementType.TEMPERATURE, 14, 0.64f, 0.78f, HAlign.START) { copy(font = FontOpt.MEDIUM) },
            ),
        ),
        builtinCombined.copy(id = "tpl_split", name = "Dividido: reloj | clima"),
        WidgetDesign(
            id = "tpl_pixel",
            name = "Pixel apilado",
            style = BgStyle.MATERIAL_YOU,
            cornerRadius = 36,
            layout = LayoutMode.SPLIT,
            split = 0.42f,
            align2 = HAlign.START,
            elements = listOf(
                el(ElementType.TIME, 64) { copy(font = FontOpt.BLACK, colorRole = ColorRole.ACCENT, timeFormat = TimeFormat.HOURS) },
                el(ElementType.TIME, 64) { copy(font = FontOpt.BLACK, timeFormat = TimeFormat.MINUTES) },
                el(ElementType.DATE, 16) { copy(column = 1, font = FontOpt.MEDIUM, dateFormat = DateFormatOpt.WEEKDAY) },
                el(ElementType.DATE, 14) { copy(column = 1, colorRole = ColorRole.MUTED, dateFormat = DateFormatOpt.DAY_MONTH) },
                el(ElementType.SPACER, 8) { copy(column = 1) },
                el(ElementType.WEATHER_ICON, 18) { copy(column = 1) },
                el(ElementType.TEMPERATURE, 18) { copy(column = 1, sameRow = true, font = FontOpt.MEDIUM) },
                el(ElementType.BATTERY, 13) { copy(column = 1, colorRole = ColorRole.MUTED) },
            ),
        ),
        WidgetDesign(
            id = "tpl_analog_glass",
            name = "Analógico de cristal",
            color1 = 0xFFB388FF,
            color2 = 0xFF80DEEA,
            layout = LayoutMode.SPLIT,
            split = 0.5f,
            columnDivider = false,
            align2 = HAlign.START,
            elements = listOf(
                el(ElementType.ANALOG_CLOCK, 120) { copy(showSeconds = true) },
                el(ElementType.GREETING, 13) { copy(column = 1, colorRole = ColorRole.ACCENT, font = FontOpt.MEDIUM) },
                el(ElementType.DATE, 26) { copy(column = 1, font = FontOpt.LIGHT, dateFormat = DateFormatOpt.WEEKDAY) },
                el(ElementType.DATE, 14) { copy(column = 1, colorRole = ColorRole.MUTED, dateFormat = DateFormatOpt.DAY_MONTH) },
                el(ElementType.ROTATOR, 13) { copy(column = 1, flipAnim = FlipAnim.SLIDE_UP) },
            ),
        ),
        WidgetDesign(
            id = "tpl_forecast",
            name = "Pronóstico líquido",
            color1 = 0xFF4FC3F7,
            color2 = 0xFF64FFDA,
            opacity = 0.4f,
            padding = 14,
            elements = listOf(
                el(ElementType.WEATHER_ICON, 30) { copy(align = ElemAlign.START) },
                el(ElementType.TEMPERATURE, 40) { copy(sameRow = true, font = FontOpt.THIN) },
                el(ElementType.ROTATOR, 13) { copy(sameRow = true, colorRole = ColorRole.MUTED, flipAnim = FlipAnim.SLIDE_SIDE) },
                el(ElementType.DIVIDER, 1),
                el(ElementType.HOURLY, 12) { copy(count = 6) },
            ),
        ),
        WidgetDesign(
            id = "tpl_week",
            name = "Semana",
            style = BgStyle.DARK_GLASS,
            color1 = 0xFF3949AB,
            color2 = 0xFF8E24AA,
            opacity = 0.6f,
            accentColor = 0xFF80DEEA,
            elements = listOf(
                el(ElementType.CITY, 12) { copy(uppercase = true, colorRole = ColorRole.ACCENT, font = FontOpt.MEDIUM, align = ElemAlign.START) },
                el(ElementType.SPACER, 6),
                el(ElementType.DAILY, 13) { copy(count = 5) },
            ),
        ),
        WidgetDesign(
            id = "tpl_day",
            name = "Mi día",
            style = BgStyle.GRADIENT,
            color1 = 0xFFFF7E5F,
            color2 = 0xFFFEB47B,
            opacity = 0.95f,
            align = HAlign.START,
            padding = 18,
            elements = listOf(
                el(ElementType.GREETING, 14) { copy(font = FontOpt.MEDIUM) },
                el(ElementType.TIME, 46) { copy(font = FontOpt.MEDIUM, timeFormat = TimeFormat.WITH_SECONDS) },
                el(ElementType.DAY_PROGRESS, 8),
                el(ElementType.SPACER, 4),
                el(ElementType.BATTERY_BAR, 8),
            ),
        ),
        WidgetDesign(
            id = "tpl_world",
            name = "Hora mundial",
            style = BgStyle.SOLID,
            color1 = 0xFF0A0F0A,
            opacity = 0.85f,
            textColor = 0xFF39FF14,
            accentColor = 0xFFFF2BD6,
            cornerRadius = 18,
            layout = LayoutMode.SPLIT,
            elements = listOf(
                el(ElementType.TIME, 40) { copy(font = FontOpt.MONO, timeFormat = TimeFormat.H24) },
                el(ElementType.CITY, 12) { copy(font = FontOpt.MONO, colorRole = ColorRole.MUTED) },
                el(ElementType.WORLD_CLOCK, 18) { copy(column = 1, font = FontOpt.MONO, timeFormat = TimeFormat.H24, zoneLabel = "Madrid", timeZone = "Europe/Madrid") },
                el(ElementType.WORLD_CLOCK, 18) { copy(column = 1, font = FontOpt.MONO, timeFormat = TimeFormat.H24, zoneLabel = "Tokio", timeZone = "Asia/Tokyo") },
                el(ElementType.WORLD_CLOCK, 18) { copy(column = 1, font = FontOpt.MONO, timeFormat = TimeFormat.H24, zoneLabel = "N. York", timeZone = "America/New_York") },
            ),
        ),
    )

    val blank = WidgetDesign(
        id = "tpl_blank",
        name = "En blanco (libre)",
        layout = LayoutMode.FREE,
        elements = listOf(
            at(ElementType.TIME, 48, 0.5f, 0.4f) { copy(font = FontOpt.THIN) },
            at(ElementType.DATE, 14, 0.5f, 0.8f) { copy(colorRole = ColorRole.MUTED) },
        ),
    )

    /** Valores por defecto al añadir un elemento nuevo en el editor. */
    fun newElement(type: ElementType, column: Int): WidgetElement {
        val base = WidgetElement(type = type, column = column)
        return when (type) {
            ElementType.TIME -> base.copy(size = 48, font = FontOpt.THIN)
            ElementType.DATE -> base.copy(size = 14, font = FontOpt.MEDIUM, colorRole = ColorRole.MUTED)
            ElementType.ANALOG_CLOCK -> base.copy(size = 100)
            ElementType.WORLD_CLOCK -> base.copy(size = 16)
            ElementType.GREETING -> base.copy(size = 15, font = FontOpt.MEDIUM)
            ElementType.WEATHER_ICON -> base.copy(size = 30)
            ElementType.TEMPERATURE -> base.copy(size = 32, font = FontOpt.LIGHT)
            ElementType.HOURLY, ElementType.DAILY -> base.copy(size = 12)
            ElementType.ROTATOR -> base.copy(size = 14)
            ElementType.DAY_PROGRESS, ElementType.BATTERY_BAR -> base.copy(size = 8, colorRole = ColorRole.ACCENT)
            ElementType.DIVIDER -> base.copy(size = 1, colorRole = ColorRole.MUTED)
            ElementType.SPACER -> base.copy(size = 8)
            ElementType.SHAPE -> base.copy(w = 0.7f, h = 0.6f, radius = 28, customColor = 0xFFFFFFFF, customColor2 = 0xFFFFFFFF, opacity = 0.2f)
            ElementType.PHOTO -> base.copy(w = 0.42f, h = 0.9f, radius = 22, customColor = 0xFF8EC5FF, customColor2 = 0xFFE0C3FC)
            ElementType.RING -> base.copy(size = 64, colorRole = ColorRole.ACCENT)
            ElementType.CUSTOM_TEXT -> base.copy(size = 14, text = "Hola 👋")
            ElementType.EMOJI -> base.copy(size = 32, text = "✨")
            else -> base.copy(size = 13, colorRole = ColorRole.MUTED)
        }
    }
}

/** Aplica el estilo global de Ajustes a un widget listo. */
fun WidgetDesign.withGlobal(s: AppSettings): WidgetDesign = copy(
    style = s.widgetStyle,
    color1 = s.tint1,
    color2 = s.tint2,
    opacity = s.widgetOpacity,
    cornerRadius = s.widgetCorner,
)
