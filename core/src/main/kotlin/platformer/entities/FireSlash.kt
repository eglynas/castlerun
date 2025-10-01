package platformer.entities

data class FireSlash(
    var x: Float,
    var y: Float,
    var vx: Float = 1500f,
    var damage: Int = 1
)
