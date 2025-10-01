package platformer.managers

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import kotlin.random.Random
import platformer.entities.Heart

class HeartManager : EntityManager<Heart>() {
    private var nextHeartSpawnX = 1500f

    fun update(delta: Float, worldLeftEdge: Float, playerX: Float, playerBounds: Rectangle,
               camera: OrthographicCamera, onHeartCollected: () -> Unit) {
        // Handle spawning
        if (playerX > nextHeartSpawnX) {
            spawnHeart(camera)
            nextHeartSpawnX += Random.nextFloat() * 1200f + 800f
        }

        // Remove off-screen hearts
        removeAll { heart -> heart.x < worldLeftEdge - 500f }

        // Check collisions with player
        val iterator = getAll().iterator()
        while (iterator.hasNext()) {
            val heart = iterator.next()
            val heartBounds = Rectangle(heart.x, heart.y, AssetsManager.heart.width.toFloat(), AssetsManager.heart.height.toFloat())
            if (playerBounds.overlaps(heartBounds)) {
                onHeartCollected()
                remove(heart)
            }
        }
    }

    private fun spawnHeart(camera: OrthographicCamera) {
        val spawnX = camera.position.x + camera.viewportWidth / 2f + 150f + randomFloat(0f, 300f)
        val minY = 100f + 40f // groundY + 40f
        val maxY = 100f + 320f // groundY + 320f
        val spawnY = minY + Random.nextFloat() * (maxY - minY)
        add(Heart(spawnX, spawnY))
    }

    fun draw(batch: SpriteBatch) {
        getAll().forEach { heart ->
            batch.draw(AssetsManager.heart, heart.x, heart.y)
        }
    }

    private fun randomFloat(min: Float, max: Float): Float {
        return Random.nextFloat() * (max - min) + min
    }
}
