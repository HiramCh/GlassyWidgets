package com.glassywidgets.ui.components

import android.graphics.Typeface
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.glassywidgets.data.BgStyle
import com.glassywidgets.data.ColorRole
import com.glassywidgets.data.DialStyle
import com.glassywidgets.data.ElemAlign
import com.glassywidgets.data.ElementType
import com.glassywidgets.data.FlipAnim
import com.glassywidgets.data.FontOpt
import com.glassywidgets.data.HAlign
import com.glassywidgets.data.LayoutMode
import com.glassywidgets.data.SystemInfo
import com.glassywidgets.data.VAlign
import com.glassywidgets.data.WeatherData
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.data.WidgetElement
import com.glassywidgets.ui.theme.LocalNow
import com.glassywidgets.widget.DesignLogic
import com.glassywidgets.widget.GlassPainter
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.max

private val fontCache = mutableMapOf<FontOpt, FontFamily>()
private fun FontOpt.compose(): FontFamily =
    fontCache.getOrPut(this) { FontFamily(Typeface.create(family, Typeface.NORMAL)) }

private fun HAlign.compose(): Alignment.Horizontal = when (this) {
    HAlign.START -> Alignment.Start
    HAlign.CENTER -> Alignment.CenterHorizontally
    HAlign.END -> Alignment.End
}

private fun VAlign.compose(): Arrangement.Vertical = when (this) {
    VAlign.TOP -> Arrangement.Top
    VAlign.CENTER -> Arrangement.Center
    VAlign.BOTTOM -> Arrangement.Bottom
}

private class PCtx(
    val design: WidgetDesign,
    val weather: WeatherData?,
    val sys: SystemInfo,
    val is24: Boolean,
    val scale: Float,
    val materialYou: Boolean,
    val selectedId: String?,
    val onSelect: ((String) -> Unit)?,
    val innerW: Float,
    val innerH: Float,
    val onTransform: ((String, Float, Float, Float) -> Unit)?,
    val onGestureEnd: (() -> Unit)?,
)

/**
 * Vista previa en vivo de un diseño. Usa el mismo bitmap de fondo y la misma
 * lógica de escala que el widget real, así que se ve igual en la pantalla de inicio.
 * En el editor, cada elemento se puede tocar para seleccionarlo.
 */
