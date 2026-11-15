package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import dev.sezertanci.drawingworkshop.domain.stroke.CalligraphyNib
import dev.sezertanci.drawingworkshop.domain.stroke.QuadSegment
import dev.sezertanci.drawingworkshop.domain.stroke.RainbowHues
import dev.sezertanci.drawingworkshop.domain.stroke.StrokePoint
import dev.sezertanci.drawingworkshop.domain.stroke.StrokeSmoother
import dev.sezertanci.drawingworkshop.domain.stroke.divide
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Mürekkep kalemine benzeyen kaligrafi çizgisi. Yumuşatılmış yol kısa düz parçalara bölünür ve her
 * parçada yassı ucun taradığı paralelkenar ([CalligraphyNib.footprint]) dolgu olarak eklenir. Böylece
 * çizgi, yönüne göre kendiliğinden kalınlaşıp incelir.
 *
 * Boya, en ince yerde de çizginin kaybolmaması için ince bir kenar çizgisiyle birlikte doldurulur.
 * Gökkuşağı seçiliyse ([hues] verilmişse) çizgi [sectionLength] uzunluğundaki bölümlere ayrılır ve her
 * bölüm, çizgi boyunca değişen renkten kendi düz rengini alır.
 */
class CalligraphyStroke(
    private val paint: Paint,
    private val nib: CalligraphyNib,
    nibWidth: Float,
    minPointDistance: Float,
    private val pieceLength: Float,
    private val sectionLength: Float,
    private val hues: RainbowHues?,
) : ActiveStroke {
    private val sections = ArrayList<Section>()
    private val smoother = StrokeSmoother(minPointDistance)
    private val reach = nibWidth + paint.strokeWidth
    private val bounds = RectF()
    private var lastEnd = StrokePoint(0f, 0f)
    private var distance = 0f
    private var sectionStartDistance = 0f

    override fun begin(x: Float, y: Float) {
        lastEnd = StrokePoint(x, y)
        bounds.set(x, y, x, y)
        sections.add(Section(colorAt(0f)))
        smoother.start(lastEnd)
        // Dokunup hemen kaldırınca da ucun izi görünsün diye başlangıçta sıfır uzunlukta bir parça eklenir.
        sections.last().add(nib.footprint(lastEnd, lastEnd))
    }

    override fun extend(x: Float, y: Float) {
        smoother.add(StrokePoint(x, y))?.let(::append)
    }

    override fun end() {
        smoother.finish()?.let(::append)
    }

    override fun drawPreview(canvas: Canvas) = drawSections(canvas)

    override fun commit(canvas: Canvas) = drawSections(canvas)

    override fun dirtyRegion(): Rect = RectF(bounds).toDirtyRegion(reach)

    private fun append(segment: QuadSegment) {
        segment.divide(lastEnd, pieceLength).forEach(::appendPiece)
    }

    private fun appendPiece(point: StrokePoint) {
        distance += hypot(point.x - lastEnd.x, point.y - lastEnd.y)
        if (hues != null && distance - sectionStartDistance >= sectionLength) {
            sectionStartDistance = distance
            sections.add(Section(colorAt(distance)))
        }
        sections.last().add(nib.footprint(lastEnd, point))
        bounds.set(
            min(bounds.left, point.x),
            min(bounds.top, point.y),
            max(bounds.right, point.x),
            max(bounds.bottom, point.y),
        )
        lastEnd = point
    }

    private fun colorAt(distance: Float): Int = hues?.colorAt(distance) ?: paint.color

    private fun drawSections(canvas: Canvas) {
        for (section in sections) {
            paint.color = section.color
            canvas.drawPath(section.path, paint)
        }
    }

    /** Tek renkle boyanan, uç izlerinden oluşan bölüm. */
    private class Section(val color: Int) {
        val path = Path()

        fun add(corners: List<StrokePoint>) {
            path.moveTo(corners.first().x, corners.first().y)
            for (index in 1 until corners.size) path.lineTo(corners[index].x, corners[index].y)
            path.close()
        }
    }
}
