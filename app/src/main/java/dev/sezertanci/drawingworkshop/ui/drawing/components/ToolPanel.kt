package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.ui.components.ConfirmDialog
import dev.sezertanci.drawingworkshop.ui.components.EmojiIcon
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.drawing.ExportStatus

private val PanelWidth = 88.dp
private val PanelDividerWidth = 56.dp
private val PanelDividerThickness = 2.dp

/** Boyut düğmesi, çizim araçlarının hemen altında; kova en üstte, çıkartma ayrı grupta durur. */
private val DrawingTools = ToolType.entries.filter { it != ToolType.STICKER }

/** Panelin araç dışındaki düğmeleri: galeriye dönme, geri al / yinele, temizleme ve galeriye aktarma. */
class ToolPanelActions(
    val canUndo: Boolean,
    val canRedo: Boolean,
    val exportStatus: ExportStatus,
    val onHome: () -> Unit,
    val onUndo: () -> Unit,
    val onRedo: () -> Unit,
    val onClear: () -> Unit,
    val onExport: () -> Unit,
)

/** Soldaki kaydırılabilir araç paneli: galeri, çizim araçları (kova desenleri ve ipek stilleriyle), fırça boyutu menüsü, kova ve çıkartma, simetri, geri al, yinele, temizle ve aktarma. */
@Composable
fun ToolPanel(
    selectedTool: ToolType,
    selectedSize: BrushSize,
    strokeColor: Color,
    drawsRainbow: Boolean,
    actions: ToolPanelActions,
    symmetry: SymmetryControls,
    silk: SilkStyleControls,
    fill: FillPatternControls,
    onToolSelected: (ToolType) -> Unit,
    onSizeSelected: (BrushSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmingClear by rememberSaveable { mutableStateOf(false) }
    // Gökkuşağıyla çizilirken boyut noktaları da gökkuşağı renkleriyle görünür.
    val dotBrush = if (drawsRainbow) RainbowBrush else SolidColor(strokeColor)
    PanelColumn(width = PanelWidth, modifier = modifier) {
        item { ActionButton(HOME_EMOJI, R.string.action_home, onClick = actions.onHome) }
        item { PanelDivider() }
        items(DrawingTools) { tool ->
            val selected = tool == selectedTool
            when (tool) {
                ToolType.SILK -> SilkToolButton(selected, silk) { onToolSelected(tool) }
                ToolType.BUCKET -> BucketToolButton(selected, strokeColor.toArgb(), fill) { onToolSelected(tool) }
                else -> ToolButton(tool, selected) { onToolSelected(tool) }
            }
        }
        item { PanelDivider() }
        item { SizeMenuButton(selectedSize, dotBrush, onSizeSelected) }
        item { PanelDivider() }
        item {
            val sticker = ToolType.STICKER
            ToolButton(sticker, sticker == selectedTool) { onToolSelected(sticker) }
        }
        item { PanelDivider() }
        symmetryItems(symmetry)
        item { PanelDivider() }
        item { ActionButton(UNDO_EMOJI, R.string.action_undo, actions.canUndo, actions.onUndo) }
        item { ActionButton(REDO_EMOJI, R.string.action_redo, actions.canRedo, actions.onRedo) }
        item { ActionButton(CLEAR_EMOJI, R.string.action_clear) { confirmingClear = true } }
        item { PanelDivider() }
        item {
            val running = actions.exportStatus == ExportStatus.RUNNING
            ActionButton(actions.exportStatus.emoji, R.string.action_export, !running, actions.onExport)
        }
    }
    if (confirmingClear) {
        ConfirmDialog(
            emoji = CLEAR_EMOJI,
            title = stringResource(R.string.clear_confirm_title),
            onConfirm = {
                confirmingClear = false
                actions.onClear()
            },
            onDismiss = { confirmingClear = false },
        )
    }
}

@Composable
internal fun ToolButton(tool: ToolType, selected: Boolean, onClick: () -> Unit) {
    PanelButton(
        selected = selected,
        description = stringResource(tool.labelRes),
        onClick = onClick,
    ) { ToolIconView(tool.icon) }
}

@Composable
private fun ActionButton(emoji: String, descriptionRes: Int, enabled: Boolean = true, onClick: () -> Unit) {
    PanelButton(
        selected = false,
        enabled = enabled,
        description = stringResource(descriptionRes),
        onClick = onClick,
    ) { EmojiIcon(emoji) }
}

@Composable
internal fun PanelDivider() {
    HorizontalDivider(modifier = Modifier.width(PanelDividerWidth), thickness = PanelDividerThickness)
}

private val ExportStatus.emoji: String
    get() = when (this) {
        ExportStatus.IDLE -> "📤"
        ExportStatus.RUNNING -> "⏳"
        ExportStatus.DONE -> "✅"
        ExportStatus.FAILED -> "⚠️"
    }

private const val HOME_EMOJI = "🏠"
private const val UNDO_EMOJI = "↩️"
private const val REDO_EMOJI = "↪️"
private const val CLEAR_EMOJI = "🧹"