@Composable
fun DesignPreview(
    design: WidgetDesign,
    weather: WeatherData?,
    modifier: Modifier = Modifier,
    selectedId: String? = null,
    onSelect: ((String) -> Unit)? = null,
    /** Modo libre: (id, dx, dy en fracción del área, zoom). */
    onTransform: ((String, Float, Float, Float) -> Unit)? = null,
    onGestureEnd: (() -> Unit)? = null,
    guideX: Float? = null,
    guideY: Float? = null,
    showGrid: Boolean = false,
) {
    BoxWithConstraints(modifier) {
        val context = LocalContext.current
        val wDp = maxWidth.value
        val hDp = maxHeight.value
        val materialYou = design.style == BgStyle.MATERIAL_YOU
        val bgKey = design.copy(elements = emptyList(), name = "")
        val bitmap = remember(bgKey, wDp, hDp) {
            if (materialYou) null else GlassPainter.render(context, wDp, hDp, design).asImageBitmap()
        }
        val (iw, ih) = DesignLogic.innerSize(design, wDp, hDp)
        val c = PCtx(
            design, weather, SystemInfo.PREVIEW, DateFormat.is24HourFormat(context),
            DesignLogic.scale(design, wDp, hDp, weather), materialYou, selectedId, onSelect,
            iw, ih, onTransform, onGestureEnd,
        )
        val geo = DesignLogic.geometry(design, wDp)
        val cols = DesignLogic.columns(design)
        val cs = MaterialTheme.colorScheme

        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(design.cornerRadius.dp))
                .then(if (materialYou) Modifier.background(cs.surfaceContainerHigh) else Modifier),
        ) {
            bitmap?.let {
                Image(it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            }
            if (design.layout == LayoutMode.FREE) {
                Box(Modifier.fillMaxSize().padding(design.padding.dp)) {
                    if (showGrid || guideX != null || guideY != null) {
                        val gc = MaterialTheme.colorScheme.primary
                        Canvas(Modifier.fillMaxSize()) {
                            if (showGrid) {
                                for (f in listOf(0.25f, 0.5f, 0.75f)) {
                                    drawLine(Color.White.copy(alpha = 0.18f), Offset(size.width * f, 0f), Offset(size.width * f, size.height), 1f)
                                    drawLine(Color.White.copy(alpha = 0.18f), Offset(0f, size.height * f), Offset(size.width, size.height * f), 1f)
                                }
                            }
                            guideX?.let { drawLine(gc, Offset(size.width * it, -40f), Offset(size.width * it, size.height + 40f), 2f) }
                            guideY?.let { drawLine(gc, Offset(-40f, size.height * it), Offset(size.width + 40f, size.height * it), 2f) }
                        }
                    }
                    design.elements.forEach { e ->
                        val ins = DesignLogic.freeInsets(e, iw, ih)
                        Box(
                            Modifier.fillMaxSize().padding(start = ins.start.dp, top = ins.top.dp, end = ins.end.dp, bottom = ins.bottom.dp),
                            contentAlignment = freeAlign(e),
                        ) {
                            PElement(e, c, e.w * iw, e.w * iw, e.h * ih)
                        }
                    }
                }
            } else if (design.layout == LayoutMode.SINGLE) {
                PColumn(cols[0], c, geo.widths[0], design.align, design.vAlign, Modifier.fillMaxSize().padding(design.padding.dp))
            } else {
                Row(Modifier.fillMaxSize().padding(design.padding.dp), verticalAlignment = Alignment.CenterVertically) {
                    PColumn(cols[0], c, geo.widths[0], design.align, design.vAlign, Modifier.width(geo.widths[0].dp).fillMaxHeight())
                    Spacer(Modifier.width((geo.gap / 2f).dp))
                    if (design.columnDivider) {
                        Box(Modifier.width(1.dp).fillMaxHeight().background(lineColor(c)))
                    }
                    Spacer(Modifier.width((geo.gap / 2f - 1f).dp))
                    PColumn(cols[1], c, geo.widths[1], design.align2, design.vAlign2, Modifier.width(geo.widths[1].dp).fillMaxHeight())
                }
            }
        }
    }
}

private fun freeAlign(e: WidgetElement): Alignment = when (e.anchorV) {
    VAlign.TOP -> when (e.anchorH) { HAlign.START -> Alignment.TopStart; HAlign.CENTER -> Alignment.TopCenter; HAlign.END -> Alignment.TopEnd }
    VAlign.CENTER -> when (e.anchorH) { HAlign.START -> Alignment.CenterStart; HAlign.CENTER -> Alignment.Center; HAlign.END -> Alignment.CenterEnd }
    VAlign.BOTTOM -> when (e.anchorH) { HAlign.START -> Alignment.BottomStart; HAlign.CENTER -> Alignment.BottomCenter; HAlign.END -> Alignment.BottomEnd }
}

@Composable
private fun lineColor(c: PCtx): Color =
    if (c.materialYou) MaterialTheme.colorScheme.outline else Color(c.design.textColor.toInt()).copy(alpha = 0.30f)

@Composable
private fun mutedColor(c: PCtx): Color =
    if (c.materialYou) MaterialTheme.colorScheme.onSurfaceVariant else Color(c.design.textColor.toInt()).copy(alpha = 0.72f)

@Composable
private fun colorFor(e: WidgetElement, c: PCtx): Color {
    val cs = MaterialTheme.colorScheme
    return if (c.materialYou && e.colorRole != ColorRole.CUSTOM) {
        when (e.colorRole) {
            ColorRole.ACCENT -> cs.primary
            ColorRole.MUTED -> cs.onSurfaceVariant
            else -> cs.onSurface
        }.let { it.copy(alpha = it.alpha * e.alpha) }
    } else {
        Color(DesignLogic.colorArgb(e, c.design))
    }
}

