package com.glassywidgets.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.glassywidgets.data.AppSettings
import com.glassywidgets.data.Repository
import com.glassywidgets.data.WeatherApi
import com.glassywidgets.data.WeatherData
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.widget.CustomWidgetReceiver
import com.glassywidgets.widget.WidgetPin
import com.glassywidgets.widget.WidgetUpdater
import com.glassywidgets.work.WeatherSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx: Context get() = getApplication()
    private val repo = Repository.get(app)

    val settings: StateFlow<AppSettings> = repo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    val weather: StateFlow<WeatherData?> = repo.weather.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val designs: StateFlow<List<WidgetDesign>> = repo.designs.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var refreshing by mutableStateOf(false)
        private set
    var searching by mutableStateOf(false)
        private set
    val searchResults = mutableStateListOf<WeatherApi.GeoResult>()

    private fun toast(msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()

    fun refreshWeather(silent: Boolean = false) {
        if (refreshing) return
        viewModelScope.launch {
            refreshing = true
            runCatching { WeatherSync.sync(ctx) }
                .onFailure { if (!silent) toast("No se pudo actualizar el clima. Revisa tu conexión.") }
            refreshing = false
        }
    }

    /** Al abrir la app: refresca si no hay datos o tienen más de 15 minutos. */
    fun refreshIfStale() {
        viewModelScope.launch {
            val w = repo.weather.first()
            if (w == null || System.currentTimeMillis() - w.updatedAt > 15 * 60_000) refreshWeather(silent = true)
        }
    }

    private var widgetRefreshJob: Job? = null

    /** Refresca los widgets con un pequeño retraso para no saturar al mover sliders. */
    private fun scheduleWidgetRefresh() {
        widgetRefreshJob?.cancel()
        widgetRefreshJob = viewModelScope.launch {
            delay(350)
            WidgetUpdater.updateAll(ctx)
        }
    }

    fun updateSettings(refetch: Boolean = false, transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            repo.updateSettings(transform)
            if (refetch) refreshWeather() else scheduleWidgetRefresh()
        }
    }

    fun searchCity(query: String) {
        viewModelScope.launch {
            searching = true
            val res = runCatching { WeatherApi.searchCity(query) }.getOrElse {
                toast("Error al buscar la ciudad")
                emptyList()
            }
            searchResults.clear()
            searchResults.addAll(res)
            if (res.isEmpty()) toast("Sin resultados")
            searching = false
        }
    }

    fun selectCity(g: WeatherApi.GeoResult) {
        searchResults.clear()
        updateSettings(refetch = true) { it.copy(cityName = g.name, lat = g.latitude, lon = g.longitude) }
    }

    fun useMyLocation() {
        viewModelScope.launch {
            val loc = currentLocation()
            if (loc == null) {
                toast("No se pudo obtener tu ubicación. ¿Está activado el GPS?")
                return@launch
            }
            val name = cityName(loc) ?: "Mi ubicación"
            updateSettings(refetch = true) { it.copy(cityName = name, lat = loc.latitude, lon = loc.longitude) }
        }
    }

    fun saveDesign(d: WidgetDesign, notify: Boolean = true) {
        viewModelScope.launch {
            repo.saveDesign(d)
            WidgetUpdater.updateAll(ctx)
            if (notify) toast("Diseño guardado")
        }
    }

    fun deleteDesign(id: String) {
        viewModelScope.launch {
            repo.deleteDesign(id)
            WidgetUpdater.updateAll(ctx)
        }
    }

    fun pinBuiltin(receiver: Class<*>) {
        if (!WidgetPin.request(ctx, receiver)) toast(PIN_FALLBACK)
    }

    /** Guarda el diseño, lo marca como el "último" y pide anclar un widget personalizado. */
    fun pinCustom(d: WidgetDesign) {
        viewModelScope.launch {
            repo.saveDesign(d)
            repo.setLastDesign(d.id)
            if (!WidgetPin.request(ctx, CustomWidgetReceiver::class.java)) toast(PIN_FALLBACK)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun currentLocation(): Location? {
        val lm = ctx.getSystemService(LocationManager::class.java) ?: return null
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            for (p in providers) {
                val loc = withTimeoutOrNull(8_000) {
                    suspendCancellableCoroutine<Location?> { cont ->
                        lm.getCurrentLocation(p, null, ctx.mainExecutor) { cont.resume(it) }
                    }
                }
                if (loc != null) return loc
            }
        }
        return (providers + LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    @Suppress("DEPRECATION")
    private suspend fun cityName(loc: Location): String? = withContext(Dispatchers.IO) {
        runCatching {
            val a = Geocoder(ctx, Locale.getDefault()).getFromLocation(loc.latitude, loc.longitude, 1)?.firstOrNull()
            a?.locality ?: a?.subAdminArea ?: a?.adminArea
        }.getOrNull()
    }

    companion object {
        const val PIN_FALLBACK = "Tu launcher no permite anclar desde la app. Mantén presionada la pantalla de inicio → Widgets → Glassy Widgets."
    }
}
