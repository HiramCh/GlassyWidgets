package com.glassywidgets.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glassywidgets.data.BgStyle
import com.glassywidgets.ui.MainViewModel
import com.glassywidgets.ui.components.ColorPickerRow
import com.glassywidgets.ui.components.GlassCard
import com.glassywidgets.ui.components.LabeledSlider
import com.glassywidgets.ui.components.ScreenHeader
import com.glassywidgets.ui.components.SectionTitle
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: MainViewModel) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.useMyLocation()
        else Toast.makeText(context, "Sin permiso de ubicación", Toast.LENGTH_SHORT).show()
    }

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader("Ajustes")

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle("Ubicación del clima")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                Text(s.cityName, Modifier.padding(start = 8.dp), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar ciudad") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { vm.searchCity(query) }),
                trailingIcon = {
                    if (vm.searching) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else IconButton(onClick = { vm.searchCity(query) }) { Icon(Icons.Filled.Search, "Buscar") }
                },
            )
            vm.searchResults.forEach { g ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { vm.selectCity(g); query = "" }
                        .padding(vertical = 10.dp),
                ) {
                    Text(g.name, style = MaterialTheme.typography.bodyLarge)
                    Text(g.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
                    if (granted) vm.useMyLocation() else permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Usar mi ubicación") }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle("Unidades")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Usar Fahrenheit (°F)", Modifier.weight(1f))
                Switch(checked = s.fahrenheit, onCheckedChange = { f -> vm.updateSettings(refetch = true) { it.copy(fahrenheit = f) } })
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle("Apariencia de la app")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = s.appGlass, onClick = { vm.updateSettings { it.copy(appGlass = true) } }, label = { Text("Liquid Glass") })
                FilterChip(selected = !s.appGlass, onClick = { vm.updateSettings { it.copy(appGlass = false) } }, label = { Text("Material You") })
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle("Widgets listos")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BgStyle.entries.forEach { style ->
                    FilterChip(
                        selected = s.widgetStyle == style,
                        onClick = { vm.updateSettings { it.copy(widgetStyle = style) } },
                        label = { Text(style.label) },
                    )
                }
            }
            if (s.widgetStyle != BgStyle.MATERIAL_YOU) {
                ColorPickerRow("Tinte 1", s.tint1, { c -> vm.updateSettings { it.copy(tint1 = c) } })
                if (s.widgetStyle != BgStyle.SOLID) {
                    ColorPickerRow("Tinte 2", s.tint2, { c -> vm.updateSettings { it.copy(tint2 = c) } })
                }
                LabeledSlider("Opacidad", s.widgetOpacity, 0f..1f, "${(s.widgetOpacity * 100).roundToInt()}%") { v ->
                    vm.updateSettings { it.copy(widgetOpacity = v) }
                }
            }
            LabeledSlider("Esquinas", s.widgetCorner.toFloat(), 0f..48f, "${s.widgetCorner} dp") { v ->
                vm.updateSettings { it.copy(widgetCorner = v.roundToInt()) }
            }
        }

        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle("Créditos e inspiración")
            Text(
                "Clima: Open-Meteo (gratis, sin API key).\n" +
                    "Widgets: Jetpack Glance y los ejemplos de android/platform-samples.\n" +
                    "Ideas del editor: sadellie/sukko (alternativa libre a KWGT).\n" +
                    "Efecto cristal: inspirado en Kyant0/AndroidLiquidGlass y styropyr0/PrismalAGSL.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
