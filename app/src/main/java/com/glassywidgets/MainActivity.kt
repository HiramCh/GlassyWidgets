package com.glassywidgets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glassywidgets.data.WidgetDesign
import com.glassywidgets.ui.MainViewModel
import com.glassywidgets.ui.components.GlassNavBar
import com.glassywidgets.ui.components.LiquidBackground
import com.glassywidgets.ui.screens.CreateScreen
import com.glassywidgets.ui.screens.EditorScreen
import com.glassywidgets.ui.screens.HomeScreen
import com.glassywidgets.ui.screens.SettingsScreen
import com.glassywidgets.ui.theme.GlassyTheme
import com.glassywidgets.ui.theme.LocalGlassMode
import com.glassywidgets.ui.theme.LocalNow
import com.glassywidgets.ui.theme.rememberNow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val settings by vm.settings.collectAsStateWithLifecycle()
            GlassyTheme {
                CompositionLocalProvider(
                    LocalGlassMode provides settings.appGlass,
                    LocalNow provides rememberNow(),
                ) {
                    AppRoot(vm)
                }
            }
        }
    }
}

@Composable
private fun AppRoot(vm: MainViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<WidgetDesign?>(null) }
    // Conserva el último diseño mientras corre la animación de salida del editor.
    var lastEditing by remember { mutableStateOf<WidgetDesign?>(null) }
    val weather by vm.weather.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.refreshIfStale() }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground {
            Box(Modifier.fillMaxSize().statusBarsPadding()) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        val dir = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(tween(320)) { it / 6 * dir } + fadeIn(tween(320))) togetherWith
                            (slideOutHorizontally(tween(240)) { -it / 6 * dir } + fadeOut(tween(200)))
                    },
                    label = "tabs",
                ) { t ->
                    when (t) {
                        0 -> HomeScreen(vm)
                        1 -> CreateScreen(vm) { editing = it; lastEditing = it }
                        else -> SettingsScreen(vm)
                    }
                }
                GlassNavBar(
                    selected = tab,
                    onSelect = { tab = it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = editing != null,
            enter = slideInVertically(tween(380)) { it / 3 } + fadeIn(tween(300)) + scaleIn(initialScale = 0.96f),
            exit = slideOutVertically(tween(280)) { it / 3 } + fadeOut(tween(220)),
        ) {
            val design = editing ?: lastEditing
            if (design != null) {
                // key: reinicia el estado del editor al abrir otro diseño
                androidx.compose.runtime.key(design.id) {
                    EditorScreen(
                        initial = design,
                        weather = weather,
                        wallpaper = settings.wallpaper,
                        onWallpaper = { w -> vm.updateSettings { it.copy(wallpaper = w) } },
                        onSave = { vm.saveDesign(it) },
                        onPin = { vm.pinCustom(it) },
                        onClose = { editing = null },
                    )
                }
            }
        }
    }
}
