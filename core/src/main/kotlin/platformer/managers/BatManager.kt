package platformer.managers

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import kotlin.random.Random
import platformer.entities.Bat
import platformer.entities.BatType

class BatManager : EntityManager<Bat>() {
    private var nextBatSpawnX = 400f
    var batSpawnChance = 50.0

    fun update(delta: Float, worldLeftEdge: Float, playerX: Float, camera: OrthographicCamera,
               playerBounds: Rectangle, onPlayerHit: () -> Unit, rockManager: RockManager) {
        // Handle spawning
        if (playerX > nextBatSpawnX) {
            spawnBat(camera)
        }

        // Remove off-screen bats
        removeAll { bat -> bat.x < worldLeftEdge - 500f }

        // Update existing bats
        getAll().forEach { bat ->
            bat.updateBlink(delta)
            bat.updateAttackTimer(delta)
            bat.x += bat.vx * delta

            // Check if player is horizontally aligned (within attack range on X-axis)
            val horizontalDistance = kotlin.math.abs(bat.x - playerX)
            val isPlayerBelow = playerBounds.y < bat.y
            val isHorizontallyAligned = horizontalDistance <= 50f // Adjust range as needed

            bat.hasLineOfSight = isPlayerBelow && isHorizontallyAligned

            // Attack logic: drop rock if conditions are met
            if (bat.hasLineOfSight && bat.canAttack()) {
                rockManager.dropRock(bat.x, bat.y - 20f, damage = 1) // Drop from bat position
                bat.resetAttackCooldown()
            }

            // Check collision with player (direct contact)
            if (playerBounds.overlaps(getBounds(bat))) {
                onPlayerHit()
            }
        }
    }

    fun getBounds(bat: Bat): Rectangle {
        val texture = when (bat.type) {
            BatType.BLACK -> AssetsManager.bat_black
            BatType.BROWN -> AssetsManager.bat_brown
        }
        return Rectangle(bat.x, bat.y, texture.width.toFloat(), texture.height.toFloat())
    }

    private fun spawnBat(camera: OrthographicCamera) {
        if (Random.nextDouble() * 100 <= batSpawnChance) {
            val spawnX = camera.position.x + camera.viewportWidth / 2f + 100f + randomFloat(0f, 200f)
            val spawnY = randomFloat(500f, 650f) // Using your existing helper function

            val type = BatType.entries.random()
            val health = when (type) {
                BatType.BLACK -> 3
                BatType.BROWN -> 4
            }
            add(
                Bat(
                    x = spawnX,
                    y = spawnY, // Random Y between 500f and 800f
                    vx = -60f,
                    isBlack = type == BatType.BLACK,
                    type = type,
                    health = health
                )
            )
        }
        nextBatSpawnX += (Random.nextDouble() * 500.0 + 300.0).toFloat()
    }


    fun draw(batch: SpriteBatch) {
        getAll().forEach { bat ->
            val texture = when (bat.type) {
                BatType.BLACK -> AssetsManager.bat_black
                BatType.BROWN -> AssetsManager.bat_brown
            }
            batch.setColor(1f, 1f, 1f, if (bat.isBlinking) 0.3f else 1f)
            batch.draw(texture, bat.x, bat.y)
        }
        batch.setColor(1f, 1f, 1f, 1f)
    }

    fun handleBatHit(bat: Bat, damage: Int): Boolean {
        val isDead = bat.takeDamage(damage)
        if (isDead) {
            remove(bat)
        }
        return isDead
    }

    private fun randomFloat(min: Float, max: Float): Float {
        return Random.nextFloat() * (max - min) + min
    }

    fun reset() {
        clear()
        nextBatSpawnX = 400f
    }
}
