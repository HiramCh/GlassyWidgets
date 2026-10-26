package com.glassywidgets.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glassywidgets.data.BgStyle
import com.glassywidgets.data.WeatherCodes
import com.glassywidgets.data.WidgetElement
import com.glassywidgets.data.ElementType
import com.glassywidgets.data.withGlobal
import com.glassywidgets.ui.MainViewModel
import com.glassywidgets.ui.components.DesignPreview
import com.glassywidgets.ui.components.GlassCard
import com.glassywidgets.ui.components.SectionTitle
import com.glassywidgets.ui.components.WallpaperStage
import com.glassywidgets.ui.components.wallpaperAt
import com.glassywidgets.ui.theme.LocalNow
import com.glassywidgets.widget.DesignLogic
import com.glassywidgets.widget.builtinWidgets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HomeScreen(vm: MainViewModel) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val w by vm.weather.collectAsStateWithLifecycle()
    val now by LocalNow.current
    val context = LocalContext.current
    val is24 = DateFormat.is24HourFormat(context)
    val carousel = rememberLazyListState()

    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // Encabezado con reloj en vivo
        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    DesignLogic.greeting(now, ""),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    SimpleDateFormat(if (is24) "HH:mm" else "h:mm", Locale.getDefault()).format(Date(now)),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Thin,
                )
                Text(
                    SimpleDateFormat("EEEE d 'de' MMMM", Locale.getDefault()).format(Date(now))
                        .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Clima actual + próximas horas
        item {
            GlassCard(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
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
                        Text(WeatherCodes.emoji(weather.code, weather.isDay), fontSize = 52.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${weather.temp.roundToInt()}°", fontSize = 48.sp, fontWeight = FontWeight.Light)
                            Text(WeatherCodes.description(weather.code), style = MaterialTheme.typography.bodyLarge)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("↑${weather.max.roundToInt()}°  ↓${weather.min.roundToInt()}°", style = MaterialTheme.typography.bodyMedium)
                            Text("💧 ${weather.humidity}%", style = MaterialTheme.typography.bodySmall)
                            Text("🌬 ${weather.wind.roundToInt()} km/h", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val cells = DesignLogic.hourlyCells(WidgetElement(type = ElementType.HOURLY, count = 6), weather, now, is24)
                    Row(Modifier.fillMaxWidth()) {
                        cells.forEach { c ->
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(c.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(c.icon, fontSize = 20.sp)
                                Text(c.main, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                SectionTitle("Estilo de los widgets listos")
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
        }

        item { SectionTitle("Widgets listos", Modifier.padding(horizontal = 16.dp)) }

        // Carrusel de widgets sobre un fondo de pantalla simulado
        item {
            LazyRow(
                state = carousel,
                flingBehavior = rememberSnapFlingBehavior(carousel),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(builtinWidgets, key = { it.title }) { b ->
                    GlassCard(Modifier.width(320.dp), contentPadding = PaddingValues(12.dp)) {
                        WallpaperStage(wallpaperAt(s.wallpaper), Modifier.fillMaxWidth().height(210.dp)) {
                            DesignPreview(
                                b.design.withGlobal(s), w,
                                Modifier.padding(14.dp).fillMaxWidth().height(if (b.tall) 180.dp else 150.dp),
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(b.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(b.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { vm.pinBuiltin(b.receiver) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Añadir a inicio")
                        }
                    }
                }
            }
        }
    }
}
