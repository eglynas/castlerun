package com.example.platformer

enum class CoinType(val value: Int) {
    GOLD(1),
    RUBY(5),
    SAPPHIRE(10)
    // Add more as needed
}

data class Coin(
    var x: Float,
    var y: Float,
    val type: CoinType,
    var stateTime: Float = 0f  // <-- new field
)
