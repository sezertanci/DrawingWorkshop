package dev.sezertanci.drawingworkshop.domain.model

import dev.sezertanci.drawingworkshop.domain.stroke.RainbowHues.Companion.FULL_TURN_DEGREES
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Renk tonu, doygunluk ve açıklıkla tanımlanan renk. Renk tekerleğinde ton açıya, doygunluk merkeze
 * uzaklığa karşılık gelir; açıklık tekerleğin altındaki çubukla seçilir.
 *
 * @property hue Renk tonu, derece cinsinden (0 ile 360 arası).
 * @property saturation Doygunluk (0: gri, 1: tam canlı).
 * @property value Açıklık (0: siyah, 1: en açık).
 */
data class HsvColor(val hue: Float, val saturation: Float, val value: Float) {

    /** Opak ARGB rengi. */
    fun toArgb(): Int {
        val chroma = value * saturation
        val sector = hue.mod(FULL_TURN_DEGREES) / SECTOR_DEGREES
        val second = chroma * (1f - abs(sector % 2f - 1f))
        val (red, green, blue) = when (sector.toInt()) {
            0 -> Triple(chroma, second, 0f)
            1 -> Triple(second, chroma, 0f)
            2 -> Triple(0f, chroma, second)
            3 -> Triple(0f, second, chroma)
            4 -> Triple(second, 0f, chroma)
            else -> Triple(chroma, 0f, second)
        }
        val offset = value - chroma
        return Argb.opaque(toChannel(red + offset), toChannel(green + offset), toChannel(blue + offset))
    }

    companion object {
        /** Renk çemberi, her biri bir ana rengin yönüne bakan bu kadar dilime bölünür. */
        private const val SECTORS = 6
        private const val SECTOR_DEGREES = FULL_TURN_DEGREES / SECTORS

        /** ARGB renginin karşılığı; saydamlık yok sayılır. Grilerin tonu 0 kabul edilir. */
        fun fromArgb(color: Int): HsvColor {
            val red = fromChannel(Argb.channel(color, Argb.RED_SHIFT))
            val green = fromChannel(Argb.channel(color, Argb.GREEN_SHIFT))
            val blue = fromChannel(Argb.channel(color, Argb.BLUE_SHIFT))
            val max = maxOf(red, green, blue)
            val chroma = max - minOf(red, green, blue)
            val sector = when {
                chroma == 0f -> 0f
                max == red -> ((green - blue) / chroma).mod(SECTORS.toFloat())
                max == green -> (blue - red) / chroma + 2f
                else -> (red - green) / chroma + 4f
            }
            val saturation = if (max == 0f) 0f else chroma / max
            return HsvColor(sector * SECTOR_DEGREES, saturation, max)
        }

        private fun toChannel(share: Float): Int = (share * Argb.CHANNEL_MAX).roundToInt().coerceIn(0, Argb.CHANNEL_MAX)

        private fun fromChannel(channel: Int): Float = channel.toFloat() / Argb.CHANNEL_MAX
    }
}
