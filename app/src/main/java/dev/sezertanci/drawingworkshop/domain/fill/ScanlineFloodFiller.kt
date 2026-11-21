package dev.sezertanci.drawingworkshop.domain.fill

import dev.sezertanci.drawingworkshop.domain.geometry.PixelRect
import dev.sezertanci.drawingworkshop.domain.model.Argb
import javax.inject.Inject
import kotlin.math.abs

/**
 * Satır taramalı (scanline) boya kovası.
 *
 * Özyineleme kullanmaz; büyük alanlarda bile yığın taşması olmaz. Başlangıç rengine [TOLERANCE]
 * kadar yakın pikseller doldurulur; desenli alanlar bu sırada dışarıda kalır. Dokunulan piksel desen
 * imzası ([PatternMark]) taşıyorsa, desenin iki rengini birden kapsayan imzalı alanın tamamı doldurulur. Ardından kenardaki yarı saydam (kenar yumuşatmalı) pikseller
 * yeni rengin üzerine karıştırılır; böylece çizgi ile dolgu arasında beyaz hale kalmaz.
 */
class ScanlineFloodFiller @Inject constructor() : FloodFiller {

    override fun fill(
        pixels: IntArray,
        width: Int,
        height: Int,
        startX: Int,
        startY: Int,
        paint: FillPaint,
        walls: BooleanArray?,
        boundary: PixelRect?,
    ): PixelRect? {
        if (startX !in 0 until width || startY !in 0 until height) return null
        val target = pixels[startY * width + startX]
        if (paint.alreadyCovers(target)) return null

        val side = boundary?.let { BoundarySide(it, width, it.contains(startX, startY)) }
        val region = FillRegion(pixels, width, height, Matcher(target), walls, side, paint)
        region.floodFrom(startX, startY)
        repeat(EDGE_BLEND_PASSES) { region.blendEdges() }
        return region.bounds()
    }

    private class FillRegion(
        private val pixels: IntArray,
        private val width: Int,
        private val height: Int,
        private val matcher: Matcher,
        private val walls: BooleanArray?,
        private val side: BoundarySide?,
        private val paint: FillPaint,
    ) {
        private val filled = BooleanArray(pixels.size)
        private val stack = IntStack()
        private var minX = width
        private var minY = height
        private var maxX = -1
        private var maxY = -1

        fun floodFrom(x: Int, y: Int) {
            stack.push(y * width + x)
            while (stack.isNotEmpty()) {
                val index = stack.pop()
                if (canFill(index)) fillSpan(index % width, index / width)
            }
        }

        private fun fillSpan(x: Int, y: Int) {
            val row = y * width
            var left = x
            while (left > 0 && canFill(row + left - 1)) left--
            var right = x
            while (right < width - 1 && canFill(row + right + 1)) right++
            for (i in left..right) paintPixel(row + i)
            include(left, y)
            include(right, y)
            if (y > 0) pushSpanSeeds(left, right, y - 1)
            if (y < height - 1) pushSpanSeeds(left, right, y + 1)
        }

        /** Komşu satırdaki her doldurulabilir aralığın ilk pikselini yığına ekler. */
        private fun pushSpanSeeds(left: Int, right: Int, y: Int) {
            val row = y * width
            var inSpan = false
            for (i in left..right) {
                val fillable = canFill(row + i)
                if (fillable && !inSpan) stack.push(row + i)
                inSpan = fillable
            }
        }

        fun blendEdges() {
            if (maxX < 0) return
            val edges = IntStack()
            val x0 = (minX - 1).coerceAtLeast(0)
            val x1 = (maxX + 1).coerceAtMost(width - 1)
            val y0 = (minY - 1).coerceAtLeast(0)
            val y1 = (maxY + 1).coerceAtMost(height - 1)
            for (y in y0..y1) {
                for (x in x0..x1) {
                    val index = y * width + x
                    if (!filled[index] && isOnSide(index) && isTranslucent(pixels[index]) && touchesFilled(x, y)) edges.push(index)
                }
            }
            while (edges.isNotEmpty()) {
                val index = edges.pop()
                pixels[index] = Argb.sourceOver(pixels[index], paint.colorAt(index % width, index / width))
                filled[index] = true
                include(index % width, index / width)
            }
        }

        fun bounds(): PixelRect? = if (maxX < 0) null else PixelRect(minX, minY, maxX + 1, maxY + 1)

        private fun touchesFilled(x: Int, y: Int): Boolean {
            val index = y * width + x
            return (x > 0 && filled[index - 1]) ||
                (x < width - 1 && filled[index + 1]) ||
                (y > 0 && filled[index - width]) ||
                (y < height - 1 && filled[index + width])
        }

        private fun canFill(index: Int): Boolean =
            !filled[index] && (walls == null || !walls[index]) && isOnSide(index) && matcher.matches(pixels[index])

        private fun isOnSide(index: Int): Boolean = side == null || side.matches(index)

        private fun paintPixel(index: Int) {
            pixels[index] = paint.colorAt(index % width, index / width)
            filled[index] = true
        }

        private fun include(x: Int, y: Int) {
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
        }

        private fun isTranslucent(pixel: Int): Boolean = ColorMath.alpha(pixel) in 1 until ColorMath.MAX_CHANNEL
    }

    /** Başlangıç pikseline göre hangi piksellerin aynı alana ait sayılacağı. */
    private class Matcher(private val target: Int) {
        private val patterned = PatternMark.isMarked(target)

        fun matches(pixel: Int): Boolean =
            if (patterned) {
                PatternMark.isMarked(pixel)
            } else {
                !PatternMark.isMarked(pixel) && ColorMath.isClose(pixel, target, TOLERANCE)
            }
    }

    /** Dolgunun [boundary] kenarının hangi tarafında ([inside]) kalacağını belirler. */
    private class BoundarySide(private val boundary: PixelRect, private val width: Int, private val inside: Boolean) {
        fun matches(index: Int): Boolean = boundary.contains(index % width, index / width) == inside
    }

    private class IntStack {
        private var items = IntArray(INITIAL_STACK_SIZE)
        private var size = 0

        fun push(value: Int) {
            if (size == items.size) items = items.copyOf(size * 2)
            items[size++] = value
        }

        fun pop(): Int = items[--size]

        fun isNotEmpty(): Boolean = size > 0
    }

    private companion object {
        const val TOLERANCE = 48
        const val EDGE_BLEND_PASSES = 2
        const val INITIAL_STACK_SIZE = 1024
    }
}

/** ARGB tam sayıları üzerinde renk hesapları. */
internal object ColorMath {
    const val MAX_CHANNEL = Argb.CHANNEL_MAX

    fun alpha(color: Int): Int = channel(color, ALPHA_SHIFT)

    private fun channel(color: Int, shift: Int): Int = Argb.channel(color, shift)

    fun isClose(a: Int, b: Int, tolerance: Int): Boolean =
        CHANNEL_SHIFTS.all { abs(channel(a, it) - channel(b, it)) <= tolerance }

    private const val ALPHA_SHIFT = Argb.ALPHA_SHIFT
    private val CHANNEL_SHIFTS = intArrayOf(ALPHA_SHIFT, Argb.RED_SHIFT, Argb.GREEN_SHIFT, Argb.BLUE_SHIFT)
}
