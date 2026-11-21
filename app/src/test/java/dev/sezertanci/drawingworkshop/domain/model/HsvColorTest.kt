package dev.sezertanci.drawingworkshop.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HsvColorTest {
    @Test
    fun toArgb_PrimaryHues_ReturnPrimaryColors() {
        assertEquals(RED, HsvColor(0f, 1f, 1f).toArgb())
        assertEquals(GREEN, HsvColor(120f, 1f, 1f).toArgb())
        assertEquals(BLUE, HsvColor(240f, 1f, 1f).toArgb())
    }

    @Test
    fun toArgb_FullTurn_EqualsZeroHue() {
        assertEquals(HsvColor(0f, 1f, 1f).toArgb(), HsvColor(360f, 1f, 1f).toArgb())
    }

    @Test
    fun toArgb_NoSaturationOrValue_ReturnsWhiteOrBlack() {
        assertEquals(WHITE, HsvColor(200f, 0f, 1f).toArgb())
        assertEquals(BLACK, HsvColor(200f, 1f, 0f).toArgb())
    }

    @Test
    fun fromArgb_Yellow_ReturnsHueSixty() {
        assertEquals(HsvColor(60f, 1f, 1f), HsvColor.fromArgb(YELLOW))
    }

    @Test
    fun fromArgb_Gray_HasNoHueOrSaturation() {
        val gray = HsvColor.fromArgb(GRAY)

        assertEquals(0f, gray.saturation, DELTA)
        assertEquals(0f, gray.hue, DELTA)
    }

    @Test
    fun fromArgb_ThenToArgb_ReturnsSameColor() {
        Palette.colors.forEach { assertEquals(it, HsvColor.fromArgb(it).toArgb()) }
    }

    private companion object {
        const val RED = 0xFFFF0000.toInt()
        const val GREEN = 0xFF00FF00.toInt()
        const val BLUE = 0xFF0000FF.toInt()
        const val YELLOW = 0xFFFFFF00.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
        const val BLACK = 0xFF000000.toInt()
        const val GRAY = 0xFF808080.toInt()
        const val DELTA = 0.0001f
    }
}
