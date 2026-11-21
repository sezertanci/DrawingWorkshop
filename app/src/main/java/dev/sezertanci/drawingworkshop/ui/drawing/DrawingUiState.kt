package dev.sezertanci.drawingworkshop.ui.drawing

import dev.sezertanci.drawingworkshop.domain.fill.FillPattern
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.Palette
import dev.sezertanci.drawingworkshop.domain.model.SilkStyle
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.domain.sticker.StickerCatalog
import dev.sezertanci.drawingworkshop.domain.symmetry.SymmetryMode

/** Renk panelinde seçilen rengin nereye uygulanacağı. */
enum class ColorTarget {
    STROKE,
    BACKGROUND,
}

/** Geniş renk panelinin durumu: kapalı, açık veya tuvalden renk almak için damlalık bekliyor. */
enum class ColorPickerMode {
    CLOSED,
    PANEL,
    EYEDROPPER,
}

data class DrawingUiState(
    val tool: ToolType = ToolType.BRUSH,
    val brushSize: BrushSize = BrushSize.DEFAULT,
    val silkStyle: SilkStyle = SilkStyle(),

    /** Boya kovasının düz mü, hangi desenle mi boyayacağı. */
    val fillPattern: FillPattern = FillPattern.SOLID,
    val strokeColor: Int = Palette.RED,

    /** Kalem için gökkuşağı seçildi mi; desteklemeyen araçlar bu sırada [strokeColor] kullanır. */
    val rainbow: Boolean = false,
    val backgroundColor: Int = Palette.WHITE,
    val colorTarget: ColorTarget = ColorTarget.STROKE,
    val colorPicker: ColorPickerMode = ColorPickerMode.CLOSED,
    val sticker: String = StickerCatalog.default,
    val symmetry: SymmetryMode = SymmetryMode.OFF,

    /** Simetri açıkken boya kovası her kopya noktasını da doldurur. */
    val symmetricFill: Boolean = true,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isLoaded: Boolean = false,
    val exportStatus: ExportStatus = ExportStatus.IDLE,
) {
    /** Renk panelinde işaretli görünecek renk. */
    val selectedColor: Int get() = if (colorTarget == ColorTarget.STROKE) strokeColor else backgroundColor

    /** Rengi seçili hedefe (kalem veya arka plan) uygular; kalem için gökkuşağı bırakılır. */
    fun withColor(color: Int): DrawingUiState = when (colorTarget) {
        ColorTarget.STROKE -> copy(strokeColor = color, rainbow = false)
        ColorTarget.BACKGROUND -> copy(backgroundColor = color)
    }

    /** Seçili araç şu an gökkuşağıyla çiziyor mu. */
    val drawsRainbow: Boolean get() = rainbow && tool.supportsRainbow

    /** Gökkuşağı düğmesi yalnızca kalem rengi seçilirken ve araç destekliyorsa kullanılabilir. */
    val isRainbowAvailable: Boolean get() = colorTarget == ColorTarget.STROKE && tool.supportsRainbow

    /** Gökkuşağı düğmesi işaretli mi; işaretliyse düz renklerden hiçbiri işaretli görünmez. */
    val isRainbowSelected: Boolean get() = isRainbowAvailable && rainbow
}
