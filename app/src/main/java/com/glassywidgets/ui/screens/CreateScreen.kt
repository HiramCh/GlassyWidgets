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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
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
import com.glassywidgets.data.Templates
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.newId
import com.glassywidgets.ui.MainViewModel
import com.glassywidgets.ui.components.DesignPreview
import com.glassywidgets.ui.components.GlassCard
import com.glassywidgets.ui.components.ScreenHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(vm: MainViewModel, onEdit: (WidgetDesign) -> Unit) {
    val designs by vm.designs.collectAsStateWithLifecycle()
    val weather by vm.weather.collectAsStateWithLifecycle()
    var showNew by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<WidgetDesign?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader("Crear", "Diseña tus widgets elemento por elemento") }
        item {
            GlassCard(Modifier.fillMaxWidth(), onClick = { showNew = true }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("Nuevo diseño", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Empieza en blanco o desde una plantilla", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        items(designs, key = { it.id }) { d ->
            GlassCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(d.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { onEdit(d) }) { Icon(Icons.Filled.Edit, "Editar") }
                    IconButton(onClick = { toDelete = d }) { Icon(Icons.Filled.Delete, "Eliminar") }
                }
                Spacer(Modifier.height(8.dp))
                DesignPreview(d, weather, Modifier.fillMaxWidth().height(150.dp))
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { onEdit(d.copy(id = newId(), name = d.name + " (copia)")) }, modifier = Modifier.weight(1f)) {
                        Text("Duplicar")
                    }
                    Button(onClick = { vm.pinCustom(d) }, modifier = Modifier.weight(1f)) {
                        Text("Añadir a inicio")
                    }
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
                item { Text("Empieza desde…", style = MaterialTheme.typography.titleLarge) }
                items(listOf(Templates.blank) + Templates.starterDesigns, key = { "new_" + it.id }) { t ->
                    GlassCard(
                        Modifier.fillMaxWidth(),
                        onClick = {
                            showNew = false
                            onEdit(t.copy(id = newId(), name = if (t.id == Templates.blank.id) "Nuevo diseño" else t.name))
                        },
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.size(6.dp))
                            Text(t.name, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        DesignPreview(t, weather, Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 4.dp))
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
