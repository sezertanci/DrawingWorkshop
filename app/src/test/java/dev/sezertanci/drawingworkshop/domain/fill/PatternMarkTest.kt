package dev.sezertanci.drawingworkshop.domain.fill

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternMarkTest {
    @Test
    fun mark_AnyOpaqueColor_IsMarked() {
        listOf(WHITE, RED, BLACK).forEach { assertTrue(PatternMark.isMarked(PatternMark.mark(it))) }
    }

    @Test
    fun mark_White_ShiftsEachChannelByAtMostThree() {
        val marked = PatternMark.mark(WHITE)

        listOf(RED_SHIFT, GREEN_SHIFT, 0).forEach { shift ->
            val difference = ((WHITE shr shift) and CHANNEL) - ((marked shr shift) and CHANNEL)
            assertTrue(difference in 0..MAX_SHIFT)
        }
    }

    @Test
    fun isMarked_TranslucentSignature_ReturnsFalse() {
        assertFalse(PatternMark.isMarked(PatternMark.mark(WHITE) and TRANSLUCENT_MASK))
    }

    @Test
    fun clear_MarkedColor_RemovesSignature() {
        assertFalse(PatternMark.isMarked(PatternMark.clear(PatternMark.mark(RED))))
    }

    @Test
    fun clear_PlainColor_ReturnsSameColor() {
        assertEquals(RED, PatternMark.clear(RED))
    }

    private companion object {
        const val WHITE = 0xFFFFFFFF.toInt()
        const val RED = 0xFFFF0000.toInt()
        const val BLACK = 0xFF000000.toInt()
        const val CHANNEL = 0xFF
        const val RED_SHIFT = 16
        const val GREEN_SHIFT = 8
        const val MAX_SHIFT = 3
        const val TRANSLUCENT_MASK = 0x80FFFFFF.toInt()
    }
}
