package com.example.platformer

enum class CoinType(val value: Int, val spawnChance: Double) {
    GOLD(1, 80.0),
    RUBY(5, 15.0),
    SAPPHIRE(10, 5.0)
}


data class Coin(
    var x: Float,
    var y: Float,
    val type: CoinType,
    var stateTime: Float = 0f  // for animation timing
)
