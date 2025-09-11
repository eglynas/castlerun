package com.example.platformer

enum class SkeletonType(val value: Int) {
    STANDARD(0),
    LIGHT(1),
    GRAY(2)
    // Add more as needed, with unique values
}

data class Skeleton(
    var x: Float,
    var y: Float,
    var vx: Float = -60f,
    val isLight: Boolean = false,
    val type: SkeletonType,
    var health: Int = 3,
    var isBlinking: Boolean = false,
    var blinkTimer: Float = 0f,
    val blinkDuration: Float = 0.2f
) {
    fun takeDamage(amount: Int = 1): Boolean {
        health -= amount

        // Trigger blink on hit
        isBlinking = true
        blinkTimer = blinkDuration

        return health <= 0
    }

    fun updateBlink(delta: Float) {
        if (isBlinking) {
            blinkTimer -= delta
            if (blinkTimer <= 0f) {
                isBlinking = false
                blinkTimer = 0f
            }
        }
    }
}


