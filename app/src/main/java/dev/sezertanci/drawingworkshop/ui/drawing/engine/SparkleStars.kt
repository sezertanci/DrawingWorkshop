package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import dev.sezertanci.drawingworkshop.domain.stroke.FloatList
import dev.sezertanci.drawingworkshop.domain.stroke.SprayScatter
import dev.sezertanci.drawingworkshop.domain.stroke.StrokePoint
import kotlin.math.hypot

/**
 * Parmağın yolu boyunca, her [spacing] uzunlukta bir küçük dört köşeli yıldız ışıltısı bırakır. Yıldızlar
 * [radius] yarıçaplı daire içinde rastgele bir yere konur; büyük ve küçük yıldızlar sırayla gelir.
 */
class SparkleStars(
    private val paint: Paint,
    private val radius: Float,
    private val spacing: Float,
    private val starSize: Float,
    private val scatter: SprayScatter,
) : ActiveStroke {
    private val path = Path()
    private val spot = FloatList()
    private var starCount = 0
    private var distance = 0f
    private var nextStarAt = 0f
    private var lastX = 0f
    private var lastY = 0f

    override fun begin(x: Float, y: Float) {
        lastX = x
        lastY = y
        nextStarAt = spacing * FIRST_STAR_SHARE
    }

    override fun extend(x: Float, y: Float) {
        distance += hypot(x - lastX, y - lastY)
        lastX = x
        lastY = y
        if (distance < nextStarAt) return
        addStar(x, y)
        nextStarAt = distance + spacing
    }

    override fun end() = Unit

    override fun drawPreview(canvas: Canvas) = canvas.drawPath(path, paint)

    override fun commit(canvas: Canvas) = canvas.drawPath(path, paint)

    override fun dirtyRegion(): Rect {
        if (starCount == 0) return Rect()
        val bounds = RectF()
        path.computeBounds(bounds, true)
        return bounds.toDirtyRegion(0f)
    }

    private fun addStar(x: Float, y: Float) {
        spot.clear()
        scatter.scatter(StrokePoint(x, y), radius, 1, spot)
        val size = if (starCount % 2 == 0) starSize else starSize * SMALL_STAR_SHARE
        addStarPath(spot.values[0], spot.values[1], size)
        starCount++
    }

    /** Uçları yukarı, sağa, aşağı ve sola bakan, beli ince dört köşeli yıldız. */
    private fun addStarPath(cx: Float, cy: Float, size: Float) {
        val waist = size * WAIST_SHARE
        path.moveTo(cx, cy - size)
        path.lineTo(cx + waist, cy - waist)
        path.lineTo(cx + size, cy)
        path.lineTo(cx + waist, cy + waist)
        path.lineTo(cx, cy + size)
        path.lineTo(cx - waist, cy + waist)
        path.lineTo(cx - size, cy)
        path.lineTo(cx - waist, cy - waist)
        path.close()
    }

    private companion object {
        /** İlk yıldız, aralığın bu kadarı yol alınınca konur. */
        const val FIRST_STAR_SHARE = 0.5f

        /** Küçük yıldızların büyük yıldıza oranı. */
        const val SMALL_STAR_SHARE = 0.6f

        /** Yıldızın belinin, ucuna olan uzaklığa oranı. */
        const val WAIST_SHARE = 0.22f
    }
}
