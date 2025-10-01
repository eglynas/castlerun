package platformer.entities

enum class BatType(val value: Int) {
    BLACK(1),
    BROWN(2)
    // Add more as needed, with unique values
}

data class Bat(
    var x: Float,
    var y: Float,
    var vx: Float,
    var health: Int,
    val type: BatType,
    val isBlack: Boolean,
    var isBlinking: Boolean = false,
    var blinkTimer: Float = 0f,
    var attackCooldown: Float = 3f, // Time between attacks
    var attackTimer: Float = 0f,    // Current cooldown timer
    var hasLineOfSight: Boolean = false // Track if player is below
) {
    fun takeDamage(damage: Int): Boolean {
        health -= damage
        if (health > 0) {
            isBlinking = true
            blinkTimer = 0.5f
        }
        return health <= 0
    }

    fun updateBlink(delta: Float) {
        if (isBlinking) {
            blinkTimer -= delta
            if (blinkTimer <= 0f) {
                isBlinking = false
            }
        }
    }

    fun updateAttackTimer(delta: Float) {
        if (attackTimer > 0f) {
            attackTimer -= delta
        }
    }

    fun canAttack(): Boolean = attackTimer <= 0f

    fun resetAttackCooldown() {
        attackTimer = attackCooldown
    }
}



