package com.glassywidgets.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.graphics.ColorUtils
import com.glassywidgets.data.BgStyle
import com.glassywidgets.data.WidgetDesign
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Dibuja el fondo "liquid glass" como bitmap.
 *
 * Los widgets de Android (RemoteViews) no permiten desenfocar lo que hay detrás,
 * así que simulamos el cristal con capas: tinte translúcido en degradado, brillo
 * especular superior, sombra interna inferior y un borde de luz (rim light).
 * El mismo bitmap se usa en la vista previa de la app, así lo que ves es lo que obtienes.
 */
object GlassPainter {

    private const val MAX_PIXELS = 700_000f // límite seguro para RemoteViews

    fun render(context: Context, widthDp: Float, heightDp: Float, d: WidgetDesign): Bitmap {
        val wDp = widthDp.coerceAtLeast(40f)
        val hDp = heightDp.coerceAtLeast(40f)
        var scale = context.resources.displayMetrics.density
        if (wDp * hDp * scale * scale > MAX_PIXELS) scale = sqrt(MAX_PIXELS / (wDp * hDp))
        val w = max(1, (wDp * scale).roundToInt())
        val h = max(1, (hDp * scale).roundToInt())

        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        if (d.style == BgStyle.MATERIAL_YOU) return bmp // Material You usa colores del tema

        val c = Canvas(bmp)
        val wf = w.toFloat()
        val hf = h.toFloat()
        val radius = d.cornerRadius * scale
        val clip = Path().apply { addRoundRect(RectF(0f, 0f, wf, hf), radius, radius, Path.Direction.CW) }
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val c1 = d.color1.toInt()
        val c2 = d.color2.toInt()
        val a = d.opacity.coerceIn(0f, 1f)

        c.save()
        c.clipPath(clip)
        when (d.style) {
            BgStyle.GLASS -> {
                p.shader = LinearGradient(0f, 0f, wf, hf, alpha(c1, a), alpha(c2, a * 0.8f), Shader.TileMode.CLAMP)
                c.drawRect(0f, 0f, wf, hf, p)
                // Escarcha: velo blanco muy suave
                c.drawColor(alpha(Color.WHITE, 0.06f + a * 0.12f))
                glassLights(c, p, wf, hf, 0.45f)
            }
            BgStyle.DARK_GLASS -> {
                val d1 = ColorUtils.blendARGB(c1, Color.BLACK, 0.72f)
                val d2 = ColorUtils.blendARGB(c2, Color.BLACK, 0.80f)
                val da = 0.45f + a * 0.45f
                p.shader = LinearGradient(0f, 0f, wf, hf, alpha(d1, da), alpha(d2, da), Shader.TileMode.CLAMP)
                c.drawRect(0f, 0f, wf, hf, p)
                glassLights(c, p, wf, hf, 0.20f)
            }
            BgStyle.SOLID -> {
                p.shader = null
                p.color = alpha(c1, a)
                c.drawRect(0f, 0f, wf, hf, p)
            }
            BgStyle.GRADIENT -> {
                p.shader = LinearGradient(0f, 0f, wf, hf, alpha(c1, a), alpha(c2, a), Shader.TileMode.CLAMP)
                c.drawRect(0f, 0f, wf, hf, p)
            }
            BgStyle.MATERIAL_YOU -> Unit
        }
        c.restore()

        // Borde de luz (rim light) estilo liquid glass
        if (d.rimLight && (d.style == BgStyle.GLASS || d.style == BgStyle.DARK_GLASS)) {
            val sw = 1.3f * scale
            val rimA = if (d.style == BgStyle.GLASS) 1f else 0.6f
            val rim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = sw
                shader = LinearGradient(
                    0f, 0f, wf, hf,
                    intArrayOf(
                        alpha(Color.WHITE, 0.90f * rimA),
                        alpha(Color.WHITE, 0.18f * rimA),
                        alpha(Color.WHITE, 0.06f * rimA),
                        alpha(Color.WHITE, 0.55f * rimA),
                    ),
                    floatArrayOf(0f, 0.35f, 0.65f, 1f),
                    Shader.TileMode.CLAMP,
                )
            }
            val inset = sw / 2f
            c.drawRoundRect(RectF(inset, inset, wf - inset, hf - inset), radius - inset, radius - inset, rim)
        }
        return bmp
    }

    /** Brillo especular superior + cáustica inferior + sombra interna. */
    private fun glassLights(c: Canvas, p: Paint, w: Float, h: Float, strength: Float) {
        val big = max(w, h)
        p.shader = RadialGradient(
            w * 0.18f, -h * 0.15f, big * 0.85f,
            alpha(Color.WHITE, strength), Color.TRANSPARENT, Shader.TileMode.CLAMP,
        )
        c.drawRect(0f, 0f, w, h, p)
        p.shader = RadialGradient(
            w * 0.9f, h * 1.05f, big * 0.45f,
            alpha(Color.WHITE, strength * 0.35f), Color.TRANSPARENT, Shader.TileMode.CLAMP,
        )
        c.drawRect(0f, 0f, w, h, p)
        p.shader = LinearGradient(0f, h * 0.55f, 0f, h, Color.TRANSPARENT, alpha(Color.BLACK, 0.10f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, w, h, p)
        p.shader = null
    }

    /** Barra de progreso tipo cápsula (día, batería). */
    fun bar(context: Context, widthDp: Float, heightDp: Float, fraction: Float, fg: Int, track: Int): Bitmap {
        val d = context.resources.displayMetrics.density.coerceAtMost(3f)
        val w = max(2, (widthDp.coerceAtLeast(10f) * d).roundToInt())
        val h = max(2, (heightDp.coerceAtLeast(2f) * d).roundToInt())
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val r = h / 2f
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = track
        c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), r, r, p)
        val fw = (w * fraction.coerceIn(0f, 1f)).coerceAtLeast(h.toFloat())
        p.shader = LinearGradient(0f, 0f, fw, 0f, alpha(fg, 0.75f), fg, Shader.TileMode.CLAMP)
        c.drawRoundRect(RectF(0f, 0f, fw, h.toFloat()), r, r, p)
        // brillo superior
        p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), alpha(Color.WHITE, 0.35f), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        c.drawRoundRect(RectF(0f, 0f, fw, h * 0.6f), r, r, p)
        return bmp
    }

    private fun alpha(color: Int, a: Float): Int =
        ColorUtils.setAlphaComponent(color, (a.coerceIn(0f, 1f) * 255).roundToInt())
}
