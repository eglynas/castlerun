package platformer.managers

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import platformer.entities.FireSlash

class FireSlashManager : EntityManager<FireSlash>() {

    fun <M> updateWithManagers(
        delta: Float,
        camera: OrthographicCamera,
        managers: List<M>,
        getTargetsFromManager: (M) -> List<Any>,
        getBoundsFromManager: (M, Any) -> Rectangle,
        onManagerHit: (manager: M, target: Any, damage: Int) -> Unit
    ) {
        val slashesToRemove = mutableListOf<FireSlash>()

        getAll().forEach { slash ->
            slash.x += slash.vx * delta
            val slashBounds = Rectangle(
                slash.x, slash.y,
                AssetsManager.fireSlash.width.toFloat(), AssetsManager.fireSlash.height.toFloat()
            )

            var hitDetected = false

            // Check collision with all managers' targets
            for (manager in managers) {
                if (hitDetected) break

                val targets = getTargetsFromManager(manager)
                val hitTarget = targets.firstOrNull { target ->
                    slashBounds.overlaps(getBoundsFromManager(manager, target))
                }

                if (hitTarget != null) {
                    val damage = slash.damage.takeIf { it > 0 } ?: 1
                    onManagerHit(manager, hitTarget, damage)
                    slashesToRemove.add(slash)
                    hitDetected = true
                }
            }

            // Remove if
            if (!hitDetected && slash.x > camera.position.x + camera.viewportWidth / 2f + 100f) {
                slashesToRemove.add(slash)
            }
        }

        // Remove slashes safely
        slashesToRemove.forEach { slash -> remove(slash) }
    }

    fun draw(batch: SpriteBatch) {
        getAll().forEach { slash ->
            batch.draw(AssetsManager.fireSlash, slash.x, slash.y)
        }
    }

    fun createFireSlash(x: Float, y: Float) {
        add(FireSlash(x, y))
    }
}
