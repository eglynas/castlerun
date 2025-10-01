package platformer.managers

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import kotlin.random.Random
import platformer.entities.Coin
import platformer.entities.CoinType

class CoinManager : EntityManager<Coin>() {
    private var coinSpawnInterval = 2.0f
    private var coinSpawnTimer = 0f
    private var rubySpawnRate = 15f
    private var sapphireSpawnRate = 5f

    fun update(delta: Float, worldLeftEdge: Float, playerBounds: Rectangle, camera: OrthographicCamera,
               onCoinCollected: (Int) -> Unit) {
        // Update coin animations
        getAll().forEach { coin -> coin.stateTime += delta }

        // Handle spawning
        updateCoinSpawning(delta, camera)

        // Remove off-screen coins
        removeAll { coin -> coin.x < worldLeftEdge - 500f }

        // Check collisions with player
        val coinsToRemove = mutableListOf<Coin>()
        getAll().forEach { coin ->
            val animation = when (coin.type) {
                CoinType.GOLD -> AssetsManager.goldCoinAnimation
                CoinType.RUBY -> AssetsManager.rubyCoinAnimation
                CoinType.SAPPHIRE -> AssetsManager.sapphireCoinAnimation
            }
            val frame = animation.getKeyFrame(coin.stateTime, true)
            val coinBounds = Rectangle(coin.x, coin.y, frame.regionWidth.toFloat(), frame.regionHeight.toFloat())

            if (playerBounds.overlaps(coinBounds)) {
                onCoinCollected(coin.value)
                coinsToRemove.add(coin)
            }
        }
        coinsToRemove.forEach { coin -> remove(coin) }
    }

    private fun updateCoinSpawning(delta: Float, camera: OrthographicCamera) {
        coinSpawnTimer += delta
        if (coinSpawnTimer >= coinSpawnInterval) {
            spawnCoin(camera)
            coinSpawnTimer = 0f
        }
    }

    private fun spawnCoin(camera: OrthographicCamera) {
        val spawnX = camera.position.x + camera.viewportWidth / 2f + 150f + Random.nextFloat() * 200f
        val minY = 100f + 40f // groundY + 40f
        val maxY = 100f + 300f // groundY + 300f
        val spawnY = minY + Random.nextFloat() * (maxY - minY)
        add(Coin(spawnX, spawnY, getRandomCoinTypeWithUpgrades()))
    }

    fun draw(batch: SpriteBatch) {
        getAll().forEach { coin ->
            val animation = when (coin.type) {
                CoinType.GOLD -> AssetsManager.goldCoinAnimation
                CoinType.RUBY -> AssetsManager.rubyCoinAnimation
                CoinType.SAPPHIRE -> AssetsManager.sapphireCoinAnimation
            }
            val frame = animation.getKeyFrame(coin.stateTime, true)
            batch.draw(frame, coin.x, coin.y)
        }
    }

    private fun getRandomCoinTypeWithUpgrades(): CoinType {
        val chances = getUpgradedCoinProbabilities()
        val rand = Random.nextFloat() * 100f
        var cumulative = 0f
        for ((type, chance) in chances) {
            cumulative += chance
            if (rand < cumulative) {
                return type
            }
        }
        return CoinType.GOLD
    }

    private fun getUpgradedCoinProbabilities(): List<Pair<CoinType, Float>> {
        val total = rubySpawnRate + sapphireSpawnRate
        val goldChance = if (total < 100f) 100f - total else 0f
        return listOf(
            CoinType.GOLD to goldChance,
            CoinType.RUBY to rubySpawnRate,
            CoinType.SAPPHIRE to sapphireSpawnRate
        )
    }

    fun updateSpawnRates(interval: Float, ruby: Float, sapphire: Float) {
        coinSpawnInterval = interval
        rubySpawnRate = ruby
        sapphireSpawnRate = sapphire
    }
}

