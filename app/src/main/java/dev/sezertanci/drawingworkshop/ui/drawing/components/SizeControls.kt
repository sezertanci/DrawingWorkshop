package dev.sezertanci.drawingworkshop.ui.drawing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.sezertanci.drawingworkshop.R
import dev.sezertanci.drawingworkshop.domain.model.BrushSize
import dev.sezertanci.drawingworkshop.ui.components.PanelButton
import dev.sezertanci.drawingworkshop.ui.theme.WorkshopColors

/** Boyut noktasının ince kenarı; beyaz gibi zeminle aynı renkler de görünür kalır. */
private val SizeDotOutline = 1.dp

/** Menüdeki boyutlar alt alta sığmadığından bu kadar sütuna dizilir; satırlar soldan sağa büyür. */
private const val SIZE_MENU_COLUMNS = 2

/** Her sütunun boyutları: soldaki sütunda 1., 3., 5. boyut; sağdakinde 2., 4., 6. boyut. */
private val SizeMenuColumns = List(SIZE_MENU_COLUMNS) { column ->
    BrushSize.entries.filterIndexed { index, _ -> index % SIZE_MENU_COLUMNS == column }
}

/**
 * Seçili fırça boyutunu gösteren panel düğmesi. Dokununca boyutlar iki sütunlu bir kutuda yanında açılır;
 * seçim yapılınca kutu kapanır. Noktalar [dotBrush] ile, yani çizilecek renkle boyanır.
 */
@Composable
internal fun SizeMenuButton(selected: BrushSize, dotBrush: Brush, onSelected: (BrushSize) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PanelButton(
            selected = false,
            description = stringResource(R.string.size_menu),
            onClick = { expanded = true },
        ) { SizeDot(selected, dotBrush) }
        ToolMenu(expanded, onDismiss = { expanded = false }) {
            Row {
                SizeMenuColumns.forEach { column ->
                    ChoiceColumn(
                        options = column,
                        selected = selected,
                        labelOf = { it.labelRes },
                        onSelected = {
                            expanded = false
                            onSelected(it)
                        },
                    ) { SizeDot(it, dotBrush) }
                }
            }
        }
    }
}

/** Boyutun çapında, kenarlı yuvarlak nokta. */
@Composable
private fun SizeDot(size: BrushSize, brush: Brush) {
    Box(
        Modifier
            .size(size.previewDiameter)
            .background(brush, CircleShape)
            .border(SizeDotOutline, WorkshopColors.Outline, CircleShape),
    )
}
