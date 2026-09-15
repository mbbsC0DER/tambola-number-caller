package com.pratham.tambola.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pratham.tambola.data.ThemePreference
import com.pratham.tambola.domain.CallerState
import com.pratham.tambola.domain.PlaybackMode
import com.pratham.tambola.domain.Session
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToLong
import kotlinx.coroutines.launch

private fun numberLabel(number: Int?) = number?.toString()?.padStart(2, '0') ?: "—"
private fun gapLabel(ms: Long) = if (ms % 1_000 == 0L) "${ms / 1_000}s" else "${ms / 1_000}.${ms % 1_000 / 100}s"
private val dateFormat = DateTimeFormatter.ofPattern("d MMM · h:mm a", Locale.ENGLISH)

@Composable
fun HomeScreen(sessions: List<Session>, now: Long, busy: Boolean, onNew: () -> Unit, onResume: (Session) -> Unit, onSettings: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 640.dp),
        contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("TAMBOLA", style = MaterialTheme.typography.labelLarge, letterSpacing = 3.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onSettings, enabled = !busy) { Icon(AppIcons.Settings, "Settings") }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Icon(AppIcons.Volume, null, Modifier.size(32.dp))
                    Button(onClick = onNew, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Icon(AppIcons.Play, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (busy) "Getting ready…" else "Start new session")
                    }
                }
            }
        }
        item {
            Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Saved sessions", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text("${sessions.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Unfinished games stay on this device for 15 days.", modifier = Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (sessions.isEmpty()) item {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(AppIcons.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No saved sessions", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        items(sessions, key = { it.id }) { session ->
            OutlinedCard(onClick = { onResume(session) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Text(numberLabel(session.current), Modifier.padding(14.dp), style = MaterialTheme.typography.headlineSmall)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(dateFormat.format(Instant.ofEpochMilli(session.startedAt).atZone(ZoneId.systemDefault())), fontWeight = FontWeight.Medium)
                        Text("${session.drawnCount} called · ${90 - session.drawnCount} remaining", style = MaterialTheme.typography.bodySmall)
                        val days = ceil((session.expiresAt - now).coerceAtLeast(0) / 86_400_000.0).toInt()
                        Text("Expires in $days ${if (days == 1) "day" else "days"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(AppIcons.Chevron, "Resume session")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallerScreen(state: CallerState, busy: Boolean, onBack: () -> Unit, onToggle: () -> Unit, onRepeat: (Boolean) -> Unit, onGap: (Long) -> Unit, onSpeechSettings: () -> Unit) {
    val session = state.session ?: return
    var menu by remember { mutableStateOf(false) }
    var speedSheet by rememberSaveable { mutableStateOf(false) }
    val historyDrawer = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val status = when (state.mode) {
        PlaybackMode.PREPARING -> "Getting the voice ready…"
        PlaybackMode.CALLING -> "Calling numbers"
        PlaybackMode.READING_CALLED -> "Called numbers · ${state.readoutIndex} of ${state.readoutTotal}"
        PlaybackMode.READING_REMAINING -> "Remaining numbers · ${state.readoutIndex} of ${state.readoutTotal}"
        PlaybackMode.ALL_CALLED -> "All 90 numbers called"
        PlaybackMode.ERROR -> "Playback paused"
        else -> if (session.drawnCount == 0) "Ready" else "Paused"
    }
    PreviousNumbersDrawer(numbers = session.called.dropLast(1), drawerState = historyDrawer) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).widthIn(max = 640.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            FilledTonalIconButton(onClick = onBack, enabled = !busy, modifier = Modifier.size(48.dp)) { Icon(AppIcons.Back, "Quit session") }
            AssistChip(onClick = { speedSheet = true }, enabled = !state.isReading && !session.complete,
                label = { Text(gapLabel(session.gapMs), fontWeight = FontWeight.SemiBold) },
                leadingIcon = { Icon(AppIcons.Speed, null, Modifier.size(18.dp)) },
                modifier = Modifier.semantics { contentDescription = "Pause between numbers: ${gapLabel(session.gapMs)}" })
            Box {
                FilledTonalIconButton(onClick = { menu = true }, modifier = Modifier.size(48.dp), enabled = !busy) { Icon(AppIcons.Repeat, "Number readouts") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Read remaining numbers") }, enabled = session.remaining.isNotEmpty(), onClick = { menu = false; onRepeat(false) })
                    DropdownMenuItem(text = { Text("Repeat called numbers") }, enabled = session.called.isNotEmpty(), onClick = { menu = false; onRepeat(true) })
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(onClick = { drawerScope.launch { historyDrawer.open() } },
                    enabled = session.previous != null && !busy,
                    shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(64.dp).semantics { contentDescription = "Show previous numbers" }) {
                    Box(contentAlignment = Alignment.Center) { Text(numberLabel(session.previous), style = MaterialTheme.typography.headlineSmall) }
                }
            }
            Column(Modifier.weight(1.8f), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth().heightIn(min = 148.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 20.dp)) {
                        AnimatedContent(targetState = if (state.isReading) state.readoutNumber else session.current, label = "Current number") { number ->
                            Text(numberLabel(number), fontSize = 68.sp, fontWeight = FontWeight.Medium, letterSpacing = (-3).sp,
                                modifier = Modifier.semantics { contentDescription = "${if (state.isReading) "Readout" else "Current"} number ${number ?: "none"}" })
                        }
                    }
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val enabled = !busy && (!session.complete || state.isPlaying)
                FilledIconButton(onClick = onToggle, enabled = enabled, modifier = Modifier.size(64.dp)) {
                    Icon(when { state.isReading -> AppIcons.Stop; state.isPlaying -> AppIcons.Pause; else -> AppIcons.Play },
                        if (state.isReading) "Stop readout" else if (state.isPlaying) "Pause" else "Play", Modifier.size(32.dp))
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(if (state.isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline))
            Text(status, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        state.error?.let { error ->
            Card(Modifier.fillMaxWidth().padding(top = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = onSpeechSettings) { Text("Speech settings") }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp), horizontalArrangement = Arrangement.End) {
            Text("${session.drawnCount} called · ${session.remaining.size} left", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        NumberBoard(session.called.toSet(), session.current)
        Spacer(Modifier.height(24.dp))
    }
    }
    if (speedSheet) {
        var selection by remember { mutableFloatStateOf(session.gapMs.toFloat()) }
        ModalBottomSheet(onDismissRequest = { speedSheet = false }) {
            Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Pause between numbers", style = MaterialTheme.typography.headlineSmall)
                Text(gapLabel(selection.roundToLong()), style = MaterialTheme.typography.displaySmall)
                Slider(value = selection, onValueChange = { selection = (it / 500).roundToLong() * 500f },
                    valueRange = 1_000f..6_000f, steps = 9, enabled = !state.isReading && !session.complete,
                    modifier = Modifier.semantics { contentDescription = "Pause between numbers" })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("1 second"); Text("6 seconds") }
                Text("After each announcement. Readouts use a fixed 0.6s pause.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { onGap(selection.roundToLong()); speedSheet = false }, enabled = !state.isReading && !session.complete, modifier = Modifier.fillMaxWidth()) { Text("Set pace") }
            }
        }
    }
}

@Composable
fun NumberBoard(called: Set<Int>, current: Int?) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(9) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(10) { col ->
                    val number = row * 10 + col + 1
                    val selected = number == current
                    val wasCalled = number in called
                    val color = when { selected -> MaterialTheme.colorScheme.primary; wasCalled -> MaterialTheme.colorScheme.primaryContainer; else -> MaterialTheme.colorScheme.surfaceContainer }
                    val foreground = when { selected -> MaterialTheme.colorScheme.onPrimary; wasCalled -> MaterialTheme.colorScheme.onPrimaryContainer; else -> MaterialTheme.colorScheme.onSurfaceVariant }
                    Surface(color = color, contentColor = foreground, shape = RoundedCornerShape(6.dp),
                        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.onPrimaryContainer) else null,
                        modifier = Modifier.weight(1f).heightIn(min = 34.dp).semantics {
                            contentDescription = "$number, ${if (selected) "current" else if (wasCalled) "called" else "remaining"}"
                        }) {
                        Text(number.toString(), Modifier.padding(vertical = 9.dp), textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = if (wasCalled) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(theme: ThemePreference, voice: String, onBack: () -> Unit, onTheme: (ThemePreference) -> Unit, onTest: () -> Unit, onSpeechSettings: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(AppIcons.Back, "Back") }
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
        }
        Text("Appearance", style = MaterialTheme.typography.titleLarge)
        ThemePreference.entries.forEach { option ->
            Surface(onClick = { onTheme(option) }, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = theme == option, onClick = { onTheme(option) })
                    Text(option.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
        HorizontalDivider()
        Text("Number announcement", style = MaterialTheme.typography.titleLarge)
        Text("“One three, thirteen”", style = MaterialTheme.typography.headlineSmall)
        Text(voice, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedButton(onClick = onTest, modifier = Modifier.fillMaxWidth()) { Icon(AppIcons.Volume, null); Spacer(Modifier.width(8.dp)); Text("Test voice") }
        TextButton(onClick = onSpeechSettings) { Text("Open system speech settings") }
        HorizontalDivider()
        Text("Just on this device", style = MaterialTheme.typography.titleLarge)
        Text("Unfinished sessions are saved for 15 days from creation. Completed sessions are discarded. No account or cloud storage.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Tambola · 1.0.0", style = MaterialTheme.typography.labelSmall)
    }
}
