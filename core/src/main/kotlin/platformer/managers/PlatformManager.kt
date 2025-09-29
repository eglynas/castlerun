package platformer.managers

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import platformer.*

class PlatformManager : EntityManager<Platform>() {
    private var nextChunkSpawnX = 0f
    private var chunksSpawned = 0
    private var platformSpawnTimer = 0f
    private val platformSpawnDelay = 0f

    fun update(delta: Float, worldLeftEdge: Float, cutoffSpeed: Float, player: Player, camera: OrthographicCamera) {
        // Move platforms with cutoff speed
        getAll().forEach { platform -> platform.x -= cutoffSpeed * delta }

        // Remove off-screen platforms
        removeAll { platform -> platform.x + platform.width < worldLeftEdge - 300f }

        // Handle spawning
        platformSpawnTimer += delta
        if (platformSpawnTimer >= platformSpawnDelay) {
            if (player.x + player.width > nextChunkSpawnX) {
                spawnNextPlatformChunk(camera)
                platformSpawnTimer = 0f
            }
        }
    }

    private fun spawnNextPlatformChunk(camera: OrthographicCamera) {
        val cameraRightEdge = camera.position.x + camera.viewportWidth / 2f
        val chunk = PlatformPatterns.getRandomPattern()
        val chunkOffsetX = maxOf(nextChunkSpawnX, cameraRightEdge + 300f)
        val groundY = 100f

        for (plat in chunk) {
            val platY = maxOf(plat.y, groundY + Assets.platform.height + 50f)
            add(Platform(chunkOffsetX + plat.x, platY, plat.width, plat.height))
        }
        nextChunkSpawnX = chunkOffsetX + chunk.maxOf { it.x + it.width }
        chunksSpawned++
    }

    fun draw(batch: SpriteBatch) {
        getAll().forEach { platform ->
            batch.draw(Assets.platform, platform.x, platform.y)
        }
    }
}
