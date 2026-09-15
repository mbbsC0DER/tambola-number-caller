package com.pratham.tambola.domain

import org.junit.Assert.*
import org.junit.Test

class NumberAnnouncementFormatterTest {
    @Test fun `live announcements split digits but readouts use whole words`() {
        assertEquals("one three, thirteen", NumberAnnouncementFormatter.live(13))
        assertEquals("one zero, ten", NumberAnnouncementFormatter.live(10))
        assertEquals("nine zero, ninety", NumberAnnouncementFormatter.live(90))
        assertEquals("seven", NumberAnnouncementFormatter.live(7))
        assertEquals("twenty-four", NumberAnnouncementFormatter.whole(24))
        assertEquals("thirteen", NumberAnnouncementFormatter.whole(13))
        (1..90).forEach { number ->
            assertTrue(NumberAnnouncementFormatter.whole(number).all { it.isLetter() || it == '-' })
            assertTrue(NumberAnnouncementFormatter.live(number).endsWith(NumberAnnouncementFormatter.whole(number)))
        }
    }
    @Test fun `speed has eleven half-second values`() {
        val valid = (0L..7_000L).filter(::validGap)
        assertEquals((1_000L..6_000L step 500L).toList(), valid)
        assertEquals(2_000L, DEFAULT_GAP_MS)
        assertEquals(2_000L, REPEAT_GAP_MS)
    }
}
