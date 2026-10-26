package com.glassywidgets.data

/** Diseños de los widgets listos y plantillas de inicio para el creador. */
object Templates {

    private fun el(type: ElementType, size: Int, block: WidgetElement.() -> WidgetElement = { this }) =
        WidgetElement(type = type, size = size).block()

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
        name = "En blanco",
        elements = listOf(
            el(ElementType.TIME, 48) { copy(font = FontOpt.THIN) },
            el(ElementType.DATE, 14) { copy(colorRole = ColorRole.MUTED) },
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
