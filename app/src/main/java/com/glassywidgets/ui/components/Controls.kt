package com.glassywidgets.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.glassywidgets.data.ElemAlign
import com.glassywidgets.data.LayoutMode

/** Botones segmentados de Material 3. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Segmented(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        options.forEachIndexed { i, o ->
            SegmentedButton(
                selected = o == selected,
                onClick = { onSelect(o) },
                shape = SegmentedButtonDefaults.itemShape(i, options.size),
                icon = {},
                label = { label(o) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChipGroup(title: String?, options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Column {
        if (title != null) FieldLabel(title)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { o ->
                FilterChip(selected = o == selected, onClick = { onSelect(o) }, label = { Text(label(o)) })
            }
        }
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(
        text,
        Modifier.padding(top = 4.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** Pequeño icono de "líneas alineadas" (izquierda/centro/derecha/auto). */
@Composable
fun AlignGlyph(align: ElemAlign, color: Color) {
    Canvas(Modifier.size(18.dp)) {
        val w = size.width
        val lens = listOf(1f, 0.6f, 0.85f)
        lens.forEachIndexed { i, l ->
            val y = size.height * (0.22f + i * 0.28f)
            val lw = w * l
            val x0 = when (align) {
                ElemAlign.START -> 0f
                ElemAlign.CENTER, ElemAlign.AUTO -> (w - lw) / 2
                ElemAlign.END -> w - lw
            }
            drawLine(
                color.copy(alpha = if (align == ElemAlign.AUTO) 0.45f else 1f),
                Offset(x0, y), Offset(x0 + lw, y), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round,
            )
        }
    }
}

/** Selector visual de distribución: una columna o dos columnas. */
@Composable
fun LayoutPicker(selected: LayoutMode, onSelect: (LayoutMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LayoutMode.entries.forEach { mode ->
            val sel = mode == selected
            val cs = MaterialTheme.colorScheme
            val border by animateColorAsState(if (sel) cs.primary else cs.outlineVariant, label = "lp")
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(if (sel) 2.dp else 1.dp, border, RoundedCornerShape(18.dp))
                    .background(if (sel) cs.primary.copy(alpha = 0.10f) else Color.Transparent)
                    .clickable { onSelect(mode) }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(10.dp))
                        .background(cs.onSurface.copy(alpha = 0.08f)).padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val fill = if (sel) cs.primary.copy(alpha = 0.55f) else cs.onSurface.copy(alpha = 0.22f)
                    if (mode == LayoutMode.FREE) {
                        Box(Modifier.weight(1f).height(42.dp)) {
                            Box(Modifier.align(Alignment.TopStart).size(26.dp, 12.dp).clip(RoundedCornerShape(4.dp)).background(fill))
                            Box(Modifier.align(Alignment.Center).size(16.dp).clip(RoundedCornerShape(8.dp)).background(fill))
                            Box(Modifier.align(Alignment.BottomEnd).size(34.dp, 10.dp).clip(RoundedCornerShape(4.dp)).background(fill))
                        }
                    } else {
                        repeat(if (mode == LayoutMode.SPLIT) 2 else 1) {
                            Box(Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(6.dp)).background(fill))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(mode.label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            }
        }
    }
}
