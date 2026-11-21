package dev.sezertanci.drawingworkshop.domain.model

import dev.sezertanci.drawingworkshop.domain.stroke.RainbowHues.Companion.FULL_TURN_DEGREES

/**
 * Geniş renk panelindeki tonlu ızgara. Her satır bir renk tonudur ve açıktan koyuya doğru dizilir:
 * önce soluk pastelden canlıya, sonra canlıdan koyuya. Son satır beyazdan siyaha grilerdir.
 */
object ColorShades {
    /** Renk çemberi bu kadar eşit aralıklı tona bölünür. */
    const val HUE_COUNT = 12

    /** Bir satırdaki tonların doygunluk ve açıklığı, soldan sağa. */
    private val SHADES = listOf(
        0.25f to 1f,
        0.5f to 1f,
        0.75f to 1f,
        1f to 1f,
        1f to 0.75f,
        1f to 0.5f,
    )

    /** Gri satırındaki açıklıklar, beyazdan siyaha. */
    private val GRAYS = listOf(1f, 0.8f, 0.6f, 0.4f, 0.2f, 0f)

    /** Satır satır ARGB renkleri: [HUE_COUNT] renk tonu satırı ve en sonda gri satırı. */
    val rows: List<List<Int>> = List(HUE_COUNT) { index ->
        val hue = index * FULL_TURN_DEGREES / HUE_COUNT
        SHADES.map { (saturation, value) -> HsvColor(hue, saturation, value).toArgb() }
    } + listOf(GRAYS.map { HsvColor(0f, 0f, it).toArgb() })
}
