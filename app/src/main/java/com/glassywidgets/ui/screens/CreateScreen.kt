package com.glassywidgets.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.glassywidgets.data.LayoutMode
import com.glassywidgets.data.Templates
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.newId
import com.glassywidgets.ui.MainViewModel
import com.glassywidgets.ui.components.DesignPreview
import com.glassywidgets.ui.components.GlassCard
import com.glassywidgets.ui.components.ScreenHeader
import com.glassywidgets.ui.components.WallpaperStage
import com.glassywidgets.ui.components.wallpaperAt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(vm: MainViewModel, onEdit: (WidgetDesign) -> Unit) {
    val designs by vm.designs.collectAsStateWithLifecycle()
    val weather by vm.weather.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    var showNew by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<WidgetDesign?>(null) }
    val wall = wallpaperAt(settings.wallpaper)

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader("Crear", "Diseña tus widgets: columnas, alineación, animaciones y más") }
        item {
            GlassCard(Modifier.fillMaxWidth(), onClick = { showNew = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Nuevo diseño", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Empieza en blanco o desde una de ${Templates.starterDesigns.size} plantillas", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        items(designs, key = { it.id }) { d ->
            GlassCard(Modifier.fillMaxWidth().animateItem(), onClick = { onEdit(d) }, contentPadding = PaddingValues(12.dp)) {
                WallpaperStage(wall, Modifier.fillMaxWidth().height(190.dp), animated = false) {
                    DesignPreview(d, weather, Modifier.padding(14.dp).fillMaxWidth().height(150.dp))
                }
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(d.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${d.style.label} · ${if (d.layout == LayoutMode.SPLIT) "2 columnas" else "1 columna"} · ${d.elements.size} elementos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { onEdit(d) }) { Icon(Icons.Filled.Edit, "Editar") }
                    IconButton(onClick = { toDelete = d }) { Icon(Icons.Filled.Delete, "Eliminar") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { onEdit(d.copy(id = newId(), name = d.name + " (copia)")) }, modifier = Modifier.weight(1f)) {
                        Text("Duplicar")
                    }
                    Button(onClick = { vm.pinCustom(d) }, modifier = Modifier.weight(1f)) { Text("Añadir a inicio") }
                }
            }
        }
    }

    if (showNew) {
        ModalBottomSheet(onDismissRequest = { showNew = false }) {
            LazyColumn(
                Modifier.navigationBarsPadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Text("Empieza desde…", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                items(listOf(Templates.blank) + Templates.starterDesigns, key = { "new_" + it.id }) { t ->
                    GlassCard(
                        Modifier.fillMaxWidth(),
                        onClick = {
                            showNew = false
                            onEdit(t.copy(id = newId(), name = if (t.id == Templates.blank.id) "Nuevo diseño" else t.name))
                        },
                        contentPadding = PaddingValues(10.dp),
                    ) {
                        Text(t.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                        WallpaperStage(wall, Modifier.fillMaxWidth().height(160.dp), animated = false) {
                            DesignPreview(t, weather, Modifier.padding(12.dp).fillMaxWidth().height(130.dp))
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { d ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("¿Eliminar \"${d.name}\"?") },
            text = { Text("Los widgets que usen este diseño mostrarán otro diseño disponible.") },
            confirmButton = { TextButton(onClick = { vm.deleteDesign(d.id); toDelete = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } },
        )
    }
}
