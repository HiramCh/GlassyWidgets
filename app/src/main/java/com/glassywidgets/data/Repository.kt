package com.glassywidgets.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.glassyStore: DataStore<Preferences> by preferencesDataStore(name = "glassy")

/** Única fuente de datos de la app y de los widgets. */
class Repository private constructor(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private object Keys {
        val SETTINGS = stringPreferencesKey("settings")
        val WEATHER = stringPreferencesKey("weather")
        val DESIGNS = stringPreferencesKey("designs")
        val LAST_DESIGN = stringPreferencesKey("last_design")
    }

    private fun decodeSettings(raw: String?): AppSettings =
        raw?.let { runCatching { json.decodeFromString<AppSettings>(it) }.getOrNull() } ?: AppSettings()

    private fun decodeDesigns(raw: String?): List<WidgetDesign> =
        raw?.let { runCatching { json.decodeFromString<List<WidgetDesign>>(it) }.getOrNull() }
            ?: Templates.starterDesigns

    val settings: Flow<AppSettings> =
        context.glassyStore.data.map { decodeSettings(it[Keys.SETTINGS]) }.distinctUntilChanged()

    val weather: Flow<WeatherData?> =
        context.glassyStore.data.map { p ->
            p[Keys.WEATHER]?.let { runCatching { json.decodeFromString<WeatherData>(it) }.getOrNull() }
        }.distinctUntilChanged()

    val designs: Flow<List<WidgetDesign>> =
        context.glassyStore.data.map { decodeDesigns(it[Keys.DESIGNS]) }.distinctUntilChanged()

    val lastDesignId: Flow<String?> =
        context.glassyStore.data.map { it[Keys.LAST_DESIGN] }.distinctUntilChanged()

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        context.glassyStore.edit { p ->
            p[Keys.SETTINGS] = json.encodeToString(transform(decodeSettings(p[Keys.SETTINGS])))
        }
    }

    suspend fun saveWeather(w: WeatherData) {
        context.glassyStore.edit { it[Keys.WEATHER] = json.encodeToString(w) }
    }

    suspend fun saveDesign(design: WidgetDesign) {
        context.glassyStore.edit { p ->
            val list = decodeDesigns(p[Keys.DESIGNS]).toMutableList()
            val i = list.indexOfFirst { it.id == design.id }
            if (i >= 0) list[i] = design else list.add(0, design)
            p[Keys.DESIGNS] = json.encodeToString(list)
            p[Keys.LAST_DESIGN] = design.id
        }
    }

    suspend fun deleteDesign(id: String) {
        context.glassyStore.edit { p ->
            val list = decodeDesigns(p[Keys.DESIGNS]).filterNot { it.id == id }
            p[Keys.DESIGNS] = json.encodeToString(list)
        }
    }

    suspend fun setLastDesign(id: String) {
        context.glassyStore.edit { it[Keys.LAST_DESIGN] = id }
    }

    companion object {
        @Volatile private var instance: Repository? = null
        fun get(context: Context): Repository =
            instance ?: synchronized(this) {
                instance ?: Repository(context.applicationContext).also { instance = it }
            }
    }
}
