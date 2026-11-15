package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalligraphyNibTest {
    private val nib = CalligraphyNib(WIDTH, ANGLE)

    @Test
    fun footprint_MovePerpendicularToNib_IsAsWideAsNib() {
        val area = abs(signedArea(nib.footprint(ORIGIN, StrokePoint(STEP, STEP))))

        assertEquals(WIDTH * STEP * SQRT_TWO, area, TOLERANCE)
    }

    @Test
    fun footprint_MoveParallelToNib_HasNoArea() {
        val area = signedArea(nib.footprint(ORIGIN, StrokePoint(STEP, -STEP)))

        assertEquals(0f, area, TOLERANCE)
    }

    @Test
    fun footprint_OppositeDirections_KeepSameTurnDirection() {
        val forward = signedArea(nib.footprint(ORIGIN, StrokePoint(STEP, STEP)))
        val backward = signedArea(nib.footprint(ORIGIN, StrokePoint(-STEP, -STEP)))

        assertTrue(forward > 0f && backward > 0f)
    }

    @Test
    fun footprint_HorizontalMove_CentersNibOnBothEnds() {
        val target = StrokePoint(STEP, 0f)

        val corners = nib.footprint(ORIGIN, target)

        assertTrue(near(midpoint(corners[0], corners[1]), ORIGIN))
        assertTrue(near(midpoint(corners[2], corners[3]), target))
    }

    /** Köşeleri sırayla izleyen çokgenin işaretli alanı (ayakkabı bağı formülü). */
    private fun signedArea(corners: List<StrokePoint>): Float =
        corners.indices.sumOf { index ->
            val current = corners[index]
            val next = corners[(index + 1) % corners.size]
            (current.x * next.y - next.x * current.y).toDouble()
        }.toFloat() / 2f

    private fun midpoint(a: StrokePoint, b: StrokePoint) = StrokePoint((a.x + b.x) / 2f, (a.y + b.y) / 2f)

    private fun near(a: StrokePoint, b: StrokePoint) = abs(a.x - b.x) < TOLERANCE && abs(a.y - b.y) < TOLERANCE

    private companion object {
        const val WIDTH = 10f
        const val ANGLE = 45f
        const val STEP = 20f
        const val SQRT_TWO = 1.4142135f
        const val TOLERANCE = 0.01f
        val ORIGIN = StrokePoint(0f, 0f)
    }
}
