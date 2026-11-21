package dev.sezertanci.drawingworkshop.domain.fill

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Boya kovasının boyama biçimleri; sıra, menüdeki görünüş sırasıdır. Düz boya dışındakiler, tuvali
 * kaplayan kare karolara çizilen iki renkli desenlerdir: [isInk], karo içindeki (u, v) noktasının
 * (ikisi de 0 ile 1 arası) seçili renkle mi, beyazla mı boyanacağını söyler.
 */
enum class FillPattern(private val ink: (u: Float, v: Float) -> Boolean) {
    SOLID({ _, _ -> true }),
    WAVES(PatternShapes::wave),
    CROSSHATCH(PatternShapes::crosshatch),
    CLOUDS(PatternShapes::cloud),
    STRIPES(PatternShapes::stripes),
    DOTS(PatternShapes::dot),
    CHECKERS(PatternShapes::checkers),
    STARS(PatternShapes::star),
    HEARTS(PatternShapes::heart),
    ;

    fun isInk(u: Float, v: Float): Boolean = ink(u, v)
}

/** Desenlerin bir karo içindeki biçimleri. */
internal object PatternShapes {
    private const val HALF = 0.5f
    private const val FULL_TURN = (2 * PI).toFloat()

    fun wave(u: Float, v: Float): Boolean =
        abs(frac(v + WAVE_AMPLITUDE * sin(FULL_TURN * u)) - HALF) < WAVE_HALF_WIDTH

    fun crosshatch(u: Float, v: Float): Boolean =
        frac(u + v) < HATCH_WIDTH || frac(u - v) < HATCH_WIDTH

    fun stripes(u: Float, v: Float): Boolean = frac(u + v) < STRIPE_WIDTH

    fun dot(u: Float, v: Float): Boolean = isInCircle(u, v, HALF, HALF, DOT_RADIUS)

    fun checkers(u: Float, v: Float): Boolean = (u < HALF) != (v < HALF)

    /** Üç yuvarlak tepe ve düz bir taban. */
    fun cloud(u: Float, v: Float): Boolean =
        isInCircle(u, v, CLOUD_SIDE_LEFT, CLOUD_SIDE_Y, CLOUD_SIDE_RADIUS) ||
            isInCircle(u, v, HALF, CLOUD_TOP_Y, CLOUD_TOP_RADIUS) ||
            isInCircle(u, v, 1f - CLOUD_SIDE_LEFT, CLOUD_SIDE_Y, CLOUD_SIDE_RADIUS) ||
            (u in CLOUD_SIDE_LEFT..1f - CLOUD_SIDE_LEFT && v in CLOUD_SIDE_Y..CLOUD_BOTTOM)

    /** Beş köşeli yıldız: köşeleri sırayla dış ve iç çemberde olan on köşeli çokgen. */
    fun star(u: Float, v: Float): Boolean {
        var inside = false
        var j = STAR_X.size - 1
        for (i in STAR_X.indices) {
            val crosses = (STAR_Y[i] > v) != (STAR_Y[j] > v) &&
                u < (STAR_X[j] - STAR_X[i]) * (v - STAR_Y[i]) / (STAR_Y[j] - STAR_Y[i]) + STAR_X[i]
            if (crosses) inside = !inside
            j = i
        }
        return inside
    }

    /** Kalp eğrisi: (x² + y² − 1)³ − x²y³ ≤ 0; y yukarı doğru artar. */
    fun heart(u: Float, v: Float): Boolean {
        val x = (u - HALF) * HEART_SCALE
        val y = (HEART_CENTER_V - v) * HEART_SCALE
        val ring = x * x + y * y - 1f
        return ring * ring * ring - x * x * y * y * y <= 0f
    }

    private fun isInCircle(u: Float, v: Float, cx: Float, cy: Float, radius: Float): Boolean {
        val dx = u - cx
        val dy = v - cy
        return dx * dx + dy * dy < radius * radius
    }

    private fun frac(value: Float): Float = value - floor(value)

    private const val WAVE_AMPLITUDE = 0.12f
    private const val WAVE_HALF_WIDTH = 0.12f
    private const val HATCH_WIDTH = 0.18f
    private const val STRIPE_WIDTH = 0.35f
    private const val DOT_RADIUS = 0.25f
    private const val CLOUD_SIDE_LEFT = 0.32f
    private const val CLOUD_SIDE_Y = 0.55f
    private const val CLOUD_SIDE_RADIUS = 0.17f
    private const val CLOUD_TOP_Y = 0.42f
    private const val CLOUD_TOP_RADIUS = 0.22f
    private const val CLOUD_BOTTOM = 0.72f
    private const val HEART_SCALE = 3.2f
    private const val HEART_CENTER_V = 0.55f
    private const val STAR_POINTS = 5
    private const val STAR_OUTER_RADIUS = 0.4f
    private const val STAR_INNER_RADIUS = 0.17f

    /** Yıldızın köşeleri: ilk köşe yukarıyı gösterir, dış ve iç köşeler sırayla gelir. */
    private val STAR_ANGLES = FloatArray(STAR_POINTS * 2) { -FULL_TURN / 4 + it * FULL_TURN / (STAR_POINTS * 2) }
    private val STAR_X = FloatArray(STAR_ANGLES.size) { HALF + starRadius(it) * cos(STAR_ANGLES[it]) }
    private val STAR_Y = FloatArray(STAR_ANGLES.size) { HALF + starRadius(it) * sin(STAR_ANGLES[it]) }

    private fun starRadius(index: Int): Float = if (index % 2 == 0) STAR_OUTER_RADIUS else STAR_INNER_RADIUS
}
