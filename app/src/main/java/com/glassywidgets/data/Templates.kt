package com.glassywidgets.data

/** Diseños de los widgets listos y plantillas de inicio para el creador. */
object Templates {

    // ---------- Widgets listos (su estilo lo controla Ajustes) ----------

    val builtinClock = WidgetDesign(
        id = "builtin_clock",
        name = "Reloj Glass",
        elements = listOf(
            WidgetElement(type = ElementType.TIME, size = 60, font = FontOpt.THIN),
            WidgetElement(type = ElementType.DATE, size = 15, font = FontOpt.MEDIUM, colorRole = ColorRole.MUTED),
        ),
    )

    val builtinWeather = WidgetDesign(
        id = "builtin_weather",
        name = "Clima Glass",
        elements = listOf(
            WidgetElement(type = ElementType.CITY, size = 12, font = FontOpt.MEDIUM, colorRole = ColorRole.MUTED, uppercase = true),
            WidgetElement(type = ElementType.WEATHER_ICON, size = 34),
            WidgetElement(type = ElementType.TEMPERATURE, size = 44, font = FontOpt.LIGHT, sameRow = true),
            WidgetElement(type = ElementType.CONDITION, size = 14, font = FontOpt.MEDIUM),
            WidgetElement(type = ElementType.MIN_MAX, size = 13, colorRole = ColorRole.MUTED),
        ),
    )

    val builtinCombined = WidgetDesign(
        id = "builtin_combined",
        name = "Reloj + Clima",
        elements = listOf(
            WidgetElement(type = ElementType.TIME, size = 52, font = FontOpt.THIN),
            WidgetElement(type = ElementType.DATE, size = 14, font = FontOpt.MEDIUM, colorRole = ColorRole.MUTED),
            WidgetElement(type = ElementType.DIVIDER),
            WidgetElement(type = ElementType.WEATHER_ICON, size = 20),
            WidgetElement(type = ElementType.TEMPERATURE, size = 20, font = FontOpt.MEDIUM, sameRow = true),
            WidgetElement(type = ElementType.CONDITION, size = 13, colorRole = ColorRole.MUTED, sameRow = true),
        ),
    )

    // ---------- Plantillas para "Crear" (ids fijos y estables) ----------