@Composable
private fun PColumn(elements: List<WidgetElement>, c: PCtx, colW: Float, hAlign: HAlign, vAlign: VAlign, modifier: Modifier) {
    Column(modifier, verticalArrangement = vAlign.compose(), horizontalAlignment = hAlign.compose()) {
        DesignLogic.rows(elements).forEach { row ->
            val override = when (row.first().align) {
                ElemAlign.AUTO -> null
                ElemAlign.START -> Alignment.CenterStart
                ElemAlign.CENTER -> Alignment.Center
                ElemAlign.END -> Alignment.CenterEnd
            }
            if (override == null) {
                PRow(row, c, colW)
            } else {
                Box(Modifier.fillMaxWidth(), contentAlignment = override) { PRow(row, c, colW) }
            }
        }
    }
}

@Composable
private fun PRow(row: List<WidgetElement>, c: PCtx, colW: Float) {
    if (row.size == 1) {
        PElement(row[0], c, colW, row[0].w * colW, row[0].h * c.innerH)
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            row.forEachIndexed { i, e ->
                if (i > 0) Spacer(Modifier.width((8 * c.scale).dp))
                PElement(e, c, colW, e.w * colW, e.h * c.innerH)
            }
        }
    }
}

private fun textStyle(color: Color, sizeSp: Float, e: WidgetElement, tight: Boolean = false, center: Boolean = false) = TextStyle(
    color = color,
    fontSize = sizeSp.sp,
    fontFamily = e.font.compose(),
    lineHeight = if (tight) 1.05.em else 1.25.em,
    textAlign = if (center) TextAlign.Center else TextAlign.Unspecified,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)

