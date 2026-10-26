package com.glassywidgets.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.glassywidgets.data.BgStyle
import com.glassywidgets.data.ColorRole
import com.glassywidgets.data.DateFormatOpt
import com.glassywidgets.data.ElementType
import com.glassywidgets.data.FontOpt
import com.glassywidgets.data.HAlign
import com.glassywidgets.data.Templates
import com.glassywidgets.data.TimeFormat
import com.glassywidgets.data.WeatherData
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.WidgetElement
import com.glassywidgets.data.newId
import com.glassywidgets.ui.components.ColorPickerRow
import com.glassywidgets.ui.components.DesignPreview
import com.glassywidgets.ui.components.GlassCard
import com.glassywidgets.ui.components.LabeledSlider
import com.glassywidgets.ui.components.LiquidBackground
import com.glassywidgets.ui.components.SectionTitle
import kotlin.math.roundToInt

private enum class PreviewSize(val label: String, val w: Int, val h: Int) {
    SMALL("2×2", 150, 150), MEDIUM("4×2", 320, 150), LARGE("4×3", 320, 230),
}

/** Editor dinámico: vista previa en vivo + elementos + fondo + colores. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    initial: WidgetDesign,
    weather: WeatherData?,
    onSave: (WidgetDesign) -> Unit,
    onPin: (WidgetDesign) -> Unit,
    onClose: () -> Unit,
) {
    var d by remember { mutableStateOf(initial) }
    var tab by remember { mutableIntStateOf(0) }
    var previewSize by remember { mutableStateOf(PreviewSize.MEDIUM) }
    var expandedId by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    BackHandler(onBack = onClose)

    fun setElements(list: List<WidgetElement>) { d = d.copy(elements = list) }

    LiquidBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
            // Barra superior
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                TextField(
                    value = d.name,
                    onValueChange = { d = d.copy(name = it) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.titleMedium,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
                IconButton(onClick = { onSave(d) }) { Icon(Icons.Filled.Check, "Guardar") }
            }

            // Vista previa en vivo
            val pw by animateDpAsState(previewSize.w.dp, label = "pw")
            val ph by animateDpAsState(previewSize.h.dp, label = "ph")
            Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
                DesignPreview(d, weather, Modifier.size(pw, ph))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                PreviewSize.entries.forEach { s ->
                    FilterChip(
                        selected = previewSize == s,
                        onClick = { previewSize = s },
                        label = { Text(s.label) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }

            // Pestañas
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Elementos", "Fondo", "Colores").forEachIndexed { i, label ->
                    FilterChip(selected = tab == i, onClick = { tab = i }, label = { Text(label) }, modifier = Modifier.weight(1f))
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                when (tab) {
                    0 -> {
                        d.elements.forEachIndexed { i, e ->
                            ElementCard(
                                e = e,
                                index = i,
                                count = d.elements.size,
                                expanded = expandedId == e.id,
                                onToggle = { expandedId = if (expandedId == e.id) null else e.id },
                                onChange = { ne -> setElements(d.elements.toMutableList().also { it[i] = ne }) },
                                onMove = { dir ->
                                    val j = i + dir
                                    if (j in d.elements.indices) {
                                        val l = d.elements.toMutableList()
                                        l[i] = l[j].also { l[j] = l[i] }
                                        setElements(l)
                                    }
                                },
                                onDuplicate = {
                                    setElements(d.elements.toMutableList().also { it.add(i + 1, e.copy(id = newId())) })
                                },
                                onDelete = { setElements(d.elements.filterIndexed { k, _ -> k != i }) },
                            )
                        }
                        FilledTonalButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Añadir elemento")
                        }
                    }
                    1 -> BackgroundTab(d) { d = it }
                    else -> ColorsTab(d) { d = it }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { onSave(d) }, modifier = Modifier.weight(1f)) { Text("Guardar") }
                    Button(onClick = { onPin(d) }, modifier = Modifier.weight(1f)) { Text("Guardar y añadir") }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showAdd) {
        ModalBottomSheet(onDismissRequest = { showAdd = false }) {
            Column(Modifier.padding(16.dp).navigationBarsPadding()) {
                Text("Añadir elemento", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ElementType.entries.forEach { type ->
                        AssistChip(
                            onClick = {
                                val ne = Templates.newElement(type)
                                setElements(d.elements + ne)
                                expandedId = ne.id
                                showAdd = false
                                tab = 0
                            },
                            label = { Text("${type.emoji}  ${type.label}") },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ElementCard(
    e: WidgetElement,
    index: Int,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onChange: (WidgetElement) -> Unit,
    onMove: (Int) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    GlassCard(Modifier.fillMaxWidth().animateContentSize(), contentPadding = PaddingValues(12.dp)) {
        Row(Modifier.clickable(onClick = onToggle), verticalAlignment = Alignment.CenterVertically) {
            Text(e.type.emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(e.type.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                val details = buildList {
                    if (e.type != ElementType.DIVIDER) add("${e.size}${if (e.type == ElementType.SPACER) " dp" else " sp"}")
                    if (e.sameRow) add("misma fila")
                    if (e.type == ElementType.TIME) add(e.timeFormat.label)
                    if (e.type == ElementType.DATE) add(e.dateFormat.label)
                }
                Text(details.joinToString(" · "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onMove(-1) }, enabled = index > 0) { Icon(Icons.Filled.KeyboardArrowUp, "Subir") }
            IconButton(onClick = { onMove(1) }, enabled = index < count - 1) { Icon(Icons.Filled.KeyboardArrowDown, "Bajar") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Eliminar") }
        }

        AnimatedVisibility(expanded) {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HorizontalDivider()
                val noText = e.type == ElementType.DIVIDER || e.type == ElementType.SPACER
                if (e.type != ElementType.DIVIDER) {
                    val range = if (e.type == ElementType.SPACER) 2f..48f else 8f..110f
                    LabeledSlider("Tamaño", e.size.toFloat(), range, "${e.size}") {
                        onChange(e.copy(size = it.roundToInt()))
                    }
                }
                if (e.type == ElementType.CUSTOM_TEXT) {
                    OutlinedTextField(
                        value = e.text,
                        onValueChange = { onChange(e.copy(text = it)) },
                        label = { Text("Texto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (e.type == ElementType.TIME) {
                    ChipGroup("Formato", TimeFormat.entries, e.timeFormat, { it.label }) { onChange(e.copy(timeFormat = it)) }
                }
                if (e.type == ElementType.DATE) {
                    ChipGroup("Formato", DateFormatOpt.entries, e.dateFormat, { it.label }) { onChange(e.copy(dateFormat = it)) }
                }
                if (!noText && e.type != ElementType.WEATHER_ICON) {
                    ChipGroup("Tipografía", FontOpt.entries, e.font, { it.label }) { onChange(e.copy(font = it)) }
                }
                if (e.type != ElementType.SPACER && e.type != ElementType.WEATHER_ICON) {
                    ChipGroup("Color", ColorRole.entries, e.colorRole, { it.label }) { onChange(e.copy(colorRole = it)) }
                    if (e.colorRole == ColorRole.CUSTOM) {
                        ColorPickerRow("Color propio", e.customColor, { onChange(e.copy(customColor = it)) })
                    }
                }
                if (!noText && index > 0) {
                    SwitchRow("En la misma fila que el anterior", e.sameRow) { onChange(e.copy(sameRow = it)) }
                }
                if (!noText && e.type != ElementType.TIME && e.type != ElementType.DATE && e.type != ElementType.WEATHER_ICON) {
                    SwitchRow("MAYÚSCULAS", e.uppercase) { onChange(e.copy(uppercase = it)) }
                }
                TextButtonRow("Duplicar elemento", onDuplicate)
            }
        }
    }
}

@Composable
private fun BackgroundTab(d: WidgetDesign, onChange: (WidgetDesign) -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        SectionTitle("Estilo")
        ChipGroup(null, BgStyle.entries, d.style, { it.label }) { onChange(d.copy(style = it)) }
        if (d.style == BgStyle.MATERIAL_YOU) {
            Text(
                "Material You toma los colores de tu fondo de pantalla automáticamente (Android 12+).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (d.style != BgStyle.MATERIAL_YOU) {
        GlassCard(Modifier.fillMaxWidth()) {
            SectionTitle("Tinte")
            ColorPickerRow(if (d.style == BgStyle.SOLID) "Color" else "Color 1", d.color1, { onChange(d.copy(color1 = it)) })
            if (d.style != BgStyle.SOLID) {
                ColorPickerRow("Color 2", d.color2, { onChange(d.copy(color2 = it)) })
            }
            LabeledSlider("Opacidad", d.opacity, 0f..1f, "${(d.opacity * 100).roundToInt()}%") { onChange(d.copy(opacity = it)) }
            if (d.style == BgStyle.GLASS || d.style == BgStyle.DARK_GLASS) {
                SwitchRow("Borde de luz (liquid glass)", d.rimLight) { onChange(d.copy(rimLight = it)) }
            }
        }
    }
    GlassCard(Modifier.fillMaxWidth()) {
        SectionTitle("Forma y espacio")
        LabeledSlider("Esquinas", d.cornerRadius.toFloat(), 0f..48f, "${d.cornerRadius} dp") { onChange(d.copy(cornerRadius = it.roundToInt())) }
        LabeledSlider("Margen interior", d.padding.toFloat(), 4f..32f, "${d.padding} dp") { onChange(d.copy(padding = it.roundToInt())) }
        ChipGroup("Alineación", HAlign.entries, d.align, { it.label }) { onChange(d.copy(align = it)) }
    }
}

@Composable
private fun ColorsTab(d: WidgetDesign, onChange: (WidgetDesign) -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        SectionTitle("Colores del texto")
        if (d.style == BgStyle.MATERIAL_YOU) {
            Text(
                "Con Material You el texto usa los colores del sistema. Elige \"Propio\" en un elemento para forzar un color.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            ColorPickerRow("Texto principal", d.textColor, { onChange(d.copy(textColor = it)) })
            ColorPickerRow("Acento", d.accentColor, { onChange(d.copy(accentColor = it)) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipGroup(title: String?, options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Column {
        if (title != null) Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { o ->
                FilterChip(selected = o == selected, onClick = { onSelect(o) }, label = { Text(label(o)) })
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun TextButtonRow(label: String, onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) { Text(label) }
}
