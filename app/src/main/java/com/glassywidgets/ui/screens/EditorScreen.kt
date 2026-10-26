package com.glassywidgets.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glassywidgets.data.BgStyle
import com.glassywidgets.data.Category
import com.glassywidgets.data.ColorRole
import com.glassywidgets.data.DateFormatOpt
import com.glassywidgets.data.DialStyle
import com.glassywidgets.data.ElemAlign
import com.glassywidgets.data.ElementType
import com.glassywidgets.data.FlipAnim
import com.glassywidgets.data.FontOpt
import com.glassywidgets.data.HAlign
import com.glassywidgets.data.LayoutMode
import com.glassywidgets.data.ROTATOR_OPTIONS
import com.glassywidgets.data.Templates
import com.glassywidgets.data.TimeFormat
import com.glassywidgets.data.VAlign
import com.glassywidgets.data.WORLD_ZONES
import com.glassywidgets.data.WeatherData
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.WidgetElement
import com.glassywidgets.data.newId
import com.glassywidgets.ui.components.AlignGlyph
import com.glassywidgets.ui.components.ChipGroup
import com.glassywidgets.ui.components.ColorPickerRow
import com.glassywidgets.ui.components.DesignPreview
import com.glassywidgets.ui.components.FieldLabel
import com.glassywidgets.ui.components.GlassCard
import com.glassywidgets.ui.components.LabeledSlider
import com.glassywidgets.ui.components.LayoutPicker
import com.glassywidgets.ui.components.LiquidBackground
import com.glassywidgets.ui.components.Segmented
import com.glassywidgets.ui.components.SwitchRow
import com.glassywidgets.ui.components.WallpaperStage
import com.glassywidgets.ui.components.wallpaperAt
import kotlin.math.roundToInt

private enum class PreviewSize(val label: String, val w: Int, val h: Int) {
    SMALL("2×2", 150, 150), MEDIUM("4×2", 316, 150), LARGE("4×3", 316, 220),
}

private val PANELS = listOf("Elemento", "Distribución", "Fondo", "Colores")