    val starterDesigns: List<WidgetDesign> = listOf(
        builtinCombined.copy(id = "tpl_glass", name = "Cristal clásico"),
        WidgetDesign(
            id = "tpl_pixel",
            name = "Pixel apilado",
            style = BgStyle.MATERIAL_YOU,
            cornerRadius = 36,
            elements = listOf(
                WidgetElement(type = ElementType.TIME, size = 72, font = FontOpt.BLACK, colorRole = ColorRole.ACCENT, timeFormat = TimeFormat.HOURS),
                WidgetElement(type = ElementType.TIME, size = 72, font = FontOpt.BLACK, timeFormat = TimeFormat.MINUTES),
                WidgetElement(type = ElementType.DATE, size = 14, font = FontOpt.MEDIUM, colorRole = ColorRole.MUTED, dateFormat = DateFormatOpt.DAY_MONTH),
            ),
        ),
        WidgetDesign(
            id = "tpl_liquid_weather",
            name = "Clima líquido",
            color1 = 0xFF4FC3F7,
            color2 = 0xFF64FFDA,
            opacity = 0.4f,
            elements = listOf(
                WidgetElement(type = ElementType.CITY, size = 12, font = FontOpt.MEDIUM, colorRole = ColorRole.MUTED, uppercase = true),
                WidgetElement(type = ElementType.WEATHER_ICON, size = 36),
                WidgetElement(type = ElementType.TEMPERATURE, size = 48, font = FontOpt.THIN, sameRow = true),
                WidgetElement(type = ElementType.CONDITION, size = 14, font = FontOpt.MEDIUM),
                WidgetElement(type = ElementType.HUMIDITY, size = 12, colorRole = ColorRole.MUTED),
                WidgetElement(type = ElementType.WIND, size = 12, colorRole = ColorRole.MUTED, sameRow = true),
            ),
        ),
        WidgetDesign(
            id = "tpl_night",
            name = "Noche minimal",
            style = BgStyle.DARK_GLASS,
            color1 = 0xFF3949AB,
            color2 = 0xFF8E24AA,
            opacity = 0.6f,
            align = HAlign.START,
            padding = 20,
            accentColor = 0xFF80DEEA,
            elements = listOf(
                WidgetElement(type = ElementType.DATE, size = 14, font = FontOpt.MEDIUM, colorRole = ColorRole.ACCENT, dateFormat = DateFormatOpt.WEEKDAY),
                WidgetElement(type = ElementType.TIME, size = 54, font = FontOpt.LIGHT),
                WidgetElement(type = ElementType.WEATHER_ICON, size = 16),
                WidgetElement(type = ElementType.TEMPERATURE, size = 16, font = FontOpt.MEDIUM, sameRow = true),
                WidgetElement(type = ElementType.MIN_MAX, size = 13, colorRole = ColorRole.MUTED, sameRow = true),
            ),
        ),
        WidgetDesign(
            id = "tpl_sunset",
            name = "Atardecer",
            style = BgStyle.GRADIENT,
            color1 = 0xFFFF7E5F,
            color2 = 0xFFFEB47B,
            opacity = 0.95f,
            elements = listOf(
                WidgetElement(type = ElementType.TIME, size = 50, font = FontOpt.MEDIUM),
                WidgetElement(type = ElementType.DATE, size = 14, font = FontOpt.MEDIUM, dateFormat = DateFormatOpt.MEDIUM),
                WidgetElement(type = ElementType.CUSTOM_TEXT, size = 13, colorRole = ColorRole.MUTED, text = "Buen día ✨"),
            ),
        ),
        WidgetDesign(
            id = "tpl_neon",
            name = "Neón mono",
            style = BgStyle.SOLID,
            color1 = 0xFF0A0F0A,
            opacity = 0.85f,
            textColor = 0xFF39FF14,
            accentColor = 0xFFFF2BD6,
            cornerRadius = 18,
            elements = listOf(
                WidgetElement(type = ElementType.TIME, size = 46, font = FontOpt.MONO, timeFormat = TimeFormat.H24),
                WidgetElement(type = ElementType.DATE, size = 13, font = FontOpt.MONO, colorRole = ColorRole.MUTED, dateFormat = DateFormatOpt.SHORT),
                WidgetElement(type = ElementType.TEMPERATURE, size = 14, font = FontOpt.MONO, colorRole = ColorRole.ACCENT),
                WidgetElement(type = ElementType.CONDITION, size = 13, font = FontOpt.MONO, sameRow = true),
            ),
        ),
    )

    val blank = WidgetDesign(
        id = "tpl_blank",
        name = "En blanco",
        elements = listOf(
            WidgetElement(type = ElementType.TIME, size = 48, font = FontOpt.THIN),
            WidgetElement(type = ElementType.DATE, size = 14, colorRole = ColorRole.MUTED),
        ),
    )

    /** Valores por defecto al añadir un elemento nuevo en el editor. */
    fun newElement(type: ElementType): WidgetElement = when (type) {
        ElementType.TIME -> WidgetElement(type = type, size = 48, font = FontOpt.THIN)
        ElementType.DATE -> WidgetElement(type = type, size = 14, font = FontOpt.MEDIUM, colorRole = ColorRole.MUTED)
        ElementType.WEATHER_ICON -> WidgetElement(type = type, size = 30)
        ElementType.TEMPERATURE -> WidgetElement(type = type, size = 32, font = FontOpt.LIGHT)
        ElementType.DIVIDER -> WidgetElement(type = type, size = 1, colorRole = ColorRole.MUTED)
        ElementType.SPACER -> WidgetElement(type = type, size = 8)
        ElementType.CUSTOM_TEXT -> WidgetElement(type = type, size = 14, text = "Hola 👋")
        else -> WidgetElement(type = type, size = 13, colorRole = ColorRole.MUTED)
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
