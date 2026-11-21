package dev.sezertanci.drawingworkshop.domain.fill

/**
 * Desenli dolgunun piksellerine gözle görülmeyen bir imza koyar. Kırmızı, yeşil ve mavi kanalların en
 * düşük iki biti sabit bir değere ayarlanır; renk en fazla üç ton kayar. Boya kovası dokunulan pikselde
 * bu imzayı görürse, desenin iki rengini birden kapsayan alanın tamamını tanır ve yeniden boyar.
 */
object PatternMark {
    private const val LOW_BITS = 0x030303
    private const val SIGNATURE = 0x010203

    fun mark(color: Int): Int = (color and LOW_BITS.inv()) or SIGNATURE

    fun isMarked(color: Int): Boolean = ColorMath.alpha(color) == ColorMath.MAX_CHANNEL && (color and LOW_BITS) == SIGNATURE

    /** Düz renk imzayı taşıyorsa mavi kanalın düşük bitleri değiştirilir; renk desen sanılmaz. */
    fun clear(color: Int): Int = if (isMarked(color)) color xor BLUE_LOW_BITS else color

    private const val BLUE_LOW_BITS = 0x000003
}
