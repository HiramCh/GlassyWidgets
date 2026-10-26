package com.glassywidgets.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glassywidgets.data.BgStyle
import com.glassywidgets.data.WeatherCodes
import com.glassywidgets.data.withGlobal
import com.glassywidgets.ui.MainViewModel
import com.glassywidgets.ui.components.DesignPreview
import com.glassywidgets.ui.components.GlassCard
import com.glassywidgets.ui.components.ScreenHeader
import com.glassywidgets.ui.components.SectionTitle
import com.glassywidgets.widget.builtinWidgets
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

@Composable
fun HomeScreen(vm: MainViewModel) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val w by vm.weather.collectAsStateWithLifecycle()

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader("Glassy Widgets", "Reloj, clima y tus propios diseños") }

        // Tarjeta de clima actual
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(s.cityName, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                    if (vm.refreshing) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { vm.refreshWeather() }) { Icon(Icons.Filled.Refresh, "Actualizar") }
                    }
                }
                val weather = w
                if (weather == null) {
                    Text("Aún no hay datos del clima. Toca actualizar.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(WeatherCodes.emoji(weather.code, weather.isDay), fontSize = 44.sp)
                        Spacer(Modifier.size(12.dp))
                        Column {
                            Text("${weather.temp.roundToInt()}°", fontSize = 44.sp, fontWeight = FontWeight.Light)
                            Text(WeatherCodes.description(weather.code), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Text(
                        "↑${weather.max.roundToInt()}°  ↓${weather.min.roundToInt()}°  ·  💧 ${weather.humidity}%  ·  🌬 ${weather.wind.roundToInt()} km/h",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Actualizado: " + DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(weather.updatedAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Estilo rápido para los widgets listos
        item {
            SectionTitle("Estilo de los widgets")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BgStyle.entries.toList()) { style ->
                    FilterChip(
                        selected = s.widgetStyle == style,
                        onClick = { vm.updateSettings { it.copy(widgetStyle = style) } },
                        label = { Text(style.label) },
                    )
                }
            }
        }

        item { SectionTitle("Widgets listos") }

        items(builtinWidgets) { b ->
            GlassCard(Modifier.fillMaxWidth()) {
                Text(b.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(b.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                DesignPreview(b.design.withGlobal(s), w, Modifier.fillMaxWidth().height(if (b.title == "Clima") 170.dp else 150.dp))
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.pinBuiltin(b.receiver) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, null)
                    Spacer(Modifier.size(8.dp))
                    Text("Añadir a la pantalla de inicio")
                }
            }
        }
    }
}
