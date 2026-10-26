package com.glassywidgets.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val palette = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFF8EC5FF, 0xFFE0C3FC, 0xFFFFD166, 0xFFFF6B6B,
    0xFF06D6A0, 0xFF118AB2, 0xFFFF7E5F, 0xFFFEB47B, 0xFF39FF14, 0xFFB388FF,
    0xFF80DEEA, 0xFFFFAB91, 0xFF263238, 0xFF90A4AE, 0xFFF48FB1, 0xFFA5D6A7,
)

private fun Long.toHex() = "#%06X".format(this and 0xFFFFFF)

/** Selector de color: muestra + paleta rápida + ajuste fino HSV. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerRow(label: String, color: Long, onChange: (Long) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable { open = !open }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(Color(color.toInt()))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            )
            Spacer(Modifier.width(12.dp))
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text(color.toHex(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AnimatedVisibility(open) {
            Column(Modifier.padding(bottom = 8.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    palette.forEach { c ->
                        val selected = (c and 0xFFFFFF) == (color and 0xFFFFFF)
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(Color(c.toInt()))
                                .border(
                                    if (selected) 3.dp else 1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape,
                                )
                                .clickable { onChange(c) },
                        )
                    }
                }
                val hsv = remember(color) { FloatArray(3).also { android.graphics.Color.colorToHSV(color.toInt(), it) } }
                fun emit(h: Float = hsv[0], s: Float = hsv[1], v: Float = hsv[2]) {
                    val argb = android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))
                    onChange(argb.toLong() and 0xFFFFFFFFL)
                }
                Spacer(Modifier.size(8.dp))
                LabeledSlider("Tono", hsv[0], 0f..360f) { emit(h = it) }
                LabeledSlider("Saturación", hsv[1], 0f..1f) { emit(s = it) }
                LabeledSlider("Brillo", hsv[2], 0f..1f) { emit(v = it) }
            }
        }
    }
}

@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: String? = null,
    steps: Int = 0,
    onChange: (Float) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            if (valueText != null) {
                Text(valueText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Slider(value = value.coerceIn(range), onValueChange = onChange, valueRange = range, steps = steps)
    }
}
