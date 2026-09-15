package com.pratham.tambola.presentation

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pratham.tambola.data.ThemePreference
import com.pratham.tambola.domain.CallerState
import com.pratham.tambola.domain.PlaybackMode
import com.pratham.tambola.domain.Session
import org.junit.Rule
import org.junit.Test

class CallerScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun completedScreenKeepsQuitAndCalledReadoutAvailable() {
        val state = CallerState(Session("test", 0, Long.MAX_VALUE, (1..90).toList(), 90), PlaybackMode.ALL_CALLED)
        compose.setContent {
            TambolaTheme(ThemePreference.LIGHT) { CallerScreen(state, false, {}, {}, {}, {}, {}) }
        }
        compose.onNodeWithContentDescription("Play").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Quit session").assertExists()
        compose.onNodeWithContentDescription("Number readouts").performClick()
        compose.onNodeWithText("Read remaining numbers").assertIsNotEnabled()
        compose.onNodeWithText("Repeat called numbers").assertExists()
    }
}
