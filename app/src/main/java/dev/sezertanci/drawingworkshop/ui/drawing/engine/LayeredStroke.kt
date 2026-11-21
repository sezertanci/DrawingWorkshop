package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Canvas
import android.graphics.Rect

/**
 * Aynı dokunuşları birlikte izleyen birkaç çizgiyi tek çizgi gibi yönetir. Katmanlar verildiği sırayla,
 * alttan üste doğru çizilir; etkilenen alan tüm katmanların alanlarının birleşimidir.
 */
class LayeredStroke(private val layers: List<ActiveStroke>) : ActiveStroke {
    override fun begin(x: Float, y: Float) = layers.forEach { it.begin(x, y) }

    override fun extend(x: Float, y: Float) = layers.forEach { it.extend(x, y) }

    override fun end() = layers.forEach { it.end() }

    override fun drawPreview(canvas: Canvas) = layers.forEach { it.drawPreview(canvas) }

    override fun commit(canvas: Canvas) = layers.forEach { it.commit(canvas) }

    override fun dirtyRegion(): Rect = Rect().apply { layers.forEach { union(it.dirtyRegion()) } }
}