@Composable
private fun PElement(e: WidgetElement, c: PCtx, colW: Float, boxW: Float, boxH: Float) {
    val selected = c.selectedId == e.id
    val primary = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val transform = c.onTransform
    val selectable = Modifier
        .then(
            if (transform != null && c.design.layout == LayoutMode.FREE) {
                // Arrastrar para mover, pellizcar para cambiar el tamaño.
                val wPx = with(density) { c.innerW.dp.toPx() }
                val hPx = with(density) { c.innerH.dp.toPx() }
                Modifier.pointerInput(e.id, wPx, hPx) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        c.onSelect?.invoke(e.id)
                        do {
                            val event = awaitPointerEvent()
                            val pan = event.calculatePan()
                            val zoom = event.calculateZoom()
                            if (pan != Offset.Zero || zoom != 1f) {
                                transform(e.id, pan.x / wPx, pan.y / hPx, zoom)
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        } while (event.changes.any { it.pressed })
                        c.onGestureEnd?.invoke()
                    }
                }
            } else if (c.onSelect != null) {
                Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { c.onSelect?.invoke(e.id) }
            } else Modifier,
        )
        .then(
            if (selected) Modifier.border(1.5.dp, primary, RoundedCornerShape(6.dp)).background(primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            else if (c.onTransform != null && c.design.layout == LayoutMode.FREE) Modifier.border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            else Modifier,
        )

    val color = colorFor(e, c)
    val px = e.size * c.scale
    val now by LocalNow.current

    Box(selectable) {
        when (e.type) {
            ElementType.TIME, ElementType.DATE ->
                Text(DesignLogic.formatClock(e, now, c.is24), maxLines = 1, style = textStyle(color, px, e, tight = true))

            ElementType.WORLD_CLOCK -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text(e.zoneLabel, maxLines = 1, style = textStyle(mutedColor(c), px * 0.62f, e))
                Spacer(Modifier.width((6 * c.scale).dp))
                Text(DesignLogic.formatClock(e, now, c.is24), maxLines = 1, style = textStyle(color, px, e, tight = true))
            }

            ElementType.ANALOG_CLOCK -> {
                val accent = colorFor(e.copy(colorRole = ColorRole.ACCENT), c)
                AnalogFace(now, px, e.dialStyle, e.showSeconds, color, accent)
            }

            ElementType.ROTATOR -> Rotator(e, c, color, px)

            ElementType.HOURLY, ElementType.DAILY -> {
                val cells = if (e.type == ElementType.HOURLY) DesignLogic.hourlyCells(e, c.weather, now, c.is24)
                else DesignLogic.dailyCells(e, c.weather)
                Row(Modifier.width(colW.dp)) {
                    cells.forEach { cell ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(cell.label, maxLines = 1, style = textStyle(mutedColor(c), px * 0.78f, e, center = true))
                            Text(cell.icon, maxLines = 1, style = textStyle(color, px * 1.35f, e, center = true))
                            Text(cell.main, maxLines = 1, style = textStyle(color, px, e, center = true))
                            cell.sub?.let { Text(it, maxLines = 1, style = textStyle(mutedColor(c), px * 0.78f, e, center = true)) }
                        }
                    }
                }
            }

            ElementType.DAY_PROGRESS, ElementType.BATTERY_BAR -> Column {
                if (e.showLabel) {
                    Text(DesignLogic.barLabel(e, c.sys, now), maxLines = 1, style = textStyle(mutedColor(c), 12f * c.scale, e))
                    Spacer(Modifier.height(4.dp))
                }
                val frac = DesignLogic.barFraction(e, c.sys, now)
                Box(
                    Modifier.width(colW.dp).height(px.dp).clip(RoundedCornerShape(50))
                        .background(color.copy(alpha = 0.24f)),
                ) {
                    Box(
                        Modifier.fillMaxHeight().width((colW * frac).coerceAtLeast(px).dp).clip(RoundedCornerShape(50))
                            .background(color),
                    )
                }
            }

            ElementType.DIVIDER -> Box(
                Modifier.width(colW.dp).padding(vertical = (4 * c.scale).dp).height(1.dp).background(lineColor(c)),
            )

            ElementType.SHAPE -> {
                if (e.shapeStyle == BgStyle.MATERIAL_YOU) {
                    Box(
                        Modifier.size(boxW.dp, boxH.dp).clip(RoundedCornerShape(e.radius.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                    )
                } else {
                    val ctx = LocalContext.current
                    val bmp = remember(e.shapeStyle, e.customColor, e.customColor2, e.opacity, e.radius, boxW, boxH) {
                        GlassPainter.shape(ctx, e, boxW, boxH).asImageBitmap()
                    }
                    Image(bmp, null, Modifier.size(boxW.dp, boxH.dp), contentScale = ContentScale.FillBounds)
                }
            }

            ElementType.PHOTO -> {
                val ctx = LocalContext.current
                val bmp = remember(e.text, e.radius, e.customColor, e.customColor2, boxW, boxH) {
                    GlassPainter.photo(ctx, e, boxW, boxH).asImageBitmap()
                }
                Box(contentAlignment = Alignment.Center) {
                    Image(bmp, null, Modifier.size(boxW.dp, boxH.dp), contentScale = ContentScale.FillBounds)
                    if (e.text.isBlank()) Text("🖼️ Elige una foto", style = textStyle(Color.White, 12f, e))
                }
            }

            ElementType.RING -> Box(Modifier.size(px.dp), contentAlignment = Alignment.Center) {
                val frac = DesignLogic.ringFraction(e, c.weather, c.sys, now)
                Canvas(Modifier.fillMaxSize()) {
                    val sw = size.minDimension * 0.11f
                    val inset = sw / 2
                    val arcSize = androidx.compose.ui.geometry.Size(size.width - sw, size.height - sw)
                    drawArc(color.copy(alpha = 0.22f), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(sw, cap = StrokeCap.Round))
                    drawArc(color, -90f, 360f * frac.coerceIn(0.005f, 1f), false, Offset(inset, inset), arcSize, style = Stroke(sw, cap = StrokeCap.Round))
                }
                if (e.showLabel) {
                    Text(DesignLogic.ringLabel(e, c.weather, c.sys, now), maxLines = 1, style = textStyle(color, px * 0.19f, e, center = true))
                }
            }

            ElementType.SPACER -> Spacer(Modifier.height(px.dp).width(if (selected) 24.dp else 0.dp))

            else -> Text(
                DesignLogic.text(e, c.weather, c.sys, now, c.is24),
                maxLines = 1,
                style = textStyle(color, px, e),
            )
        }
    }
}

