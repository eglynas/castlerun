package platformer

data class FireSlash(
    var x: Float,
    var y: Float,
    var vx: Float = 1000f,
    var damage: Int = 1
)
