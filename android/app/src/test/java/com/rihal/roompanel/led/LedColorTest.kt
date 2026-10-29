package com.rihal.roompanel.led

import com.rihal.roompanel.domain.Meeting
import com.rihal.roompanel.domain.RoomStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LedColorTest {

    private val now = Instant.parse("2026-10-07T09:00:00Z")
    private val meeting = Meeting("a", "Standup", "Org", now, now.plusSeconds(1800))

    @Test
    fun `green when free, yellow when starting soon, red when in use`() {
        assertEquals(LedColor.FREE, LedColor.of(RoomStatus.Free(null)))
        assertEquals(LedColor.STARTING_SOON, LedColor.of(RoomStatus.StartingSoon(meeting)))
        assertEquals(LedColor.BUSY, LedColor.of(RoomStatus.Busy(meeting, null)))
    }

    @Test
    fun `every colour is a six-digit hex the light bar accepts`() {
        LedColor.entries.forEach { assertTrue(it.hex, Regex("^#[0-9A-Fa-f]{6}$").matches(it.hex)) }
    }
}
