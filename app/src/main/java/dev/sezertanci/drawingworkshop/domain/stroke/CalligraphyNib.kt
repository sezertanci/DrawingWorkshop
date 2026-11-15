package dev.sezertanci.drawingworkshop.domain.stroke

import kotlin.math.cos
import kotlin.math.sin

/**
 * Yassı ve eğik uçlu kaligrafi kaleminin geometrisi. Uç, [width] genişliğinde ve yataydan
 * [angleDegrees] derece yukarı eğik bir çizgidir; kalem hareket ettikçe bu çizginin taradığı alan
 * boyanır. Uca dik yönde çekilen çizgiler kalın, uca paralel çekilenler ince olur.
 */
class CalligraphyNib(width: Float, angleDegrees: Float) {
    private val halfX: Float
    private val halfY: Float

    init {
        val radians = Math.toRadians(angleDegrees.toDouble())
        // Ekranda y ekseni aşağı doğru arttığından yukarı eğim için y bileşeni eksi alınır.
        halfX = (cos(radians) * width / 2.0).toFloat()
        halfY = (-sin(radians) * width / 2.0).toFloat()
    }

    /**
     * Ucun [from] noktasından [to] noktasına giderken taradığı paralelkenarın dört köşesi. Köşeler her
     * zaman aynı dönüş yönünde sıralanır; böylece üst üste binen parçalar dolguda birbirini silmez.
     */
    fun footprint(from: StrokePoint, to: StrokePoint): List<StrokePoint> {
        val corners = listOf(
            StrokePoint(from.x - halfX, from.y - halfY),
            StrokePoint(from.x + halfX, from.y + halfY),
            StrokePoint(to.x + halfX, to.y + halfY),
            StrokePoint(to.x - halfX, to.y - halfY),
        )
        val turn = halfX * (to.y - from.y) - halfY * (to.x - from.x)
        return if (turn >= 0f) corners else corners.asReversed()
    }
}
