package com.pratham.tambola.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** A clipped, start-edge overlay; inspecting history never changes live playback. */
@Composable
fun PreviousNumbersDrawer(numbers: List<Int>, drawerState: DrawerState, content: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    val close: () -> Unit = { scope.launch { drawerState.close() } }
    BackHandler(enabled = drawerState.isOpen, onBack = close)
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight().width(232.dp),
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                drawerContainerColor = Color(0xFF151D18),
                drawerContentColor = Color(0xFFF0F5ED),
            ) {
                Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Previous numbers", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = close) { Icon(AppIcons.Back, "Close previous numbers") }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.padding(top = 8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).testTag("previous-numbers-list"),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    itemsIndexed(numbers, key = { _, number -> number }) { index, number ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp).semantics(mergeDescendants = true) {
                            contentDescription = "Call ${index + 1}, number $number"
                        }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Text("${index + 1}", color = Color(0xFF9CA99F), style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.widthIn(min = 24.dp))
                            Text(number.toString().padStart(2, '0'), style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    }
                }
            }
        },
        content = content,
    )
}
