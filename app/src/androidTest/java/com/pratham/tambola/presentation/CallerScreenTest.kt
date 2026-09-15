package com.pratham.tambola.presentation

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.pratham.tambola.data.ThemePreference
import com.pratham.tambola.domain.CallerState
import com.pratham.tambola.domain.PlaybackMode
import com.pratham.tambola.domain.Session
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

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

    @Test fun previousNumbersDrawerPreservesCallOrderAndScrollsWithoutChangingPlayback() {
        val order = listOf(34, 13, 7, 1) + (1..90).filter { it !in listOf(34, 13, 7, 1) }
        val state = CallerState(Session("test", 0, Long.MAX_VALUE, order, 50), PlaybackMode.CALLING)
        var playbackChanged = false
        var quit = false
        compose.setContent {
            TambolaTheme(ThemePreference.LIGHT) {
                CallerScreen(state, false, { quit = true }, { playbackChanged = true }, {}, {}, {})
            }
        }
        compose.onNodeWithContentDescription("Show previous numbers").performClick()
        val first = compose.onNodeWithContentDescription("Call 1, number 34").assertIsDisplayed().fetchSemanticsNode()
        val second = compose.onNodeWithContentDescription("Call 2, number 13").assertIsDisplayed().fetchSemanticsNode()
        assertTrue(first.boundsInRoot.top < second.boundsInRoot.top)
        compose.onNodeWithTag("previous-numbers-list").performScrollToIndex(48)
        compose.onNodeWithContentDescription("Call 49, number ${order[48]}").assertIsDisplayed()
        compose.onNodeWithContentDescription("Call 50, number ${order[49]}").assertDoesNotExist()
        compose.onNodeWithContentDescription("Close previous numbers").performClick()
        compose.onNodeWithContentDescription("Show previous numbers").assertIsDisplayed()
        assertFalse(playbackChanged)
        assertFalse(quit)
    }
}
