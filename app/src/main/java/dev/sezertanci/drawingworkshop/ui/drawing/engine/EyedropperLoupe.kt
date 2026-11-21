package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sezertanci.drawingworkshop.R
import javax.inject.Inject

/** Damlalığın tuvalde dokunduğu nokta ve oradaki renk. */
data class ColorProbe(val x: Float, val y: Float, val color: Int)

/**
 * Damlalık kullanılırken parmağın üstünde gösterilen büyüteç: içi parmağın altındaki renkle dolu bir
 * daire ve ucu dokunulan noktada duran damlalık simgesi. Daire üstte sığmazsa parmağın altına çizilir.
 * Boyutlar büyütme oranına bölünür; büyüteç ekranda hep aynı boyda görünür.
 */
class EyedropperLoupe @Inject constructor(
    @ApplicationContext context: Context,
    private val density: ScreenDensity,
) {
    private val icon = checkNotNull(ContextCompat.getDrawable(context, R.drawable.ic_eyedropper)).mutate()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
    }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = OUTLINE_COLOR
    }

    fun draw(canvas: Canvas, probe: ColorProbe, zoom: Float) {
        val radius = density.toPx(RADIUS_DP) / zoom
        val offset = density.toPx(OFFSET_DP) / zoom
        val ring = density.toPx(RING_DP) / zoom
        val above = probe.y - offset
        val centerY = if (above - radius - ring >= 0f) above else probe.y + offset
        fillPaint.color = probe.color
        ringPaint.strokeWidth = ring
        outlinePaint.strokeWidth = ring / 2f
        canvas.drawCircle(probe.x, centerY, radius, fillPaint)
        canvas.drawCircle(probe.x, centerY, radius, ringPaint)
        canvas.drawCircle(probe.x, centerY, radius + ring, outlinePaint)
        drawIcon(canvas, probe, density.toPx(ICON_DP) / zoom)
    }

    /** Simgenin ucu (sol alt köşesi) dokunulan noktaya gelecek şekilde çizilir. */
    private fun drawIcon(canvas: Canvas, probe: ColorProbe, size: Float) {
        val left = probe.x - size * ICON_TIP_X
        val top = probe.y - size * ICON_TIP_Y
        canvas.save()
        canvas.translate(left, top)
        canvas.scale(size / icon.intrinsicWidth, size / icon.intrinsicHeight)
        icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
        icon.draw(canvas)
        canvas.restore()
    }

    private companion object {
        const val RADIUS_DP = 36f
        const val OFFSET_DP = 96f
        const val RING_DP = 4f
        const val ICON_DP = 40f

        /** Simgenin ucunun, simge boyutuna oranla konumu (24'lük çizimde 3, 21 noktası). */
        const val ICON_TIP_X = 3f / 24f
        const val ICON_TIP_Y = 21f / 24f

        const val OUTLINE_COLOR = 0xFF3C3F44.toInt()
    }
}
