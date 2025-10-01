package platformer.managers

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import kotlin.random.Random
import platformer.entities.Skeleton
import platformer.entities.SkeletonType

class SkeletonManager : EntityManager<Skeleton>() {
    private var nextSkeletonSpawnX = 400f
    var skeletonSpawnChance = 50.0

    fun update(delta: Float, worldLeftEdge: Float, playerX: Float, camera: OrthographicCamera,
               playerBounds: Rectangle, onPlayerHit: () -> Unit) {
        // Handle spawning
        if (playerX > nextSkeletonSpawnX) {
            spawnSkeleton(camera)
        }

        // Remove off-screen skeletons
        removeAll { skeleton -> skeleton.x < worldLeftEdge - 500f }

        // Update existing skeletons
        getAll().forEach { skeleton ->
            skeleton.updateBlink(delta)
            skeleton.x += skeleton.vx * delta

            // Check collision with player
            val texture = when (skeleton.type) {
                SkeletonType.STANDARD -> AssetsManager.skeleton
                SkeletonType.LIGHT -> AssetsManager.skeletonLight
                SkeletonType.GRAY -> AssetsManager.skeletonGray
            }
            val skeletonBounds = Rectangle(skeleton.x, skeleton.y, texture.width.toFloat(), texture.height.toFloat())
            if (playerBounds.overlaps(skeletonBounds)) {
                onPlayerHit()
            }
        }
    }

    private fun spawnSkeleton(camera: OrthographicCamera) {
        if (Random.nextDouble() * 100 <= skeletonSpawnChance) {
            val spawnX = camera.position.x + camera.viewportWidth / 2f + 100f + randomFloat(0f, 200f)
            val type = SkeletonType.entries.random()
            val health = when (type) {
                SkeletonType.STANDARD -> 3
                SkeletonType.LIGHT -> 2
                SkeletonType.GRAY -> 4
            }
            add(
                Skeleton(
                    x = spawnX,
                    y = 100f, // groundY
                    vx = -60f,
                    isLight = type == SkeletonType.LIGHT,
                    type = type,
                    health = health
                )
            )
        }
        nextSkeletonSpawnX += (Random.nextDouble() * 500.0 + 300.0).toFloat()
    }

    fun draw(batch: SpriteBatch) {
        getAll().forEach { skeleton ->
            val texture = when (skeleton.type) {
                SkeletonType.STANDARD -> AssetsManager.skeleton
                SkeletonType.LIGHT -> AssetsManager.skeletonLight
                SkeletonType.GRAY -> AssetsManager.skeletonGray
            }
            batch.setColor(1f, 1f, 1f, if (skeleton.isBlinking) 0.3f else 1f)
            batch.draw(texture, skeleton.x, skeleton.y)
        }
        batch.setColor(1f, 1f, 1f, 1f)
    }

    fun handleSkeletonHit(skeleton: Skeleton, damage: Int): Boolean {
        val isDead = skeleton.takeDamage(damage)
        if (isDead) {
            remove(skeleton)
        }
        return isDead
    }

    private fun randomFloat(min: Float, max: Float): Float {
        return Random.nextFloat() * (max - min) + min
    }

    fun reset() {
        clear()
        nextSkeletonSpawnX = 400f
    }
}

