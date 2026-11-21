package dev.sezertanci.drawingworkshop.ui.drawing.components

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.fill.FillPaint
import dev.sezertanci.drawingworkshop.domain.fill.FillPattern
import dev.sezertanci.drawingworkshop.domain.model.Palette
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

private val SwatchSize = 40.dp
private val SwatchTile = 20.dp
private val SwatchShape = RoundedCornerShape(8.dp)
private val SwatchOutline = 1.dp

/** Boya kovasının seçili deseni ve desen değiştiğinde çağrılacak işlem. */
class FillPatternControls(
    val pattern: FillPattern,
    val onPatternSelected: (FillPattern) -> Unit,
)

/**
 * Boya kovası düğmesi: dokununca kova seçilir ve yanında desen kutusu açılır. Kutuda düz boya ve
 * desenler, seçili renk ve beyazla boyanmış örnek karelerle görünür; seçim yapılınca kutu kapanır.
 */
@Composable
internal fun BucketToolButton(selected: Boolean, color: Int, controls: FillPatternControls, onToolSelected: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        ToolButton(ToolType.BUCKET, selected) {
            onToolSelected()
            expanded = true
        }
        ToolMenu(expanded = expanded, onDismiss = { expanded = false }) {
            ChoiceGrid(
                options = FillPattern.entries,
                columns = MENU_COLUMNS,
                selected = controls.pattern,
                labelOf = { it.labelRes },
                onSelected = {
                    expanded = false
                    controls.onPatternSelected(it)
                },
            ) { PatternSwatch(it, color) }
        }
    }
}

/** Desenin, kovanın boyayacağı biçimde çizilmiş küçük örneği. */
@Composable
private fun PatternSwatch(pattern: FillPattern, color: Int) {
    val density = LocalDensity.current
    val sizePx = with(density) { SwatchSize.roundToPx() }
    val tilePx = with(density) { SwatchTile.toPx() }
    val image = remember(pattern, color, sizePx, tilePx) { swatchImage(pattern, color, sizePx, tilePx) }
    Image(
        bitmap = image,
        contentDescription = null,
        modifier = Modifier
            .size(SwatchSize)
            .clip(SwatchShape)
            .border(SwatchOutline, WorkshopColors.Outline, SwatchShape),
    )
}

private fun swatchImage(pattern: FillPattern, color: Int, sizePx: Int, tilePx: Float): ImageBitmap {
    val paint = FillPaint.of(pattern, color, Palette.WHITE, tilePx)
    val pixels = IntArray(sizePx * sizePx) { paint.colorAt(it % sizePx, it / sizePx) }
    return Bitmap.createBitmap(pixels, sizePx, sizePx, Bitmap.Config.ARGB_8888).asImageBitmap()
}

@get:StringRes
private val FillPattern.labelRes: Int
    get() = when (this) {
        FillPattern.SOLID -> R.string.fill_solid
        FillPattern.WAVES -> R.string.fill_waves
        FillPattern.CROSSHATCH -> R.string.fill_crosshatch
        FillPattern.CLOUDS -> R.string.fill_clouds
        FillPattern.STRIPES -> R.string.fill_stripes
        FillPattern.DOTS -> R.string.fill_dots
        FillPattern.CHECKERS -> R.string.fill_checkers
        FillPattern.STARS -> R.string.fill_stars
        FillPattern.HEARTS -> R.string.fill_hearts
    }
