package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.ColorShades
import dev.sezertanci.drawingworkshop.domain.model.HsvColor
import dev.sezertanci.drawingworkshop.domain.model.Palette
import dev.sezertanci.drawingworkshop.domain.stroke.RainbowHues.Companion.FULL_TURN_DEGREES
import dev.sezertanci.drawingworkshop.ui.components.DrawableIcon
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.components.clickableWithSound
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private val PanelShape = RoundedCornerShape(20.dp)
private val PanelPadding = 12.dp
private val SectionSpacing = 16.dp
private val CellSpacing = 4.dp
private val MaxCellSize = 32.dp
private val CellShape = RoundedCornerShape(6.dp)
private val SelectedCellBorder = 3.dp
private val CellBorder = 1.dp
private val WheelSize = 220.dp
private val SliderHeight = 40.dp
private val MarkerRadius = 10.dp
private val MarkerOutline = 2.dp
private val MarkerRing = 3.dp
private val ScrimColor = Color.Black.copy(alpha = 0.3f)

/** Renk tekerleğindeki ton geçişinin durak sayısı; durak arttıkça geçiş daha düzgün görünür. */
private const val WHEEL_HUE_STOPS = 12

/**
 * Sağdan, tuvalin üstüne kayarak açılan geniş renk paneli. Solda tonlu renk ızgarası, sağda renk
 * tekerleği, altında açıklık çubuğu ve tuvalden renk almaya yarayan damlalık düğmesi bulunur. Seçim yapılınca panel açık kalır; böylece renk
 * denenerek ayarlanabilir. Panel yalnızca dışına dokununca kapanır.
 *
 * @param selectedColor Seçili renk; gökkuşağı seçiliyse `null` olur ve tekerlek kırmızıyla açılır.
 */
@Composable
fun BoxScope.ColorPickerPanel(
    visible: Boolean,
    selectedColor: Int?,
    onColorSelected: (Int) -> Unit,
    onEyedropper: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(ScrimColor)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown().consume()
                        onDismiss()
                    }
                },
        )
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally { it },
        exit = slideOutHorizontally { it },
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = RightPanelWidth, top = PanelPadding, bottom = PanelPadding),
    ) {
        // Durum panel her açılışta yeniden kurulur; tekerlek o anki rengi göstererek açılır.
        var hsv by remember { mutableStateOf(HsvColor.fromArgb(selectedColor ?: Palette.RED)) }
        val pick: (HsvColor) -> Unit = {
            hsv = it
            onColorSelected(it.toArgb())
        }
        Surface(shape = PanelShape, color = WorkshopColors.ItemBackground, modifier = Modifier.fillMaxHeight()) {
            Row(
                modifier = Modifier.padding(PanelPadding),
                horizontalArrangement = Arrangement.spacedBy(SectionSpacing),
            ) {
                ShadeGrid(selectedColor) { pick(HsvColor.fromArgb(it)) }
                Column(verticalArrangement = Arrangement.spacedBy(SectionSpacing)) {
                    HueWheel(hsv) { hue, saturation -> pick(hsv.copy(hue = hue, saturation = saturation)) }
                    LightnessSlider(hsv) { pick(hsv.copy(value = it)) }
                    PanelButton(
                        selected = false,
                        description = stringResource(R.string.color_eyedropper),
                        onClick = onEyedropper,
                    ) { DrawableIcon(R.drawable.ic_eyedropper) }
                }
            }
        }
    }
}

/** Tonlu renk ızgarası; kareler panelin yüksekliğine sığacak kadar büyür. */
@Composable
private fun ShadeGrid(selectedColor: Int?, onColorSelected: (Int) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxHeight()) {
        val rows = ColorShades.rows
        val cellSize = min((maxHeight - CellSpacing * (rows.size - 1)) / rows.size, MaxCellSize)
        Column(verticalArrangement = Arrangement.spacedBy(CellSpacing)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(CellSpacing)) {
                    row.forEach { color -> ShadeCell(color, color == selectedColor, cellSize) { onColorSelected(color) } }
                }
            }
        }
    }
}

