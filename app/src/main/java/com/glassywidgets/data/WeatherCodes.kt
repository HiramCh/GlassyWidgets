package com.glassywidgets.data

/** Códigos WMO usados por Open-Meteo → texto en español + emoji. */
object WeatherCodes {
    fun description(code: Int): String = when (code) {
        0 -> "Despejado"
        1 -> "Mayormente despejado"
        2 -> "Parcialmente nublado"
        3 -> "Nublado"
        45, 48 -> "Niebla"
        51, 53, 55 -> "Llovizna"
        56, 57 -> "Llovizna helada"
        61 -> "Lluvia ligera"
        63 -> "Lluvia"
        65 -> "Lluvia intensa"
        66, 67 -> "Lluvia helada"
        71, 73, 75, 77 -> "Nieve"
        80, 81 -> "Chubascos"
        82 -> "Chubascos fuertes"
        85, 86 -> "Chubascos de nieve"
        95 -> "Tormenta"
        96, 99 -> "Tormenta con granizo"
        else -> "—"
    }

    fun emoji(code: Int, isDay: Boolean): String = when (code) {
        0 -> if (isDay) "☀️" else "🌙"
        1 -> if (isDay) "🌤️" else "🌙"
        2 -> if (isDay) "⛅" else "☁️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55, 80, 81 -> if (isDay) "🌦️" else "🌧️"
        56, 57, 61, 63, 65, 66, 67, 82 -> "🌧️"
        71, 73, 75, 77, 85, 86 -> "🌨️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }
}
