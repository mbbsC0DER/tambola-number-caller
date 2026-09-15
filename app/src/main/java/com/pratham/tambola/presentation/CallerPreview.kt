package com.pratham.tambola.presentation

import androidx.compose.runtime.Composable
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview
import com.pratham.tambola.data.ThemePreference
import com.pratham.tambola.domain.CallerState
import com.pratham.tambola.domain.PlaybackMode
import com.pratham.tambola.domain.Session

@Preview(name = "Caller · Light", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CallerLightPreview() = CallerPreview(ThemePreference.LIGHT)

@Preview(name = "Caller · Dark", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CallerDarkPreview() = CallerPreview(ThemePreference.DARK)

@Composable
private fun CallerPreview(theme: ThemePreference) {
    val order = listOf(34, 1) + (1..90).filter { it != 34 && it != 1 }
    TambolaTheme(theme) {
        Surface {
        CallerScreen(CallerState(Session("preview", 0, Long.MAX_VALUE, order, drawnCount = 2), PlaybackMode.PAUSED),
            false, {}, {}, {}, {}, {})
        }
    }
}
