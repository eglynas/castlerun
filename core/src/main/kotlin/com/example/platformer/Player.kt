package com.example.platformer

import com.badlogic.gdx.math.Rectangle

class Player(
    var x: Float = 100f,
    var y: Float = 100f,
    var width: Float = 137f,
    var height: Float = 151f,
    var health: Int = 3,
    var maxhealth: Int = 5
) {
    var verticalVelocity: Float = 0f
    var isInvincible: Boolean = false
    var invincibilityTimer: Float = 0f
    var flashTimer: Float = 0f
    var isVisible: Boolean = true

    // Attack variables
    var isAttacking: Boolean = false
    var attackTimer: Float = 0f
    val attackDuration: Float = 0.18f

    // Constants related to player movement and physics
    val jumpSpeed = 900f
    val gravity = -2000f
    val fastFallGravity = -8000f
    val groundY = 100f
    val invincibilityTime = 1f
    val flashInterval = 0.1f
    var jumpsDone = 0

    /**
     * Rectangle representing player bounds for collision detection.
     */
    val bounds: Rectangle
        get() = Rectangle(x, y, width, height)

    /**
     * Update player physics like vertical velocity and position.
     * This does NOT handle input directly; input handling goes in the GameplayScreen.
     */
    fun update(delta: Float, isJumpPressed: Boolean, isDownPressed: Boolean) {
        // Detect if player is on ground (with a small epsilon)
        val onGround = y <= groundY + 0.1f

        // Reset jumps counter once on the ground
        if (onGround) {
            jumpsDone = 0
        }

        // Jump logic (allow up to 2 jumps)
        if (isJumpPressed && jumpsDone < 2) {
            verticalVelocity = jumpSpeed
            jumpsDone++
        }

        val effectiveGravity = if (isDownPressed) fastFallGravity else gravity

        verticalVelocity += effectiveGravity * delta
        y += verticalVelocity * delta

        if (y < groundY) {
            y = groundY
            verticalVelocity = 0f
        }

        if (isInvincible) {
            invincibilityTimer += delta
            flashTimer += delta

            if (flashTimer >= flashInterval) {
                isVisible = !isVisible
                flashTimer = 0f
            }

            if (invincibilityTimer >= invincibilityTime) {
                isInvincible = false
                isVisible = true
            }
        }
    }

    /**
     * Called when player takes damage.
     */
    fun onHit() {
        if (!isInvincible) {
            health -= 1
            isInvincible = true
            invincibilityTimer = 0f
            flashTimer = 0f
            isVisible = true
        }
    }

    /**
     * Check if the player is alive.
     */
    fun isAlive(): Boolean = health > 0

    /**
     * Reset player state to initial values.
     */
    fun reset(startX: Float = 100f, startY: Float = groundY, startHealth: Int = 3) {
        x = startX
        y = startY
        health = startHealth
        verticalVelocity = 0f
        isInvincible = false
        invincibilityTimer = 0f
        flashTimer = 0f
        isVisible = true
    }
}
