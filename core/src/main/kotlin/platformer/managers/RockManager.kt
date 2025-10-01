package platformer.managers

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import platformer.entities.Rock

class RockManager : EntityManager<Rock>() {

    fun update(delta: Float, worldLeftEdge: Float, playerBounds: Rectangle, onPlayerHit: (Int) -> Unit) {
        val rocksToRemove = mutableListOf<Rock>()

        getAll().forEach { rock ->
            // Update rock position (falling down)
            rock.y -= rock.vy * delta
            rock.x += rock.vx * delta

            val rockBounds = Rectangle(
                rock.x, rock.y,
                AssetsManager.rock.width.toFloat(), // Add rock texture to AssetsManager
                AssetsManager.rock.height.toFloat()
            )

            // Check collision with player
            if (playerBounds.overlaps(rockBounds)) {
                onPlayerHit(rock.damage)
                rocksToRemove.add(rock)
            }

            // Remove if rock hits ground or goes
            else if (rock.y <= 100f || rock.x < worldLeftEdge - 100f) { // 100f = groundY
                rocksToRemove.add(rock)
            }
        }

        // Remove rocks safely
        rocksToRemove.forEach { rock -> remove(rock) }
    }

    fun draw(batch: SpriteBatch) {
        getAll().forEach { rock ->
            batch.draw(AssetsManager.rock, rock.x, rock.y)
        }
    }

    fun dropRock(x: Float, y: Float, damage: Int = 1) {
        add(Rock(x, y, damage = damage))
    }
}
