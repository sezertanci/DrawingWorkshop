package dev.sezertanci.drawingworkshop.domain.model

/** ARGB tamsayı renklerinin kanallarını okuyan ve kanallardan renk kuran yardımcılar. */
object Argb {
    const val CHANNEL_MAX = 255
    const val RED_SHIFT = 16
    const val GREEN_SHIFT = 8
    const val BLUE_SHIFT = 0
    const val ALPHA_SHIFT = 24

    /** [color] renginin [shift] konumundaki kanalı (0 ile 255 arası). */
    fun channel(color: Int, shift: Int): Int = (color shr shift) and CHANNEL_MAX

    /** Kanallarından opak bir renk kurar; kanallar 0 ile 255 arasında olmalıdır. */
    fun opaque(red: Int, green: Int, blue: Int): Int =
        (CHANNEL_MAX shl ALPHA_SHIFT) or (red shl RED_SHIFT) or (green shl GREEN_SHIFT) or (blue shl BLUE_SHIFT)

    /** [source] rengini [destination] renginin üzerine koyar (premültiplike olmayan ARGB). */
    fun sourceOver(source: Int, destination: Int): Int {
        val sa = channel(source, ALPHA_SHIFT)
        val da = channel(destination, ALPHA_SHIFT)
        val outAlpha = sa + da * (CHANNEL_MAX - sa) / CHANNEL_MAX
        if (outAlpha == 0) return 0
        var result = outAlpha shl ALPHA_SHIFT
        for (shift in COLOR_SHIFTS) {
            val blended = (channel(source, shift) * sa +
                channel(destination, shift) * da * (CHANNEL_MAX - sa) / CHANNEL_MAX) / outAlpha
            result = result or (blended shl shift)
        }
        return result
    }

    private val COLOR_SHIFTS = intArrayOf(RED_SHIFT, GREEN_SHIFT, BLUE_SHIFT)
}
