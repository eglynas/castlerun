package platformer

import kotlin.math.pow

enum class ScalingType { ADDITIVE, MULTIPLICATIVE }

class Upgrade(
    val name: String,
    var level: Int = 0,
    var currentValue: Float = 0f,
    val maxLevel: Int = 10,
    var cost: Int = 100,
    val baseCost: Int = 100,
    val baseValue: Float = 1f,
    val costScale: Float = 1.15f,
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
        cost = (baseCost * Math.pow(costScale.toDouble(), (level - 1).toDouble())).toInt()
        refreshValue()
    }

    fun refreshValue() {
        currentValue = when (scalingType) {
            ScalingType.ADDITIVE -> baseValue + increment * (level - 1)
            ScalingType.MULTIPLICATIVE -> (baseValue * valueScale.toDouble().pow((level - 1).toDouble())).toFloat()
        }
    }
}
