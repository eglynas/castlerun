package platformer

enum class CoinType(val value: Int) {
    GOLD(1),
    RUBY(5),
    SAPPHIRE(10)
}

data class Coin(
    var x: Float,
    var y: Float,
    val type: CoinType,
    var stateTime: Float = 0f  // for animation timing
){
    val value: Int
        get() = type.value
}

