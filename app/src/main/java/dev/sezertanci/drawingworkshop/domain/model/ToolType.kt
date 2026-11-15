package dev.sezertanci.drawingworkshop.domain.model

/** Araç panelinde seçilebilen çizim araçları; sıra, paneldeki görünüş sırasıdır. */
enum class ToolType {
    BUCKET,
    PENCIL,
    BRUSH,
    MARKER,
    CALLIGRAPHY,
    CRAYON,
    SPRAY,
    SILK,
    ERASER,
    STICKER,
    ;

    /** Çizgi çizen araçlar fırça boyutunu kullanır; kova ve çıkartma kullanmaz. */
    val usesBrushSize: Boolean
        get() = this != BUCKET && this != STICKER

    /** Gökkuşağı rengiyle çizebilen araçlar; diğerleri son seçilen düz rengi kullanır. */
    val supportsRainbow: Boolean
        get() = this == PENCIL || this == BRUSH || this == MARKER ||
            this == CALLIGRAPHY || this == SPRAY
}
