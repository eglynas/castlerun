package platformer.managers

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import platformer.*

class FireSlashManager : EntityManager<FireSlash>() {

    fun update(delta: Float, camera: OrthographicCamera, skeletons: List<Skeleton>,
               onSkeletonHit: (Skeleton, Int) -> Unit) {
        val iterator = getAll().iterator()
        while (iterator.hasNext()) {
            val slash = iterator.next()
            slash.x += slash.vx * delta
            val slashBounds = Rectangle(slash.x, slash.y, Assets.fireSlash.width.toFloat(), Assets.fireSlash.height.toFloat())

            // Check collision with skeletons
            val hitSkeleton = skeletons.firstOrNull { skeleton ->
                val textureRef = when (skeleton.type) {
                    SkeletonType.STANDARD -> Assets.skeleton
                    SkeletonType.LIGHT -> Assets.skeletonLight
                    SkeletonType.GRAY -> Assets.skeletonGray
                }
                val skeletonBounds = Rectangle(skeleton.x, skeleton.y, textureRef.width.toFloat(), textureRef.height.toFloat())
                slashBounds.overlaps(skeletonBounds)
            }

            if (hitSkeleton != null) {
                val damage = slash.damage.takeIf { it > 0 } ?: 1
                onSkeletonHit(hitSkeleton, damage)
                remove(slash)
                continue
            }

            // Remove if off screen
            if (slash.x > camera.position.x + camera.viewportWidth / 2f + 100f) {
                remove(slash)
            }
        }
    }

    fun draw(batch: SpriteBatch) {
        getAll().forEach { slash ->
            batch.draw(Assets.fireSlash, slash.x, slash.y)
        }
    }

    fun createFireSlash(x: Float, y: Float) {
        add(FireSlash(x, y))
    }
}
