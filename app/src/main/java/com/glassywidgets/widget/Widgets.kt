package com.glassywidgets.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.glassywidgets.MainActivity
import com.glassywidgets.data.AppSettings
import com.glassywidgets.data.Repository
import com.glassywidgets.data.SystemInfo
import com.glassywidgets.data.Templates
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.withGlobal
import com.glassywidgets.work.WeatherWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Base para todos los widgets: cada uno solo decide QUÉ diseño mostrar;
 * el dibujo lo hace [GlanceDesign]. Los datos se observan como Flow para que
 * el widget se redibuje cuando cambian el clima, los ajustes o los diseños.
 */
abstract class DesignWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    protected abstract fun resolve(
        widgetState: Preferences,
        settings: AppSettings,
        designs: List<WidgetDesign>,
        lastDesignId: String?,
    ): WidgetDesign

    protected open suspend fun prepare(context: Context, id: GlanceId, designs: List<WidgetDesign>, lastDesignId: String?) = Unit

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = Repository.get(context)
        val s0 = repo.settings.first()
        val w0 = repo.weather.first()
        val d0 = repo.designs.first()
        val l0 = repo.lastDesignId.first()
        prepare(context, id, d0, l0)

        provideContent {
            val settings by repo.settings.collectAsState(s0)
            val weather by repo.weather.collectAsState(w0)
            val designs by repo.designs.collectAsState(d0)
            val last by repo.lastDesignId.collectAsState(l0)
            val design = resolve(currentState(), settings, designs, last)
            GlanceTheme {
                GlanceDesign(
                    design = design,
                    weather = weather,
                    sys = SystemInfo.read(context),
                    modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
                )
            }
        }
    }
}

class ClockWidget : DesignWidget() {
    override fun resolve(widgetState: Preferences, settings: AppSettings, designs: List<WidgetDesign>, lastDesignId: String?) =
        Templates.builtinClock.withGlobal(settings)
}

class AnalogWidget : DesignWidget() {
    override fun resolve(widgetState: Preferences, settings: AppSettings, designs: List<WidgetDesign>, lastDesignId: String?) =
        Templates.builtinAnalog.withGlobal(settings)
}

class WeatherWidget : DesignWidget() {
    override fun resolve(widgetState: Preferences, settings: AppSettings, designs: List<WidgetDesign>, lastDesignId: String?) =
        Templates.builtinWeather.withGlobal(settings)
}

class ForecastWidget : DesignWidget() {
    override fun resolve(widgetState: Preferences, settings: AppSettings, designs: List<WidgetDesign>, lastDesignId: String?) =
        Templates.builtinForecast.withGlobal(settings)
}

class CombinedWidget : DesignWidget() {
    override fun resolve(widgetState: Preferences, settings: AppSettings, designs: List<WidgetDesign>, lastDesignId: String?) =
        Templates.builtinCombined.withGlobal(settings)
}

/** Widget que muestra un diseño creado por el usuario. */
class CustomWidget : DesignWidget() {
    companion object {
        val DESIGN_KEY = stringPreferencesKey("design_id")
    }

    /** Si el widget aún no tiene diseño asignado, fija el último editado/anclado. */
    override suspend fun prepare(context: Context, id: GlanceId, designs: List<WidgetDesign>, lastDesignId: String?) {
        val state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        if (state[DESIGN_KEY] == null) {
            val chosen = lastDesignId ?: designs.firstOrNull()?.id ?: return
            updateAppWidgetState(context, id) { it[DESIGN_KEY] = chosen }
        }
    }

    override fun resolve(widgetState: Preferences, settings: AppSettings, designs: List<WidgetDesign>, lastDesignId: String?): WidgetDesign {
        val id = widgetState[DESIGN_KEY] ?: lastDesignId
        return designs.firstOrNull { it.id == id } ?: designs.firstOrNull() ?: Templates.blank
    }
}

abstract class BaseReceiver : GlanceAppWidgetReceiver() {
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherWorker.schedule(context)
        WeatherWorker.runNow(context)
    }
}

class ClockWidgetReceiver : BaseReceiver() { override val glanceAppWidget: GlanceAppWidget = ClockWidget() }
class AnalogWidgetReceiver : BaseReceiver() { override val glanceAppWidget: GlanceAppWidget = AnalogWidget() }
class WeatherWidgetReceiver : BaseReceiver() { override val glanceAppWidget: GlanceAppWidget = WeatherWidget() }
class ForecastWidgetReceiver : BaseReceiver() { override val glanceAppWidget: GlanceAppWidget = ForecastWidget() }
class CombinedWidgetReceiver : BaseReceiver() { override val glanceAppWidget: GlanceAppWidget = CombinedWidget() }
class CustomWidgetReceiver : BaseReceiver() { override val glanceAppWidget: GlanceAppWidget = CustomWidget() }

/** Refresca los widgets cuando cambia algo del teléfono (cargador, alarma, zona horaria). */
class SystemEventsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                WidgetUpdater.updateAll(context)
            } finally {
                pending.finish()
            }
        }
    }
}

object WidgetUpdater {
    suspend fun updateAll(context: Context) {
        runCatching { ClockWidget().updateAll(context) }
        runCatching { AnalogWidget().updateAll(context) }
        runCatching { WeatherWidget().updateAll(context) }
        runCatching { ForecastWidget().updateAll(context) }
        runCatching { CombinedWidget().updateAll(context) }
        runCatching { CustomWidget().updateAll(context) }
    }
}

/** Widgets listos que se muestran en la pestaña Inicio. */
data class BuiltinWidget(
    val title: String,
    val description: String,
    val design: WidgetDesign,
    val receiver: Class<out GlanceAppWidgetReceiver>,
    val tall: Boolean = false,
)

val builtinWidgets = listOf(
    BuiltinWidget("Reloj + Clima", "Partido a la mitad, con carrusel animado", Templates.builtinCombined, CombinedWidgetReceiver::class.java),
    BuiltinWidget("Analógico", "Manecillas en vivo con segundero", Templates.builtinAnalog, AnalogWidgetReceiver::class.java),
    BuiltinWidget("Pronóstico", "Las próximas 6 horas", Templates.builtinForecast, ForecastWidgetReceiver::class.java),
    BuiltinWidget("Reloj", "Hora y fecha grandes", Templates.builtinClock, ClockWidgetReceiver::class.java),
    BuiltinWidget("Clima", "Temperatura, condición y máx/mín", Templates.builtinWeather, WeatherWidgetReceiver::class.java, tall = true),
)

object WidgetPin {
    /** Pide al launcher anclar el widget. Devuelve false si el launcher no lo soporta. */
    fun request(context: Context, receiver: Class<*>): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        if (!manager.isRequestPinAppWidgetSupported) return false
        return manager.requestPinAppWidget(ComponentName(context, receiver), null, null)
    }
}
