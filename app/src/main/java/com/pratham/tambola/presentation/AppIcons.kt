package com.pratham.tambola.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Small native vectors keep the app independent of an entire icon font/library. */
object AppIcons {
    private fun outline(name: String, autoMirror: Boolean = false, draw: PathBuilder.() -> Unit) =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathBuilder = draw)
        }.build()
    private fun filled(name: String, draw: PathBuilder.() -> Unit) =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.Black), pathBuilder = draw)
        }.build()
    val Play = filled("Play") { moveTo(8f, 5f); lineTo(19f, 12f); lineTo(8f, 19f); close() }
    val Pause = filled("Pause") {
        moveTo(6f, 5f); lineTo(10f, 5f); lineTo(10f, 19f); lineTo(6f, 19f); close()
        moveTo(14f, 5f); lineTo(18f, 5f); lineTo(18f, 19f); lineTo(14f, 19f); close()
    }
    val Stop = filled("Stop") { moveTo(6f, 6f); lineTo(18f, 6f); lineTo(18f, 18f); lineTo(6f, 18f); close() }
    val Back = outline("Back", true) { moveTo(19f, 12f); lineTo(5f, 12f); moveTo(11f, 6f); lineTo(5f, 12f); lineTo(11f, 18f) }
    val Chevron = outline("Chevron", true) { moveTo(9f, 6f); lineTo(15f, 12f); lineTo(9f, 18f) }
    val Volume = outline("Volume", true) {
        moveTo(3f, 9f); lineTo(7f, 9f); lineTo(12f, 5f); lineTo(12f, 19f); lineTo(7f, 15f); lineTo(3f, 15f); close()
        moveTo(16f, 8f); curveTo(19f, 10f, 19f, 14f, 16f, 16f)
        moveTo(19f, 5f); curveTo(24f, 9f, 24f, 15f, 19f, 19f)
    }
    val History = outline("History") {
        moveTo(4f, 8f); curveTo(8f, 0f, 21f, 3f, 21f, 12f); curveTo(21f, 22f, 7f, 25f, 3f, 16f)
        moveTo(3f, 3f); lineTo(3f, 9f); lineTo(9f, 9f)
        moveTo(12f, 7f); lineTo(12f, 12f); lineTo(16f, 14f)
    }
    val Speed = outline("Speed") {
        moveTo(4f, 19f); curveTo(-3f, 9f, 8f, -1f, 17f, 5f)
        moveTo(20f, 9f); curveTo(22f, 12f, 22f, 16f, 20f, 19f); lineTo(4f, 19f)
        moveTo(11f, 14f); lineTo(18f, 7f)
    }
    val Settings = outline("Settings") {
        moveTo(4f, 7f); lineTo(20f, 7f); moveTo(4f, 17f); lineTo(20f, 17f)
        moveTo(8f, 4f); lineTo(8f, 10f); moveTo(16f, 14f); lineTo(16f, 20f)
    }
}
