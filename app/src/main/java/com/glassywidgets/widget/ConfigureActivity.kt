package com.glassywidgets.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.glassywidgets.data.AppSettings
import com.glassywidgets.data.Repository
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.ui.components.DesignPreview
import com.glassywidgets.ui.components.GlassCard
import com.glassywidgets.ui.components.LiquidBackground
import com.glassywidgets.ui.components.ScreenHeader
import com.glassywidgets.ui.theme.GlassyTheme
import com.glassywidgets.ui.theme.LocalGlassMode
import com.glassywidgets.ui.theme.LocalNow
import com.glassywidgets.ui.theme.rememberNow
import kotlinx.coroutines.launch

/** Se abre al añadir/reconfigurar el widget "Mi diseño": elige qué diseño mostrar. */
class ConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        enableEdgeToEdge()
        val repo = Repository.get(this)
        setContent {
            val settings by repo.settings.collectAsState(AppSettings())
            val designs by repo.designs.collectAsState(emptyList())
            val weather by repo.weather.collectAsState(null)
            GlassyTheme {
                CompositionLocalProvider(LocalGlassMode provides settings.appGlass, LocalNow provides rememberNow()) {
                    LiquidBackground {
                        LazyColumn(
                            Modifier.statusBarsPadding(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item { ScreenHeader("Elige un diseño", "Puedes crear más en la app, pestaña Crear") }
                            items(designs, key = { it.id }) { d ->
                                GlassCard(Modifier.fillMaxWidth(), onClick = { choose(d) }) {
                                    Text(d.name, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(8.dp))
                                    DesignPreview(d, weather, Modifier.fillMaxWidth().height(140.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun choose(design: WidgetDesign) {
        lifecycleScope.launch {
            val context = this@ConfigureActivity
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
            updateAppWidgetState(context, glanceId) { it[CustomWidget.DESIGN_KEY] = design.id }
            CustomWidget().update(context, glanceId)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
            finish()
        }
    }
}
