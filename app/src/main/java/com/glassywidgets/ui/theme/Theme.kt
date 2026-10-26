package com.glassywidgets.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

/** true = interfaz Liquid Glass, false = Material You clásico. */
val LocalGlassMode = staticCompositionLocalOf { true }

/**
 * Hora actual compartida por todas las vistas previas. Se pasa como State para que
 * solo se recompongan los textos de hora/fecha cada segundo, no toda la app.
 */
val LocalNow = staticCompositionLocalOf<State<Long>> { mutableLongStateOf(System.currentTimeMillis()) }

@Composable
fun rememberNow(): State<Long> = produceState(System.currentTimeMillis()) {
    while (true) {
        delay(1000 - System.currentTimeMillis() % 1000)
        value = System.currentTimeMillis()
    }
}

private val FallbackLight = lightColorScheme(
    primary = Color(0xFF4F5BD5),
    secondary = Color(0xFF7E57C2),
    tertiary = Color(0xFFE5677F),
)
private val FallbackDark = darkColorScheme(
    primary = Color(0xFFB8C3FF),
    secondary = Color(0xFFD0BCFF),
    tertiary = Color(0xFFFFB1C1),
)

/** Material 3 con color dinámico (Material You) en Android 12+. */
@Composable
fun GlassyTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> FallbackDark
        else -> FallbackLight
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
