package dev.sezertanci.drawingworkshop.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ArgbTest {
    @Test
    fun sourceOver_OpaqueTop_ReturnsTop() {
        assertEquals(RED, Argb.sourceOver(RED, WHITE))
    }

    @Test
    fun sourceOver_TransparentTop_ReturnsBottom() {
        assertEquals(WHITE, Argb.sourceOver(TRANSPARENT, WHITE))
    }

    @Test
    fun sourceOver_HalfTransparentBlackOnWhite_ReturnsGray() {
        assertEquals(GRAY, Argb.sourceOver(HALF_BLACK, WHITE))
    }

    private companion object {
        const val RED = 0xFFFF0000.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
        const val TRANSPARENT = 0x00000000
        const val HALF_BLACK = 0x80000000.toInt()
        const val GRAY = 0xFF7F7F7F.toInt()
    }
}
