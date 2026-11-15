package dev.sezertanci.drawingworkshop.domain.model

/** Altı fırça boyutu, inceden kalına. Genişlik dp cinsindendir; her araç bu değeri kendi katsayısıyla ölçekler. */
enum class BrushSize(val widthDp: Float) {
    EXTRA_SMALL(3f),
    SMALL(6f),
    MEDIUM_SMALL(10f),
    MEDIUM(14f),
    LARGE(28f),
    EXTRA_LARGE(44f),
    ;

    companion object {
        /** Yeni çizimde seçili gelen boyut. */
        val DEFAULT = SMALL
    }
}