/**
 * Editor visual:
 *  - Vista previa sobre un fondo de pantalla simulado; toca un elemento para editarlo.
 *  - Tira de elementos para seleccionar/añadir.
 *  - Paneles: Elemento (inspector), Distribución (columnas y alineación), Fondo y Colores.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    initial: WidgetDesign,
    weather: WeatherData?,
    wallpaper: Int,
    onWallpaper: (Int) -> Unit,
    onSave: (WidgetDesign) -> Unit,
    onPin: (WidgetDesign) -> Unit,
    onClose: () -> Unit,
) {
    var d by remember { mutableStateOf(initial) }
    var selectedId by remember { mutableStateOf(initial.elements.firstOrNull()?.id) }
    var panel by remember { mutableIntStateOf(0) }
    var previewSize by remember { mutableStateOf(PreviewSize.MEDIUM) }
    var showAdd by remember { mutableStateOf(false) }
    val stripState = rememberLazyListState()
    BackHandler(onBack = onClose)

    val selIndex = d.elements.indexOfFirst { it.id == selectedId }
    val selected = d.elements.getOrNull(selIndex)

    fun setElements(list: List<WidgetElement>) { d = d.copy(elements = list) }
    fun updateEl(e: WidgetElement) { setElements(d.elements.map { if (it.id == e.id) e else it }) }
    fun select(id: String) { selectedId = id; panel = 0 }

    LaunchedEffect(selIndex) { if (selIndex >= 0) stripState.animateScrollToItem(selIndex + 1) }

    LiquidBackground {
        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
            // ---------- Barra superior ----------
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                TextField(
                    value = d.name,
                    onValueChange = { d = d.copy(name = it) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
                IconButton(onClick = { onSave(d) }) { Icon(Icons.Filled.Check, "Guardar") }
            }

            // ---------- Escenario con vista previa ----------
            val pw by animateDpAsState(previewSize.w.dp, spring(dampingRatio = Spring.DampingRatioLowBouncy), label = "pw")
            val ph by animateDpAsState(previewSize.h.dp, spring(dampingRatio = Spring.DampingRatioLowBouncy), label = "ph")
            WallpaperStage(
                wallpaperAt(wallpaper),
                Modifier.padding(horizontal = 12.dp).fillMaxWidth().height(ph + 74.dp),
            ) {
                DesignPreview(
                    d, weather,
                    Modifier.align(Alignment.Center).padding(bottom = 22.dp).size(pw, ph)
                        .shadow(18.dp, RoundedCornerShape(d.cornerRadius.dp), clip = false, ambientColor = Color.Black.copy(alpha = 0.4f)),
                    selectedId = selectedId,
                    onSelect = { select(it) },
                )
                Row(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PreviewSize.entries.forEach { s ->
                        StagePill(s.label, previewSize == s) { previewSize = s }
                    }
                    StagePill("🎨 Fondo", false) { onWallpaper(wallpaper + 1) }
                }
            }

            // ---------- Tira de elementos ----------
            LazyRow(
                state = stripState,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "add") {
                    ElementPill("＋", "Añadir", selected = false, badge = null, accent = true) { showAdd = true }
                }
                itemsIndexed(d.elements, key = { _, e -> e.id }) { _, e ->
                    ElementPill(
                        e.type.emoji, e.type.label, selected = e.id == selectedId,
                        badge = if (d.layout == LayoutMode.SPLIT) (if (e.column == 0) "I" else "D") else null,
                        modifier = Modifier.animateItem(),
                    ) { select(e.id) }
                }
            }

            // ---------- Paneles ----------
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PANELS.forEachIndexed { i, label ->
                    FilterChip(selected = panel == i, onClick = { panel = i }, label = { Text(label, maxLines = 1) })
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                AnimatedContent(targetState = panel, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "panel") { p ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        when (p) {
                            0 -> if (selected == null) {
                                GlassCard(Modifier.fillMaxWidth()) {
                                    Text("Toca un elemento en la vista previa o en la tira para editarlo.", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(8.dp))
                                    FilledTonalButton(onClick = { showAdd = true }) { Text("Añadir elemento") }
                                }
                            } else {
                                Inspector(
                                    e = selected,
                                    index = selIndex,
                                    count = d.elements.size,
                                    split = d.layout == LayoutMode.SPLIT,
                                    onChange = ::updateEl,
                                    onMove = { dir ->
                                        val j = selIndex + dir
                                        if (j in d.elements.indices) {
                                            val l = d.elements.toMutableList()
                                            l[selIndex] = l[j].also { l[j] = l[selIndex] }
                                            setElements(l)
                                        }
                                    },
                                    onDuplicate = {
                                        val copy = selected.copy(id = newId())
                                        setElements(d.elements.toMutableList().also { it.add(selIndex + 1, copy) })
                                        selectedId = copy.id
                                    },
                                    onDelete = {
                                        val next = d.elements.getOrNull(selIndex + 1) ?: d.elements.getOrNull(selIndex - 1)
                                        setElements(d.elements.filterNot { it.id == selected.id })
                                        selectedId = next?.id
                                    },
                                )
                            }
                            1 -> LayoutPanel(d) { d = it }
                            2 -> BackgroundPanel(d) { d = it }
                            else -> ColorsPanel(d) { d = it }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ---------- Acciones ----------
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(onClick = { onSave(d) }, modifier = Modifier.weight(1f)) { Text("Guardar") }
                Button(onClick = { onPin(d) }, modifier = Modifier.weight(1f)) { Text("Guardar y añadir") }
            }
        }
    }

    if (showAdd) {
        ModalBottomSheet(onDismissRequest = { showAdd = false }) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).navigationBarsPadding(),
            ) {
                Text("Añadir elemento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                if (d.layout == LayoutMode.SPLIT) {
                    Text(
                        "Se añadirá en la columna ${if ((selected?.column ?: 0) == 0) "izquierda" else "derecha"}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Category.entries.forEach { cat ->
                    FieldLabel(cat.label)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElementType.entries.filter { it.category == cat }.forEach { type ->
                            AddTile(type) {
                                val ne = Templates.newElement(type, selected?.column ?: 0)
                                val at = if (selIndex >= 0) selIndex + 1 else d.elements.size
                                setElements(d.elements.toMutableList().also { it.add(at, ne) })
                                select(ne.id)
                                showAdd = false
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Piezas del editor
// ---------------------------------------------------------------------------

@Composable
private fun StagePill(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (selected) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.Black else Color.White,
        )
    }
}

@Composable
private fun ElementPill(
    emoji: String,
    label: String,
    selected: Boolean,
    badge: String?,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val bg = when {
        accent -> cs.primary
        selected -> cs.primaryContainer
        else -> cs.surface.copy(alpha = 0.55f)
    }
    val fg = when {
        accent -> cs.onPrimary
        selected -> cs.onPrimaryContainer
        else -> cs.onSurface
    }
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(if (selected) 2.dp else 1.dp, if (selected) cs.primary else Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1)
        if (badge != null) {
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier.size(18.dp).clip(CircleShape).background(cs.tertiary),
                contentAlignment = Alignment.Center,
            ) { Text(badge, style = MaterialTheme.typography.labelSmall, color = cs.onTertiary) }
        }
    }
}

@Composable
private fun AddTile(type: ElementType, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier
            .width(104.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cs.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(type.emoji, fontSize = 26.sp)
        Spacer(Modifier.height(4.dp))
        Text(type.label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Inspector(
    e: WidgetElement,
    index: Int,
    count: Int,
    split: Boolean,
    onChange: (WidgetElement) -> Unit,
    onMove: (Int) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme

    // Encabezado + acciones rápidas
    GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(e.type.emoji, fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Text(e.type.label, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { onMove(-1) }, enabled = index > 0) { Icon(Icons.Filled.KeyboardArrowUp, "Subir") }
            IconButton(onClick = { onMove(1) }, enabled = index < count - 1) { Icon(Icons.Filled.KeyboardArrowDown, "Bajar") }
            IconButton(onClick = onDuplicate) { Icon(Icons.Filled.Add, "Duplicar") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Eliminar", tint = cs.error) }
        }
    }

    // Posición
    GlassCard(Modifier.fillMaxWidth()) {
        if (split) {
            FieldLabel("Columna")
            Segmented(listOf(0, 1), e.column, { onChange(e.copy(column = it, sameRow = false)) }) {
                Text(if (it == 0) "◧  Izquierda" else "Derecha  ◨")
            }
            Spacer(Modifier.height(8.dp))
        }
        FieldLabel("Alineación")
        Segmented(ElemAlign.entries, e.align, { onChange(e.copy(align = it)) }) { a ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlignGlyph(a, cs.onSurface)
                if (a == ElemAlign.AUTO) {
                    Spacer(Modifier.width(4.dp))
                    Text("Auto", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        if (!e.type.isStretch && e.type != ElementType.SPACER && index > 0) {
            Spacer(Modifier.height(4.dp))
            SwitchRow("Junto al anterior (misma fila)", e.sameRow) { onChange(e.copy(sameRow = it)) }
        }
    }

    // Contenido específico de cada tipo
    val hasContent = e.type in setOf(
        ElementType.TIME, ElementType.DATE, ElementType.WORLD_CLOCK, ElementType.ANALOG_CLOCK, ElementType.GREETING,
        ElementType.CUSTOM_TEXT, ElementType.EMOJI, ElementType.HOURLY, ElementType.DAILY, ElementType.ROTATOR,
        ElementType.DAY_PROGRESS, ElementType.BATTERY_BAR,
    )
    if (hasContent) {
        GlassCard(Modifier.fillMaxWidth()) {
            FieldLabel("Contenido")
            when (e.type) {
                ElementType.TIME -> ChipGroup(null, TimeFormat.entries, e.timeFormat, { it.label }) { onChange(e.copy(timeFormat = it)) }
                ElementType.DATE -> ChipGroup(null, DateFormatOpt.entries, e.dateFormat, { it.label }) { onChange(e.copy(dateFormat = it)) }
                ElementType.WORLD_CLOCK -> {
                    ChipGroup(null, WORLD_ZONES, WORLD_ZONES.firstOrNull { it.second == e.timeZone } ?: WORLD_ZONES[0], { it.first }) {
                        onChange(e.copy(timeZone = it.second, zoneLabel = it.first))
                    }
                    OutlinedTextField(
                        value = e.zoneLabel, onValueChange = { onChange(e.copy(zoneLabel = it)) },
                        label = { Text("Etiqueta") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    ChipGroup("Formato", listOf(TimeFormat.AUTO, TimeFormat.H24, TimeFormat.H12_AMPM), e.timeFormat, { it.label }) {
                        onChange(e.copy(timeFormat = it))
                    }
                }
                ElementType.ANALOG_CLOCK -> {
                    Segmented(DialStyle.entries, e.dialStyle, { onChange(e.copy(dialStyle = it)) }) { Text(it.label) }
                    SwitchRow("Segundero animado", e.showSeconds) { onChange(e.copy(showSeconds = it)) }
                    Text(
                        "En Android 12+ las manecillas toman el color del elemento y el segundero el color de acento.",
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                    )
                }
                ElementType.GREETING -> OutlinedTextField(
                    value = e.text, onValueChange = { onChange(e.copy(text = it)) },
                    label = { Text("Tu nombre (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                ElementType.CUSTOM_TEXT -> OutlinedTextField(
                    value = e.text, onValueChange = { onChange(e.copy(text = it)) },
                    label = { Text("Texto") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                ElementType.EMOJI -> {
                    OutlinedTextField(
                        value = e.text, onValueChange = { onChange(e.copy(text = it)) },
                        label = { Text("Emoji o símbolo") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("✨", "🌙", "☀️", "🌈", "❤️", "🔥", "🎧", "☕", "🌸", "⚡", "🪐", "🐾").forEach { em ->
                            TextButton(onClick = { onChange(e.copy(text = em)) }) { Text(em, fontSize = 20.sp) }
                        }
                    }
                }
                ElementType.HOURLY, ElementType.DAILY -> {
                    val max = if (e.type == ElementType.HOURLY) 8f else 7f
                    LabeledSlider(
                        if (e.type == ElementType.HOURLY) "Horas a mostrar" else "Días a mostrar",
                        e.count.toFloat(), 3f..max, "${e.count}", steps = (max - 4).toInt(),
                    ) { onChange(e.copy(count = it.roundToInt())) }
                }
                ElementType.ROTATOR -> {
                    Text("Datos que van rotando:", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ROTATOR_OPTIONS.forEach { opt ->
                            val on = opt in e.rotateItems
                            FilterChip(
                                selected = on,
                                onClick = {
                                    val list = if (on) e.rotateItems - opt else e.rotateItems + opt
                                    if (list.isNotEmpty()) onChange(e.copy(rotateItems = list))
                                },
                                label = { Text("${opt.emoji} ${opt.label}") },
                            )
                        }
                    }
                    FieldLabel("Animación")
                    Segmented(FlipAnim.entries, e.flipAnim, { onChange(e.copy(flipAnim = it)) }) { Text(it.label) }
                    LabeledSlider("Cambiar cada", e.interval.toFloat(), 2f..12f, "${e.interval} s", steps = 9) {
                        onChange(e.copy(interval = it.roundToInt()))
                    }
                }
                ElementType.DAY_PROGRESS, ElementType.BATTERY_BAR ->
                    SwitchRow("Mostrar etiqueta", e.showLabel) { onChange(e.copy(showLabel = it)) }
                else -> Unit
            }
        }
    }

    // Tamaño y tipografía
    if (e.type != ElementType.DIVIDER) {
        GlassCard(Modifier.fillMaxWidth()) {
            val (label, range) = when (e.type) {
                ElementType.SPACER -> "Alto" to 2f..48f
                ElementType.ANALOG_CLOCK -> "Diámetro" to 40f..200f
                ElementType.DAY_PROGRESS, ElementType.BATTERY_BAR -> "Grosor" to 3f..24f
                ElementType.HOURLY, ElementType.DAILY -> "Tamaño del texto" to 9f..20f
                else -> "Tamaño" to 8f..110f
            }
            LabeledSlider(label, e.size.toFloat(), range, "${e.size}") { onChange(e.copy(size = it.roundToInt())) }
            if (e.type.isText || e.type == ElementType.HOURLY || e.type == ElementType.DAILY) {
                ChipGroup("Tipografía", FontOpt.entries, e.font, { it.label }) { onChange(e.copy(font = it)) }
            }
            if (e.type.isText && !e.type.isLiveClock) {
                SwitchRow("MAYÚSCULAS", e.uppercase) { onChange(e.copy(uppercase = it)) }
            }
        }
    }

    // Color
    if (e.type != ElementType.SPACER && e.type != ElementType.WEATHER_ICON && e.type != ElementType.EMOJI) {
        GlassCard(Modifier.fillMaxWidth()) {
            FieldLabel("Color")
            Segmented(ColorRole.entries, e.colorRole, { onChange(e.copy(colorRole = it)) }) { Text(it.label) }
            if (e.colorRole == ColorRole.CUSTOM) {
                ColorPickerRow("Color propio", e.customColor, { onChange(e.copy(customColor = it)) })
            }
        }
    }
}

@Composable
private fun LayoutPanel(d: WidgetDesign, onChange: (WidgetDesign) -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        FieldLabel("Distribución")
        LayoutPicker(d.layout) { onChange(d.copy(layout = it)) }
        if (d.layout == LayoutMode.SPLIT) {
            Spacer(Modifier.height(8.dp))
            LabeledSlider("Ancho columna izquierda", d.split, 0.25f..0.75f, "${(d.split * 100).roundToInt()}%") {
                onChange(d.copy(split = it))
            }
            SwitchRow("Línea divisoria", d.columnDivider) { onChange(d.copy(columnDivider = it)) }
            Text(
                "Usa la pestaña Elemento para mandar cada elemento a la izquierda o a la derecha.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    ColumnAlignCard(if (d.layout == LayoutMode.SPLIT) "Columna izquierda" else "Contenido", d.align, d.vAlign) { h, v ->
        onChange(d.copy(align = h, vAlign = v))
    }
    if (d.layout == LayoutMode.SPLIT) {
        ColumnAlignCard("Columna derecha", d.align2, d.vAlign2) { h, v -> onChange(d.copy(align2 = h, vAlign2 = v)) }
    }
    GlassCard(Modifier.fillMaxWidth()) {
        LabeledSlider("Margen interior", d.padding.toFloat(), 4f..32f, "${d.padding} dp") { onChange(d.copy(padding = it.roundToInt())) }
    }
}

@Composable
private fun ColumnAlignCard(title: String, h: HAlign, v: VAlign, onChange: (HAlign, VAlign) -> Unit) {
    val cs = MaterialTheme.colorScheme
    GlassCard(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        FieldLabel("Horizontal")
        Segmented(HAlign.entries, h, { onChange(it, v) }) { a ->
            AlignGlyph(
                when (a) {
                    HAlign.START -> ElemAlign.START
                    HAlign.CENTER -> ElemAlign.CENTER
                    HAlign.END -> ElemAlign.END
                },
                cs.onSurface,
            )
        }
        FieldLabel("Vertical")
        Segmented(VAlign.entries, v, { onChange(h, it) }) { Text(it.label) }
    }
}

@Composable
private fun BackgroundPanel(d: WidgetDesign, onChange: (WidgetDesign) -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        FieldLabel("Estilo")
        ChipGroup(null, BgStyle.entries, d.style, { it.label }) { onChange(d.copy(style = it)) }
        if (d.style == BgStyle.MATERIAL_YOU) {
            Text(
                "Material You toma los colores de tu fondo de pantalla (Android 12+).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (d.style != BgStyle.MATERIAL_YOU) {
        GlassCard(Modifier.fillMaxWidth()) {
            FieldLabel("Tinte")
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
        LabeledSlider("Esquinas", d.cornerRadius.toFloat(), 0f..48f, "${d.cornerRadius} dp") { onChange(d.copy(cornerRadius = it.roundToInt())) }
    }
}

@Composable
private fun ColorsPanel(d: WidgetDesign, onChange: (WidgetDesign) -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        FieldLabel("Colores del texto")
        if (d.style == BgStyle.MATERIAL_YOU) {
            Text(
                "Con Material You el texto usa los colores del sistema. Elige \"Propio\" en un elemento para forzar un color.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            ColorPickerRow("Texto principal", d.textColor, { onChange(d.copy(textColor = it)) })
            ColorPickerRow("Acento", d.accentColor, { onChange(d.copy(accentColor = it)) })
            FieldLabel("Combinaciones rápidas")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    0xFFFFFFFF to 0xFFFFD166, 0xFFFFFFFF to 0xFF80DEEA, 0xFF1A1A1A to 0xFFE53935,
                    0xFF39FF14 to 0xFFFF2BD6, 0xFFFFF3E0 to 0xFFFF8A65,
                ).forEach { (t, a) ->
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(Color(t.toInt()))
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { onChange(d.copy(textColor = t, accentColor = a)) },
                        contentAlignment = Alignment.Center,
                    ) { Box(Modifier.size(16.dp).clip(CircleShape).background(Color(a.toInt()))) }
                }
            }
        }
    }
}
