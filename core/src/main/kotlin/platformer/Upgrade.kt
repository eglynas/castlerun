package platformer

import kotlin.math.pow
import kotlin.math.roundToInt

enum class ScalingType { ADDITIVE, MULTIPLICATIVE }

class Upgrade(
    val name: String,
    var level: Int = 0,
    val maxLevel: Int = 10,
    var currentCost: Int = 100,
    val baseCost: Int = 100,
    val costScale: Float = 1.1f,
    var currentValue: Float = 0f,
    val baseValue: Float = 1f,
    val valueScale: Float = 1.1f,
    val scalingType: ScalingType = ScalingType.ADDITIVE,
    val increment: Float = 1f
) {
    init {
        refreshValue()
    }

    fun canUpgrade() = level < maxLevel

    fun upgrade() {
        if (!canUpgrade()) return
        level++
        currentCost = (baseCost * costScale.toDouble().pow((level - 1).toDouble())).toInt()
        refreshValue()
    }

    fun getCurrentCost(baseCost: Int, level: Int, costMultiplier: Float): Int {
        val raw = baseCost * costMultiplier.pow(level - 1)
        return when {
            raw >= 10f -> ((raw / 5f).roundToInt() * 5f).toInt()
            else -> ((raw * 100).roundToInt() / 100f).toInt()
        }
    }

    fun refreshValue() {
        currentValue = when (scalingType) {
            ScalingType.ADDITIVE -> baseValue + increment * (level - 1)
            ScalingType.MULTIPLICATIVE -> (baseValue * valueScale.toDouble().pow((level - 1).toDouble())).toFloat()
        }
    }
}
