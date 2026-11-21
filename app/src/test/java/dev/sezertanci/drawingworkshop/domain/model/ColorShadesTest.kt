package dev.sezertanci.drawingworkshop.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorShadesTest {
    @Test
    fun rows_HuesAndGrays_HaveOneRowEach() {
        assertEquals(ColorShades.HUE_COUNT + 1, ColorShades.rows.size)
    }

    @Test
    fun rows_EveryRow_HasSameLength() {
        assertEquals(1, ColorShades.rows.map { it.size }.distinct().size)
    }

    @Test
    fun rows_GrayRow_GoesFromWhiteToBlack() {
        val grays = ColorShades.rows.last()

        assertEquals(WHITE, grays.first())
        assertEquals(BLACK, grays.last())
    }

    @Test
    fun rows_AllColors_AreDistinct() {
        val colors = ColorShades.rows.flatten()

        assertEquals(colors.size, colors.distinct().size)
    }

    @Test
    fun rows_HueRow_GetsDarkerTowardsEnd() {
        ColorShades.rows.dropLast(1).forEach { row ->
            assertTrue(HsvColor.fromArgb(row.first()).value >= HsvColor.fromArgb(row.last()).value)
        }
    }

    private companion object {
        const val WHITE = 0xFFFFFFFF.toInt()
        const val BLACK = 0xFF000000.toInt()
    }
}
