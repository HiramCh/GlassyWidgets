package com.glassywidgets.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Cliente mínimo de Open-Meteo (gratis, sin API key).
 * https://open-meteo.com/en/docs
 */
object WeatherApi {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Serializable
    private data class ForecastResponse(
        @SerialName("utc_offset_seconds") val utcOffset: Int = 0,
        val current: Current,
        val hourly: Hourly? = null,
        val daily: Daily,
    )

    @Serializable
    private data class Current(
        @SerialName("temperature_2m") val temperature: Double,
        @SerialName("apparent_temperature") val apparent: Double,
        @SerialName("relative_humidity_2m") val humidity: Double,
        @SerialName("wind_speed_10m") val wind: Double,
        @SerialName("weather_code") val code: Int,
        @SerialName("is_day") val isDay: Int,
    )

    @Serializable
    private data class Hourly(
        val time: List<Long> = emptyList(),
        @SerialName("temperature_2m") val temp: List<Double?> = emptyList(),
        @SerialName("weather_code") val code: List<Int?> = emptyList(),
        @SerialName("is_day") val isDay: List<Int?> = emptyList(),
        @SerialName("precipitation_probability") val precip: List<Int?> = emptyList(),
    )

    @Serializable
    private data class Daily(
        val time: List<Long> = emptyList(),
        @SerialName("temperature_2m_max") val max: List<Double?> = emptyList(),
        @SerialName("temperature_2m_min") val min: List<Double?> = emptyList(),
        @SerialName("weather_code") val code: List<Int?> = emptyList(),
        val sunrise: List<Long?> = emptyList(),
        val sunset: List<Long?> = emptyList(),
        @SerialName("uv_index_max") val uv: List<Double?> = emptyList(),
        @SerialName("precipitation_probability_max") val precip: List<Int?> = emptyList(),
    )

    @Serializable
    data class GeoResult(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val country: String? = null,
        val admin1: String? = null,
    ) {
        val subtitle: String get() = listOfNotNull(admin1, country).joinToString(", ")
    }

    @Serializable
    private data class GeoResponse(val results: List<GeoResult>? = null)

    suspend fun fetch(lat: Double, lon: Double, fahrenheit: Boolean, city: String): WeatherData =
        withContext(Dispatchers.IO) {
            val url = buildString {
                append("https://api.open-meteo.com/v1/forecast")
                append("?latitude=").append(String.format(Locale.US, "%.4f", lat))
                append("&longitude=").append(String.format(Locale.US, "%.4f", lon))
                append("&current=temperature_2m,apparent_temperature,relative_humidity_2m,wind_speed_10m,weather_code,is_day")
                append("&hourly=temperature_2m,weather_code,is_day,precipitation_probability&forecast_hours=24")
                append("&daily=temperature_2m_max,temperature_2m_min,weather_code,sunrise,sunset,uv_index_max,precipitation_probability_max")
                append("&forecast_days=7&timezone=auto&timeformat=unixtime")
                if (fahrenheit) append("&temperature_unit=fahrenheit")
            }
            val r = json.decodeFromString<ForecastResponse>(get(url))
            val h = r.hourly
            val hourly = h?.time?.mapIndexedNotNull { i, t ->
                val temp = h.temp.getOrNull(i) ?: return@mapIndexedNotNull null
                HourPoint(
                    time = t,
                    temp = temp,
                    code = h.code.getOrNull(i) ?: 0,
                    isDay = (h.isDay.getOrNull(i) ?: 1) == 1,
                    precip = h.precip.getOrNull(i) ?: 0,
                )
            }.orEmpty()
            val d = r.daily
            val daily = d.time.mapIndexedNotNull { i, t ->
                DayPoint(
                    time = t,
                    max = d.max.getOrNull(i) ?: return@mapIndexedNotNull null,
                    min = d.min.getOrNull(i) ?: return@mapIndexedNotNull null,
                    code = d.code.getOrNull(i) ?: 0,
                )
            }
            WeatherData(
                temp = r.current.temperature,
                feelsLike = r.current.apparent,
                humidity = r.current.humidity.roundToInt(),
                wind = r.current.wind,
                code = r.current.code,
                isDay = r.current.isDay == 1,
                max = daily.firstOrNull()?.max ?: r.current.temperature,
                min = daily.firstOrNull()?.min ?: r.current.temperature,
                city = city,
                fahrenheit = fahrenheit,
                updatedAt = System.currentTimeMillis(),
                precipProb = d.precip.firstOrNull() ?: hourly.firstOrNull()?.precip ?: 0,
                uvMax = d.uv.firstOrNull() ?: 0.0,
                sunrise = d.sunrise.firstOrNull() ?: 0,
                sunset = d.sunset.firstOrNull() ?: 0,
                utcOffset = r.utcOffset,
                hourly = hourly,
                daily = daily,
            )
        }

    suspend fun searchCity(query: String): List<GeoResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val q = URLEncoder.encode(query.trim(), "UTF-8")
        val url = "https://geocoding-api.open-meteo.com/v1/search?name=$q&count=8&language=es&format=json"
        json.decodeFromString<GeoResponse>(get(url)).results.orEmpty()
    }

    private fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        return try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("User-Agent", "GlassyWidgets/1.1")
            if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
