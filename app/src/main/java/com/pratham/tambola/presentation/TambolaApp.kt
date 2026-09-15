package com.pratham.tambola.presentation

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

private enum class Route { HOME, CALLER, SETTINGS }

@Composable
fun TambolaApp(model: TambolaViewModel) {
    val caller by model.caller.collectAsStateWithLifecycle()
    val saved by model.saved.collectAsStateWithLifecycle()
    val theme by model.theme.collectAsStateWithLifecycle()
    val busy by model.busy.collectAsStateWithLifecycle()
    val message by model.message.collectAsStateWithLifecycle()
    val now by model.now.collectAsStateWithLifecycle()
    val voice by model.voiceStatus.collectAsStateWithLifecycle()
    var route by rememberSaveable { mutableStateOf(Route.HOME) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val view = LocalView.current
    val context = LocalContext.current
    val snackbars = remember { SnackbarHostState() }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) model.foreground(true)
            if (event == Lifecycle.Event.ON_PAUSE) model.foreground(false)
        }
        lifecycle.addObserver(observer)
        model.foreground(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        onDispose { lifecycle.removeObserver(observer); model.foreground(false) }
    }
    DisposableEffect(view, route, caller.isPlaying) {
        view.keepScreenOn = route == Route.CALLER && caller.isPlaying
        onDispose { view.keepScreenOn = false }
    }
    LaunchedEffect(message) {
        message?.let { snackbars.showSnackbar(it); model.clearMessage() }
    }
    LaunchedEffect(caller.session, busy) {
        if (route == Route.CALLER && caller.session == null && !busy) route = Route.HOME
    }
    val back: () -> Unit = {
        model.pause()
        if (route == Route.CALLER) model.quit { route = Route.HOME } else route = Route.HOME
    }
    val speechSettings: () -> Unit = {
        model.pause()
        runCatching { context.startActivity(Intent("com.android.settings.TTS_SETTINGS")) }
            .onFailure {
                runCatching { context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)) }
                    .onFailure { model.reportMessage("Open your device Settings and search for Text-to-speech.") }
            }
    }

    TambolaTheme(theme) {
        Scaffold(snackbarHost = { SnackbarHost(snackbars) }) { insets ->
            NavDisplay(
                backStack = if (route == Route.HOME) listOf(Route.HOME) else listOf(Route.HOME, route),
                onBack = { back() },
                modifier = Modifier.fillMaxSize().padding(insets),
                entryProvider = { key ->
                    NavEntry(key) {
                        when (key) {
                            Route.HOME -> HomeScreen(
                                sessions = saved.filter { it.expiresAt > now }, now = now, busy = busy,
                                onNew = { model.newSession { route = Route.CALLER } },
                                onResume = { model.resume(it) { route = Route.CALLER } },
                                onSettings = { model.pause(); route = Route.SETTINGS },
                            )
                            Route.CALLER -> CallerScreen(caller, busy, back, model::togglePlayback,
                                { model.repeat(it) }, { model.setGap(it) }, speechSettings)
                            Route.SETTINGS -> SettingsScreen(theme, voice, back, model::setTheme, model::testVoice, speechSettings)
                        }
                    }
                },
            )
        }
    }
}
