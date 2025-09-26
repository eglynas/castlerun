package platformer

import com.badlogic.gdx.math.Rectangle

class Player(
    var x: Float = 100f,
    var y: Float = 100f,
    var width: Float = 135f,
    var height: Float = 150f,
    var health: Int = 3,
    var maxhealth: Int = 5
) {
    var maxJumps: Int = 2
    var attackCooldown: Float = 1f
    var moveSpeed: Float = 200f

    var verticalVelocity: Float = 0f
    var isInvincible: Boolean = false
    var invincibilityTimer: Float = 0f
    var flashTimer: Float = 0f
    var isVisible: Boolean = true

    var isAttacking: Boolean = false
    var attackTimer: Float = 0f

    val jumpSpeed = 900f
    val gravity = -2000f
    val fastFallGravity = -8000f
    val groundY = 100f
    val invincibilityTime = 1f
    val flashInterval = 0.1f
    var jumpsDone = 0

    val bounds: Rectangle
        get() = Rectangle(x, y, width, height)

    fun update(delta: Float, isJumpPressed: Boolean, isDownPressed: Boolean, moveDirection: Int) {
        val onGround = y <= groundY + 0.1f
        if (onGround) jumpsDone = 0

        if (isJumpPressed && jumpsDone < maxJumps) {
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

        x += moveDirection * moveSpeed * delta

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

        if (attackTimer > 0f) {
            attackTimer -= delta
            if (attackTimer <= 0f) {
                attackTimer = 0f
                isAttacking = false
            }
        }
    }

    fun applyUpgrades(maxJumps: Int, attackCooldown: Float, moveSpeed: Float, maxHealth: Int) {
        this.maxJumps = maxJumps
        this.attackCooldown = attackCooldown
        this.moveSpeed = moveSpeed
        this.maxhealth = maxHealth
        if (health > maxHealth) health = maxHealth
    }

    fun onHit() {
        if (!isInvincible) {
            health -= 1
            isInvincible = true
            invincibilityTimer = 0f
            flashTimer = 0f
            isVisible = true
        }
    }

    fun isAlive(): Boolean = health > 0

    fun reset(startX: Float = 100f, startY: Float = groundY, startHealth: Int = 3) {
        x = startX
        y = startY
        health = startHealth
        verticalVelocity = 0f
        isInvincible = false
        invincibilityTimer = 0f
        flashTimer = 0f
        isVisible = true
        jumpsDone = 0
        isAttacking = false
        attackTimer = 0f
    }
}
