package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.Palette
import dev.sezertanci.drawingworkshop.ui.components.EmojiIcon
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.drawing.ColorTarget
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

private val SwatchSize = 52.dp

/**
 * Sağdaki kaydırılabilir renk paneli. Üstteki iki düğme, seçilen rengin kaleme mi yoksa tuvalin
 * arka planına mı uygulanacağını belirler. Listenin başındaki gökkuşağı, onu destekleyen araçlarla
 * kalemde seçilebilir; seçilemediği yerde soluk görünür. Renklerin başındaki düğme geniş renk panelini
 * açar; oradan seçilen, listede olmayan bir renk bu düğmenin içinde görünür.
 */
@Composable
fun ColorPanel(
    colorTarget: ColorTarget,
    selectedColor: Int,
    rainbow: RainbowChoice,
    onColorTargetSelected: (ColorTarget) -> Unit,
    onColorSelected: (Int) -> Unit,
    onMoreColors: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val customSelected = !rainbow.selected && selectedColor !in Palette.colors
    PanelColumn(width = RightPanelWidth, modifier = modifier) {
        item {
            PanelButton(
                selected = colorTarget == ColorTarget.STROKE,
                description = stringResource(R.string.color_target_stroke),
                onClick = { onColorTargetSelected(ColorTarget.STROKE) },
            ) { EmojiIcon(STROKE_TARGET_EMOJI) }
        }
        item {
            PanelButton(
                selected = colorTarget == ColorTarget.BACKGROUND,
                description = stringResource(R.string.color_target_background),
                onClick = { onColorTargetSelected(ColorTarget.BACKGROUND) },
            ) { EmojiIcon(BACKGROUND_TARGET_EMOJI) }
        }
        item { PanelDivider() }
        item {
            PanelButton(
                selected = customSelected,
                description = stringResource(R.string.color_more),
                onClick = onMoreColors,
                size = SwatchSize,
                shape = CircleShape,
                background = SolidColor(if (customSelected) Color(selectedColor) else WorkshopColors.ItemBackground),
            ) { if (!customSelected) EmojiIcon(MORE_COLORS_EMOJI) }
        }
        item {
            PanelButton(
                selected = rainbow.selected,
                enabled = rainbow.available,
                description = stringResource(R.string.color_rainbow),
                onClick = rainbow.onSelected,
                size = SwatchSize,
                shape = CircleShape,
                background = RainbowBrush,
            ) {}
        }
        items(Palette.colors) { color ->
            PanelButton(
                selected = !rainbow.selected && color == selectedColor,
                description = stringResource(R.string.color_swatch),
                onClick = { onColorSelected(color) },
                size = SwatchSize,
                shape = CircleShape,
                background = SolidColor(Color(color)),
            ) {}
        }
    }
}

/** Renk panelindeki gökkuşağının durumu ve seçilince yapılacak iş. */
class RainbowChoice(
    val selected: Boolean,
    val available: Boolean,
    val onSelected: () -> Unit,
)

private const val STROKE_TARGET_EMOJI = "🖌️"
private const val BACKGROUND_TARGET_EMOJI = "🖼️"
private const val MORE_COLORS_EMOJI = "🎨"
