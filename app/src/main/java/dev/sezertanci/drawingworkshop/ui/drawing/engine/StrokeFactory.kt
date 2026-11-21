package dev.sezertanci.drawingworkshop.ui.drawing.engine

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.ColorUtils
import dev.sezertanci.drawingworkshop.domain.model.StrokeSettings
import dev.sezertanci.drawingworkshop.domain.model.ToolType
import dev.sezertanci.drawingworkshop.domain.stroke.CalligraphyNib
import dev.sezertanci.drawingworkshop.domain.stroke.RainbowHues
import dev.sezertanci.drawingworkshop.domain.stroke.SilkMotion
import dev.sezertanci.drawingworkshop.domain.stroke.SprayScatter
import javax.inject.Inject

/** Seçili araca göre doğru boyayla yapılandırılmış bir [ActiveStroke] oluşturur. */
class StrokeFactory @Inject constructor(
    private val density: ScreenDensity,
    private val crayonTextures: CrayonTextureCache,
    private val sprayScatter: SprayScatter,
) {
    /**
     * Boya kovası bir çizgi değil tek dokunuşluk bir işlem olduğundan `null` döner. Tüm ölçüler
     * [zoom] oranına bölünür; görüntü büyütülmüşken çizgi ekranda yine aynı kalınlıkta görünür.
     */
    fun create(settings: StrokeSettings, backgroundColor: Int, zoom: Float): ActiveStroke? {
        if (settings.tool == ToolType.SILK) return null
        val style = TOOL_STYLES[settings.tool] ?: return null
        val scale = Scale(density, zoom)
        val width = scale.toPx(settings.size.widthDp) * style.widthFactor
        return when (settings.tool) {
            ToolType.SPRAY -> createSpray(settings, width, scale)
            ToolType.ERASER -> createEraser(backgroundColor, width, scale)
            ToolType.CALLIGRAPHY -> createCalligraphy(settings, width, scale)
            ToolType.GLITTER -> createGlitter(settings, width, scale)
            else -> if (settings.rainbow) {
                createRainbow(width, style.alpha, scale, rainbowHues(scale))
            } else {
                createPath(settings, width, style.alpha, scale)
            }
        }
    }

    /**
     * Kendiliğinden ilerleyen çizgiyi (ipek fırçası) oluşturur; diğer araçlar için `null` döner.
     * Fırça boyutu şeridin genişliğini belirler; seçili stil tellerin hareketini, temposunu, sayısını ve
     * saydamlığını belirler. Koyu zeminde renkler üst üste bindikçe toplanıp ışık saçar; açık zeminde ise
     * yarı saydam katmanlar halinde koyulaşır.
     */
    fun createLive(settings: StrokeSettings, backgroundColor: Int, zoom: Float): LiveStroke? {
        if (settings.tool != ToolType.SILK) return null
        val scale = Scale(density, zoom)
        val style = settings.silkStyle
        val threads = style.threads
        val spread = scale.toPx(settings.size.widthDp) * TOOL_STYLES.getValue(ToolType.SILK).widthFactor *
            threads.spreadShare
        val glows = Color.luminance(backgroundColor) < DARK_BACKGROUND_LUMINANCE
        val threadPaint = silkPaint(settings.color, scale.toPx(threads.threadWidthDp), threads.threadAlpha, glows)
        val sheetPaint = silkPaint(settings.color, scale.toPx(SILK_SHEET_DP), threads.sheetAlpha, glows)
        val motion = SilkMotion(threads.threadCount, spread, style.tempo.tuning, style.movement)
        return SilkStroke(motion, threadPaint, sheetPaint, settings.color)
    }

    private fun silkPaint(color: Int, width: Float, alpha: Int, glows: Boolean) =
        strokePaint(color, width, alpha).apply {
            if (glows) xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        }

    /** Renk çizgi boyunca kendiliğinden ve rastgele değişir; aracın saydamlığı bütün çizgiye birden uygulanır. */
    private fun createRainbow(width: Float, alpha: Int, scale: Scale, hues: RainbowHues): ActiveStroke {
        val paint = strokePaint(hues.colorAt(0f), width, OPAQUE)
        val layerAlpha = alpha.takeIf { it < OPAQUE }
        return RainbowStroke(
            paint,
            scale.toPx(MIN_POINT_DISTANCE_DP),
            scale.toPx(RAINBOW_SECTION_DP),
            hues,
            layerAlpha,
        )
    }

    private fun rainbowHues(scale: Scale) = RainbowHues(scale.toPx(RAINBOW_LEG_DP))

    private fun createPath(settings: StrokeSettings, width: Float, alpha: Int, scale: Scale): ActiveStroke {
        val paint = strokePaint(settings.color, width, alpha)
        if (settings.tool == ToolType.CRAYON) paint.shader = crayonTextures.shaderFor(settings.color)
        return PathStroke(paint, paint, scale.toPx(MIN_POINT_DISTANCE_DP))
    }

    /** Yassı uçlu kalem; uç genişliği fırça boyutundan, en ince çizgi kalınlığı sabit dp'den gelir. */
    private fun createCalligraphy(settings: StrokeSettings, nibWidth: Float, scale: Scale): ActiveStroke {
        val paint = strokePaint(settings.color, scale.toPx(CALLIGRAPHY_HAIRLINE_DP), OPAQUE).apply {
            style = Paint.Style.FILL_AND_STROKE
        }
        return CalligraphyStroke(
            paint,
            CalligraphyNib(nibWidth, CALLIGRAPHY_NIB_ANGLE),
            nibWidth,
            scale.toPx(MIN_POINT_DISTANCE_DP),
            scale.toPx(CALLIGRAPHY_PIECE_DP),
            scale.toPx(RAINBOW_SECTION_DP),
            if (settings.rainbow) rainbowHues(scale) else null,
        )
    }

    /**
     * Sim tozu: seçili renkte (veya gökkuşağı renklerinde) bir çizginin üzerine beyaz ve rengin açık
     * tonunda parıltı taneleri serpilir, aralarına küçük beyaz yıldız ışıltıları konur. Gökkuşağında
     * çizgi ve açık tonlu taneler aynı renk akışını izler.
     */
    private fun createGlitter(settings: StrokeSettings, width: Float, scale: Scale): ActiveStroke {
        val hues = if (settings.rainbow) rainbowHues(scale) else null
        val line = hues?.let { createRainbow(width, OPAQUE, scale, it) } ?: createPath(settings, width, OPAQUE, scale)
        val baseColor: (Float) -> Int = hues?.let { it::colorAt } ?: { settings.color }
        val radius = width / 2f
        val grainWidth = scale.toPx(GLITTER_GRAIN_DP)
        val tintGrains = SprayStroke(
            strokePaint(settings.color, grainWidth, OPAQUE),
            radius,
            GLITTER_GRAINS_PER_TOUCH,
            sprayScatter,
        ) { ColorUtils.blendARGB(baseColor(it), Color.WHITE, GLITTER_TINT_SHARE) }
        val whiteGrains = SprayStroke(strokePaint(Color.WHITE, grainWidth, OPAQUE), radius, GLITTER_GRAINS_PER_TOUCH, sprayScatter)
        val starPaint = strokePaint(Color.WHITE, 0f, OPAQUE).apply { style = Paint.Style.FILL }
        val stars = SparkleStars(starPaint, radius, scale.toPx(GLITTER_STAR_SPACING_DP), scale.toPx(GLITTER_STAR_DP), sprayScatter)
        return LayeredStroke(listOf(line, tintGrains, whiteGrains, stars))
    }

    /** Önizlemede arka plan rengiyle boyar, boya katmanında ise pikselleri tamamen siler. */
    private fun createEraser(backgroundColor: Int, width: Float, scale: Scale): ActiveStroke {
        val preview = strokePaint(backgroundColor, width, OPAQUE)
        val clear = strokePaint(backgroundColor, width, OPAQUE).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        return PathStroke(preview, clear, scale.toPx(MIN_POINT_DISTANCE_DP))
    }

    private fun createSpray(settings: StrokeSettings, radius: Float, scale: Scale): ActiveStroke {
        val dotPaint = strokePaint(settings.color, scale.toPx(SPRAY_DOT_DP), OPAQUE)
        val hues = if (settings.rainbow) rainbowHues(scale) else null
        return SprayStroke(dotPaint, radius, SPRAY_DOTS_PER_TOUCH, sprayScatter, hues?.let { it::colorAt })
    }

    private fun strokePaint(color: Int, width: Float, alpha: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.alpha = alpha
        style = Paint.Style.STROKE
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    /** dp ölçülerini, büyütme oranını hesaba katarak resmin pikseline çevirir. */
    private class Scale(private val density: ScreenDensity, private val zoom: Float) {
        fun toPx(dp: Float): Float = density.toPx(dp) / zoom
    }

    /** Aracın boyut katsayısı ve saydamlığı. */
    private data class ToolStyle(val widthFactor: Float, val alpha: Int = OPAQUE)

    private companion object {
        const val OPAQUE = 255
        const val MIN_POINT_DISTANCE_DP = 1f
        const val SPRAY_DOT_DP = 1.5f
        const val SPRAY_DOTS_PER_TOUCH = 14

        /** Sim tozu parıltı tanelerinin çapı ve her dokunuşta serpilen beyaz ve açık tonlu tane sayısı. */
        const val GLITTER_GRAIN_DP = 2f
        const val GLITTER_GRAINS_PER_TOUCH = 3

        /** Taneler, çizgi renginin bu oranda beyazla karışmış açık tonudur. */
        const val GLITTER_TINT_SHARE = 0.6f

        /** Sim tozu yıldızlarının aralığı ve büyük yıldızın merkezden ucuna uzunluğu. */
        const val GLITTER_STAR_SPACING_DP = 28f
        const val GLITTER_STAR_DP = 5f

        /** Gökkuşağı renginin bir hedef renkten sonrakine geçerken aldığı yol. */
        const val RAINBOW_LEG_DP = 400f

        /** Gökkuşağı çizgisinin tek renk geçişiyle boyanan bölümlerinin uzunluğu. */
        const val RAINBOW_SECTION_DP = 6f

        /** Kaligrafi ucunun yataydan yukarı doğru eğim açısı (derece). */
        const val CALLIGRAPHY_NIB_ANGLE = 45f

        /** Kaligrafi çizgisinin uca paralel çekildiği en ince yerdeki kalınlığı. */
        const val CALLIGRAPHY_HAIRLINE_DP = 1f

        /** Kaligrafi çizgisinin bölündüğü düz parçaların en büyük uzunluğu; eğriler köşeli görünmez. */
        const val CALLIGRAPHY_PIECE_DP = 3f

        /** Bu parlaklığın altındaki zeminler koyu sayılır; ipek orada ışık saçarak çizilir. */
        const val DARK_BACKGROUND_LUMINANCE = 0.4f
        const val SILK_SHEET_DP = 0.75f

        val TOOL_STYLES = mapOf(
            ToolType.PENCIL to ToolStyle(widthFactor = 0.35f, alpha = 230),
            ToolType.BRUSH to ToolStyle(widthFactor = 1f),
            ToolType.SILK to ToolStyle(widthFactor = 6f),
            ToolType.MARKER to ToolStyle(widthFactor = 1.3f, alpha = 170),
            ToolType.CALLIGRAPHY to ToolStyle(widthFactor = 1.4f),
            ToolType.CRAYON to ToolStyle(widthFactor = 1.2f),
            ToolType.SPRAY to ToolStyle(widthFactor = 1.6f),
            ToolType.GLITTER to ToolStyle(widthFactor = 1.2f),
            ToolType.ERASER to ToolStyle(widthFactor = 1.6f),
        )
    }
}