@Composable
private fun Rotator(e: WidgetElement, c: PCtx, color: Color, px: Float) {
    val now by LocalNow.current
    val texts = DesignLogic.rotatorTexts(e, c.weather, c.sys, now, c.is24)
    var index by remember(e.rotateItems) { mutableIntStateOf(0) }
    LaunchedEffect(e.rotateItems, e.interval) {
        while (true) {
            delay(e.interval.coerceIn(2, 30) * 1000L)
            index++
        }
    }
    AnimatedContent(
        targetState = index,
        transitionSpec = {
            when (e.flipAnim) {
                FlipAnim.FADE -> fadeIn() togetherWith fadeOut()
                FlipAnim.SLIDE_UP -> (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                FlipAnim.SLIDE_SIDE -> (slideInHorizontally { it / 2 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 2 } + fadeOut())
            }
        },
        label = "rotator",
    ) { i ->
        Text(texts[Math.floorMod(i, texts.size)], maxLines = 1, style = textStyle(color, px, e))
    }
}

/** Reloj analógico dibujado con la misma geometría que el widget real. */
@Composable
fun AnalogFace(now: Long, sizeDp: Float, style: DialStyle, seconds: Boolean, hand: Color, accent: Color) {
    val cal = Calendar.getInstance().apply { timeInMillis = now }
    val h = cal.get(Calendar.HOUR) + cal.get(Calendar.MINUTE) / 60f
    val m = cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND) / 60f
    val s = cal.get(Calendar.SECOND).toFloat()
    Canvas(Modifier.size(max(sizeDp, 20f).dp)) {
        val u = size.minDimension / 400f
        val center = Offset(size.width / 2, size.height / 2)
        if (style == DialStyle.GLASS) {
            drawCircle(hand.copy(alpha = 0.20f), 194 * u, center)
            drawCircle(hand.copy(alpha = 0.70f), 194 * u, center, style = Stroke(3 * u))
        }
        for (i in 0 until 12) {
            val major = i % 3 == 0
            if (style == DialStyle.MINIMAL && !major) continue
            rotate(i * 30f, center) {
                drawLine(
                    hand.copy(alpha = 0.85f),
                    Offset(center.x, center.y - (if (major) 162 else 172) * u),
                    Offset(center.x, center.y - 184 * u),
                    strokeWidth = (if (major) 9 else 5) * u,
                    cap = StrokeCap.Round,
                )
            }
        }
        rotate(h * 30f, center) {
            drawLine(hand, Offset(center.x, center.y + 14 * u), Offset(center.x, center.y - 92 * u), 16 * u, StrokeCap.Round)
        }
        rotate(m * 6f, center) {
            drawLine(hand, Offset(center.x, center.y + 18 * u), Offset(center.x, center.y - 142 * u), 10 * u, StrokeCap.Round)
        }
        drawCircle(hand, 13 * u, center)
        if (seconds) {
            rotate(s * 6f, center) {
                drawLine(accent, Offset(center.x, center.y + 32 * u), Offset(center.x, center.y - 154 * u), 4 * u, StrokeCap.Round)
            }
            drawCircle(accent, 8 * u, center)
        }
    }
}

/** Utilidad para colores con alfa a partir de ARGB Long. */
fun Long.withAlpha(a: Float): Long = (ColorUtils.setAlphaComponent(toInt(), (a * 255).toInt()).toLong() and 0xFFFFFFFFL)
