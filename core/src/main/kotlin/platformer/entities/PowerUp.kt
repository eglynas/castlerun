package platformer.entities

class PowerUpPickup(
    var x: Float,
    var y: Float,
    val type: PowerUpType
) {
    var vx: Float = -50f  // Example flying speed

    fun update(delta: Float) {
        x += vx * delta
        // Handle position updates and let collision detection happen externally
    }
}

class PowerUpEffect(
    val type: PowerUpType,
    val durationSeconds: Float,
    val onActivate: () -> Unit,
    val onDeactivate: () -> Unit
) {
    var timeRemaining: Float = durationSeconds
    var active: Boolean = true

    fun update(delta: Float) {
        if (!active) return
        timeRemaining -= delta
        if (timeRemaining <= 0f) {
            active = false
            onDeactivate()
        }
    }
}

enum class PowerUpType {
    COIN_SPAWN_RATE_BOOST
}


