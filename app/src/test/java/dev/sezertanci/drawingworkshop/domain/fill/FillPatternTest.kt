package dev.sezertanci.drawingworkshop.domain.fill

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FillPatternTest {
    @Test
    fun isInk_Solid_AlwaysTrue() {
        assertTrue(samples().all { (u, v) -> FillPattern.SOLID.isInk(u, v) })
    }

    @Test
    fun isInk_EveryPattern_HasBothInkAndBackground() {
        FillPattern.entries.filter { it != FillPattern.SOLID }.forEach { pattern ->
            val inks = samples().map { (u, v) -> pattern.isInk(u, v) }
            assertTrue("$pattern mürekkep içermeli", inks.any { it })
            assertTrue("$pattern zemin içermeli", inks.any { !it })
        }
    }

    @Test
    fun isInk_DotsCenter_IsInkAndCornerIsBackground() {
        assertTrue(FillPattern.DOTS.isInk(CENTER, CENTER))
        assertFalse(FillPattern.DOTS.isInk(0f, 0f))
    }

    @Test
    fun isInk_CheckersNeighbourQuarters_Differ() {
        assertNotEquals(
            FillPattern.CHECKERS.isInk(QUARTER, QUARTER),
            FillPattern.CHECKERS.isInk(THREE_QUARTERS, QUARTER),
        )
    }

    @Test
    fun isInk_StarAndHeartCenters_AreInk() {
        assertTrue(FillPattern.STARS.isInk(CENTER, CENTER))
        assertTrue(FillPattern.HEARTS.isInk(CENTER, CENTER))
    }

    /** Karonun içinden düzenli aralıklarla seçilen noktalar. */
    private fun samples() = (0 until STEPS).flatMap { i ->
        (0 until STEPS).map { j -> (i + CENTER) / STEPS to (j + CENTER) / STEPS }
    }

    private companion object {
        const val STEPS = 32
        const val CENTER = 0.5f
        const val QUARTER = 0.25f
        const val THREE_QUARTERS = 0.75f
    }
}
