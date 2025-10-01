package platformer.entities

data class Rock(
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 350f, // Falling speed (positive = downward)
    var damage: Int = 1
)
