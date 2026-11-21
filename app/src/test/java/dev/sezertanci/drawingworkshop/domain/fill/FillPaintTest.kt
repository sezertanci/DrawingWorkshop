package dev.sezertanci.drawingworkshop.domain.fill

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FillPaintTest {
    @Test
    fun of_Solid_ReturnsSolidPaint() {
        assertTrue(FillPaint.of(FillPattern.SOLID, RED, WHITE, TILE) is FillPaint.Solid)
    }

    @Test
    fun solid_ColorCarryingSignature_IsPaintedWithoutSignature() {
        val paint = FillPaint.Solid(PatternMark.mark(RED))

        assertFalse(PatternMark.isMarked(paint.colorAt(0, 0)))
    }

    @Test
    fun solid_SameColorPixel_AlreadyCovers() {
        assertTrue(FillPaint.Solid(RED).alreadyCovers(RED))
    }

    @Test
    fun patterned_NeighbourTiles_RepeatSamePattern() {
        val paint = FillPaint.of(FillPattern.DOTS, RED, WHITE, TILE)
        val tile = TILE.toInt()

        for (y in 0 until tile) {
            for (x in 0 until tile) {
                assertEquals(paint.colorAt(x, y), paint.colorAt(x + tile, y + tile))
            }
        }
    }

    @Test
    fun patterned_AnyPixel_NeverAlreadyCovers() {
        val paint = FillPaint.of(FillPattern.STRIPES, RED, WHITE, TILE)

        assertFalse(paint.alreadyCovers(paint.colorAt(0, 0)))
    }

    @Test
    fun patterned_Pixels_UseMarkedInkAndBackground() {
        val paint = FillPaint.of(FillPattern.CHECKERS, RED, WHITE, TILE)
        val colors = (0 until TILE.toInt()).map { paint.colorAt(it, 0) }.toSet()

        assertEquals(setOf(PatternMark.mark(RED), PatternMark.mark(WHITE)), colors)
    }

    private companion object {
        const val RED = 0xFFFF0000.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
        const val TILE = 8f
    }
}
