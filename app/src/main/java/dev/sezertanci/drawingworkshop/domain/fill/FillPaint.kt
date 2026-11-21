package dev.sezertanci.drawingworkshop.domain.fill

/** Boya kovasının bir pikseli hangi renge boyayacağı. */
sealed interface FillPaint {
    fun colorAt(x: Int, y: Int): Int

    /** Dolgu bu pikselden başlasa hiçbir şey değişmeyecekse `true` döner. */
    fun alreadyCovers(pixel: Int): Boolean

    /** Tek renk; desen imzası taşımaz, böylece düz boyanan alan desen sanılmaz. */
    class Solid(color: Int) : FillPaint {
        private val color = PatternMark.clear(color)

        override fun colorAt(x: Int, y: Int): Int = color

        override fun alreadyCovers(pixel: Int): Boolean = pixel == color
    }

    /**
     * Tuvalin sol üst köşesinden başlayarak [tileSize] piksellik karolarla döşenen desen. Desenin her
     * pikseli [ink] veya [background] renginde ve imzalıdır; komşu dolgular aynı karo düzenine oturur.
     */
    class Patterned(
        private val pattern: FillPattern,
        ink: Int,
        background: Int,
        private val tileSize: Float,
    ) : FillPaint {
        private val ink = PatternMark.mark(ink)
        private val background = PatternMark.mark(background)

        override fun colorAt(x: Int, y: Int): Int =
            if (pattern.isInk(tileShare(x), tileShare(y))) ink else background

        override fun alreadyCovers(pixel: Int): Boolean = false

        /** Piksel merkezinin karo içindeki oransal yeri. */
        private fun tileShare(position: Int): Float {
            val inTile = (position + PIXEL_CENTER) % tileSize
            return inTile / tileSize
        }
    }

    companion object {
        private const val PIXEL_CENTER = 0.5f

        fun of(pattern: FillPattern, color: Int, background: Int, tileSize: Float): FillPaint =
            if (pattern == FillPattern.SOLID) Solid(color) else Patterned(pattern, color, background, tileSize)
    }
}
