package dev.sezertanci.drawingworkshop.ui.drawing.engine

import dev.sezertanci.drawingworkshop.domain.fill.FillPaint
import dev.sezertanci.drawingworkshop.domain.fill.FillPattern
import dev.sezertanci.drawingworkshop.domain.model.Palette
import javax.inject.Inject

/**
 * Boya kovasının boyasını hazırlar. Desenler seçili renk ve beyazla çizilir; karo boyutu [zoom] oranına
 * bölünür, böylece desen büyütülmüş görüntüde de ekranda aynı büyüklükte görünür.
 */
class FillPaintFactory @Inject constructor(private val density: ScreenDensity) {
    fun create(pattern: FillPattern, color: Int, zoom: Float): FillPaint =
        FillPaint.of(pattern, color, Palette.WHITE, density.toPx(PATTERN_TILE_DP) / zoom)

    private companion object {
        /** Desen karosunun ekrandaki kenar uzunluğu. */
        const val PATTERN_TILE_DP = 28f
    }
}
