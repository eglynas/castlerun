package platformer.managers

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import kotlin.random.Random
import platformer.*

class PowerUpManager : EntityManager<PowerUpPickup>() {
    private val activePowerUps = mutableListOf<PowerUpEffect>()
    private var nextPowerUpSpawn = 3000f
    var powerUpSpawnChance = 5.0

    fun update(delta: Float, worldLeftEdge: Float, playerX: Float, playerBounds: Rectangle,
               camera: OrthographicCamera, onPowerUpActivated: (PowerUpType) -> Unit) {
        // Handle spawning
        if (playerX > nextPowerUpSpawn) {
            spawnPowerUp(camera)
            nextPowerUpSpawn += Random.nextFloat() * 1200f + 800f
        }

        // Update pickup movement
        getAll().forEach { it.update(delta) }

        // Update active power-up effects
        activePowerUps.forEach { it.update(delta) }
        activePowerUps.removeAll { !it.active }

        // Remove off-screen pickups
        removeAll { it.x < worldLeftEdge - 500f }

        // Check collisions with player
        val iterator = getAll().iterator()
        while (iterator.hasNext()) {
            val pickup = iterator.next()
            val width = when (pickup.type) {
                PowerUpType.COIN_SPAWN_RATE_BOOST -> Assets.coin_bonus.width.toFloat()
            }
            val height = when (pickup.type) {
                PowerUpType.COIN_SPAWN_RATE_BOOST -> Assets.coin_bonus.height.toFloat()
            }
            val pickupBounds = Rectangle(pickup.x, pickup.y, width, height)

            if (playerBounds.overlaps(pickupBounds)) {
                onPowerUpActivated(pickup.type)
                remove(pickup)
            }
        }
    }

    private fun spawnPowerUp(camera: OrthographicCamera) {
        if (Random.nextDouble() * 100 <= powerUpSpawnChance) {
            val spawnX = camera.position.x + camera.viewportWidth / 2f + 150f + randomFloat(0f, 300f)
            val minY = 100f + 40f // groundY + 40f
            val maxY = 100f + 320f // groundY + 320f
            val spawnY = minY + Random.nextFloat() * (maxY - minY)

            val pickup = PowerUpPickup(spawnX, spawnY, PowerUpType.COIN_SPAWN_RATE_BOOST)
            pickup.vx = -50f
            add(pickup)
        }
    }

    fun draw(batch: SpriteBatch) {
        getAll().forEach { pickup ->
            val texture = when (pickup.type) {
                PowerUpType.COIN_SPAWN_RATE_BOOST -> Assets.coin_bonus
            }
            batch.draw(texture, pickup.x, pickup.y)
        }
    }

    fun addActivePowerUp(powerUp: PowerUpEffect) {
        activePowerUps.add(powerUp)
    }

    private fun randomFloat(min: Float, max: Float): Float {
        return Random.nextFloat() * (max - min) + min
    }
}