@Composable
private fun ShadeCell(color: Int, selected: Boolean, size: Dp, onClick: () -> Unit) {
    val description = stringResource(R.string.color_swatch)
    Box(
        Modifier
            .size(size)
            .clip(CellShape)
            .background(Color(color))
            .border(
                width = if (selected) SelectedCellBorder else CellBorder,
                color = if (selected) WorkshopColors.Selection else WorkshopColors.Outline,
                shape = CellShape,
            )
            .clickableWithSound(onClick = onClick)
            .semantics { contentDescription = description },
    )
}

/** Renk tekerleği: açı renk tonunu, merkeze uzaklık doygunluğu seçer; açıklık çubuğa göre koyulaşır. */
@Composable
private fun HueWheel(hsv: HsvColor, onPicked: (hue: Float, saturation: Float) -> Unit) {
    val description = stringResource(R.string.color_wheel)
    Canvas(
        Modifier
            .size(WheelSize)
            .semantics { contentDescription = description }
            .pickPositions { position, size ->
                val radius = size.width / 2f
                val dx = position.x - radius
                val dy = position.y - radius
                val hue = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat().mod(FULL_TURN_DEGREES)
                onPicked(hue, (hypot(dx, dy) / radius).coerceAtMost(1f))
            },
    ) {
        val radius = size.minDimension / 2f
        drawCircle(Brush.sweepGradient(WheelHues, center))
        drawCircle(Brush.radialGradient(listOf(Color.White, Color.Transparent), center, radius))
        drawCircle(Color.Black.copy(alpha = 1f - hsv.value))
        val angle = Math.toRadians(hsv.hue.toDouble())
        val distance = hsv.saturation * radius
        drawMarker(center + Offset((cos(angle) * distance).toFloat(), (sin(angle) * distance).toFloat()), hsv)
    }
}

/** Açıklık çubuğu: soldaki siyahtan sağdaki en açık renge kadar seçilir. */
@Composable
private fun LightnessSlider(hsv: HsvColor, onPicked: (Float) -> Unit) {
    val description = stringResource(R.string.color_lightness)
    Canvas(
        Modifier
            .width(WheelSize)
            .height(SliderHeight)
            .semantics { contentDescription = description }
            .pickPositions { position, size -> onPicked((position.x / size.width).coerceIn(0f, 1f)) },
    ) {
        val brightest = Color(hsv.copy(value = 1f).toArgb())
        val corner = CornerRadius(size.height / 2f)
        drawRoundRect(Brush.horizontalGradient(listOf(Color.Black, brightest)), cornerRadius = corner)
        drawRoundRect(WorkshopColors.Outline, cornerRadius = corner, style = Stroke(CellBorder.toPx()))
        drawMarker(Offset(hsv.value * size.width, size.height / 2f), hsv)
    }
}

/** Seçili noktayı gösteren, içi seçili renkte olan beyaz halkalı daire. */
private fun DrawScope.drawMarker(position: Offset, hsv: HsvColor) {
    val radius = MarkerRadius.toPx()
    drawCircle(Color(hsv.toArgb()), radius, position)
    drawCircle(Color.White, radius, position, style = Stroke(MarkerRing.toPx()))
    drawCircle(Color.Black, radius + MarkerRing.toPx() / 2f, position, style = Stroke(MarkerOutline.toPx() / 2f))
}

/** Dokunulan ve parmağın kaydırıldığı her noktayı, öğenin boyutuyla birlikte bildirir. */
@Composable
private fun Modifier.pickPositions(onPick: (Offset, IntSize) -> Unit): Modifier {
    val latest by rememberUpdatedState(onPick)
    return pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            latest(down.position, size)
            while (true) {
                val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                change.consume()
                latest(change.position, size)
            }
        }
    }
}

/** Tekerlek çevresindeki tam canlı tonlar; ilk ve son durak aynı renktir, böylece çember kapanır. */
private val WheelHues: List<Color> = (0..WHEEL_HUE_STOPS).map {
    Color(HsvColor(it * FULL_TURN_DEGREES / WHEEL_HUE_STOPS, 1f, 1f).toArgb())
}
