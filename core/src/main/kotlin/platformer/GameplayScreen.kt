package platformer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.random.Random

class GameplayScreen(private val game: PlatformerGame) : Screen {

    private lateinit var batch: SpriteBatch
    private lateinit var camera: OrthographicCamera

    private val player = Player().apply {
        reset(100f, 100f, 3)  // Use positional args, floats with 'f' suffix
    }

    private val upgradeManager = UpgradeManager()

    private val fireSlashes = mutableListOf<FireSlash>()
    private val skeletons = mutableListOf<Skeleton>()
    private val coins = mutableListOf<Coin>()
    private val hearts = mutableListOf<Heart>()
    private val platforms = mutableListOf<Platform>()
    private val activePowerUps = mutableListOf<PowerUpEffect>()
    private val powerUpPickups = mutableListOf<PowerUpPickup>()

    private var chunksSpawned = 0
    private var platformSpawnTimer = 0f
    private val platformSpawnDelay = 0f

    private var worldLeftEdge = 0f

    private var gameOverFlag = false
    private var paused = false
    private var isPlayerStandingOnPlatform = false

    private var coinSpawnInterval = 2.0f
    private var coinSpawnTimer: Float = 0f
    private var rubySpawnRate = 15f
    private var sapphireSpawnRate = 5f
    private var expGainMultiplier = 1f

    private var nextHeartSpawnX = 1500f
    private var nextSkeletonSpawnX = 400f
    private var nextPowerUpSpawn = 3000f
    private var nextChunkSpawnX = 0f

    var skeletonSpawnChance = 50.0
    var powerUpSpawnChance = 5.0

    val moveDirection: Int
        get() = when {
            Gdx.input.isKeyPressed(Input.Keys.A) -> -1
            Gdx.input.isKeyPressed(Input.Keys.D) -> 1
            else -> 0
        }

    private val cutoffSpeed = 150f
    private val groundY = 100f

    override fun show() {
        batch = SpriteBatch()
        camera = OrthographicCamera()
        camera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        if (!upgradeManager.hasSavedUpgrades()) {
            upgradeManager.initializeUpgrades()
            upgradeManager.saveUpgrades()
        } else {
            upgradeManager.initializeUpgrades()
            upgradeManager.loadUpgrades()
        }

        applyUpgradesToGame()
        resetGame()
    }
    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) paused = !paused

        if (!paused && !gameOverFlag) updateGame(delta)

        drawGame()

        if (paused) {
            drawPauseScreen()
            handlePauseInput()
        } else if (gameOverFlag) {
            game.setScreen(GameOverScreen(game))
            dispose()
        }
    }

    private fun updateGame(delta: Float) {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.x -= player.moveSpeed * delta
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.x += player.moveSpeed * delta
        val isJumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
        val isDownPressed = Gdx.input.isKeyPressed(Input.Keys.S)
        player.update(delta, isJumpPressed, isDownPressed, moveDirection)
        handleShooting()
        handlePlatformCollision()


        //PLATFORMS
        platforms.forEach { it.x -= cutoffSpeed * delta }
        platforms.removeAll { it.x + it.width < worldLeftEdge - 300f }
        platformSpawnTimer += delta
        if (platformSpawnTimer >= platformSpawnDelay) {
            if (player.x + player.width > nextChunkSpawnX) {
                spawnNextPlatformChunk()
                platformSpawnTimer = 0f
            }
        }

        worldLeftEdge += cutoffSpeed * delta

        val halfViewportWidth = camera.viewportWidth / 2f
        val minCameraCenterX = worldLeftEdge + halfViewportWidth
        camera.position.x = (player.x + player.width / 2f).coerceAtLeast(minCameraCenterX)
        camera.position.y = camera.viewportHeight / 2f
        camera.update()
        val playerBounds = player.bounds

        //SKELETONS
        if (player.x > nextSkeletonSpawnX) {
            spawnSkeleton()
        }
        skeletons.removeAll { it.x < worldLeftEdge - 500f }
        skeletons.forEach { it.updateBlink(delta) }
        val skeletonIterator = skeletons.iterator()
        while (skeletonIterator.hasNext()) {
            val skeleton = skeletonIterator.next()
            skeleton.x += skeleton.vx * delta
            val texture = when (skeleton.type) {
                SkeletonType.STANDARD -> Assets.skeleton
                SkeletonType.LIGHT -> Assets.skeletonLight
                SkeletonType.GRAY -> Assets.skeletonGray
            }
            val skeletonBounds = Rectangle(skeleton.x, skeleton.y, texture.width.toFloat(), texture.height.toFloat())
            if (playerBounds.overlaps(skeletonBounds)) onPlayerHit()
        }

        //FIRE SLASHES
        val slashIterator = fireSlashes.iterator()
        while (slashIterator.hasNext()) {
            val slash = slashIterator.next()
            slash.x += slash.vx * delta
            val slashBounds = Rectangle(slash.x, slash.y, Assets.fireSlash.width.toFloat(), Assets.fireSlash.height.toFloat())

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
                val isDead = hitSkeleton.takeDamage(damage)
                if (isDead) skeletons.remove(hitSkeleton)
                slashIterator.remove()
                game.addXP((10 * expGainMultiplier).toInt())
                game.saveGlobals()
                continue
            }

            if (slash.x > camera.position.x + camera.viewportWidth / 2f + 100f) slashIterator.remove()
        }

        //COINS
        coins.forEach { it.stateTime += delta }
        updateCoinSpawning(delta)
        coins.removeAll { it.x < worldLeftEdge - 500f }
        val coinIterator = coins.iterator()
        while (coinIterator.hasNext()) {
            val coin = coinIterator.next()
            val animation = when (coin.type) {
                CoinType.GOLD -> Assets.goldCoinAnimation
                CoinType.RUBY -> Assets.rubyCoinAnimation
                CoinType.SAPPHIRE -> Assets.sapphireCoinAnimation
            }
            val frame = animation.getKeyFrame(coin.stateTime, true)
            val coinBounds = Rectangle(coin.x, coin.y, frame.regionWidth.toFloat(), frame.regionHeight.toFloat())
            if (playerBounds.overlaps(coinBounds)) {
                coinIterator.remove()
                val coinValue = coin.value  // Get the coin value from the enum
                game.addCoins(coinValue)
                game.saveGlobals()
            }
        }

        //HEARTS
        if (player.x > nextHeartSpawnX) {
            spawnHeart()
            nextHeartSpawnX += Random.nextFloat() * 1200f + 800f
        }
        hearts.removeAll { it.x < worldLeftEdge - 500f }
        val heartIterator = hearts.iterator()
        while (heartIterator.hasNext()) {
            val heart = heartIterator.next()
            val heartBounds = Rectangle(heart.x, heart.y, Assets.heart.width.toFloat(), Assets.heart.height.toFloat())
            if (playerBounds.overlaps(heartBounds)) {
                if (player.health < player.maxhealth) player.health++
                heartIterator.remove()
            }
        }

        //POWER UPS
        if (player.x > nextPowerUpSpawn) {
            spawnPowerUp()
            nextPowerUpSpawn += Random.nextFloat() * 1200f + 800f
        }
        powerUpPickups.forEach { it.update(delta) }
        activePowerUps.forEach { it.update(delta) }
        activePowerUps.removeAll { !it.active }
        val iterator = powerUpPickups.iterator()
        while (iterator.hasNext()) {
            val pickup = iterator.next()
            val width = when (pickup.type) {
                PowerUpType.COIN_SPAWN_RATE_BOOST -> Assets.coin_bonus.width.toFloat()
                // Add other power-up types here
            }
            val height = when (pickup.type) {
                PowerUpType.COIN_SPAWN_RATE_BOOST -> Assets.coin_bonus.height.toFloat()
                // Add other power-up types here
            }
            val pickupBounds = Rectangle(pickup.x, pickup.y, width, height)
            if (playerBounds.overlaps(pickupBounds)) {
                iterator.remove()   // safely remove from the list while iterating
                activatePowerUp(pickup.type)
            }
        }
        player.x = player.x.coerceAtLeast(worldLeftEdge)
    }

    private fun drawGame() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        batch.projectionMatrix = camera.combined
        batch.begin()

        val bgWidth = Assets.bg.width.toFloat()
        val halfViewportWidth = camera.viewportWidth / 2f
        val viewportLeft = camera.position.x - halfViewportWidth
        val viewportRight = camera.position.x + halfViewportWidth
        var startX = (viewportLeft / bgWidth).toInt() * bgWidth
        while (startX < viewportRight) {
            batch.draw(Assets.bg, startX, 0f, bgWidth, Gdx.graphics.height.toFloat())
            startX += bgWidth
        }

        val currentPlayerTexture = if (player.isAttacking) Assets.playerAttack else Assets.player

        if (player.isInvincible) {
            batch.setColor(1f, 1f, 1f, if (player.isVisible) 1f else 0.3f)
        } else {
            batch.setColor(1f, 1f, 1f, 1f)
        }
        batch.draw(currentPlayerTexture, player.x, player.y)
        batch.setColor(1f, 1f, 1f, 1f)

        for (skeleton in skeletons) {
            val texture = when (skeleton.type) {
                SkeletonType.STANDARD -> Assets.skeleton
                SkeletonType.LIGHT -> Assets.skeletonLight
                SkeletonType.GRAY -> Assets.skeletonGray
            }
            batch.setColor(1f, 1f, 1f, if (skeleton.isBlinking) 0.3f else 1f)
            batch.draw(texture, skeleton.x, skeleton.y)
        }
        batch.setColor(1f, 1f, 1f, 1f)

        for (coin in coins) {
            val animation = when (coin.type) {
                CoinType.GOLD -> Assets.goldCoinAnimation
                CoinType.RUBY -> Assets.rubyCoinAnimation
                CoinType.SAPPHIRE -> Assets.sapphireCoinAnimation
            }
            val frame = animation.getKeyFrame(coin.stateTime, true)
            batch.draw(frame, coin.x, coin.y)
        }

        for (platform in platforms) {
            batch.draw(Assets.platform, platform.x, platform.y)
        }

        for (slash in fireSlashes) {
            batch.draw(Assets.fireSlash, slash.x, slash.y)
        }

        for (heart in hearts) {
            batch.draw(Assets.heart, heart.x, heart.y)
        }

        powerUpPickups.forEach { pickup ->
            val texture = when (pickup.type) {
                PowerUpType.COIN_SPAWN_RATE_BOOST -> Assets.coin_bonus
                // Add other power-up types here if needed
            }
            batch.draw(texture, pickup.x, pickup.y)
        }

        // Draw player's health icons at top left
        val heartW = Assets.heart.width.toFloat()
        val heartH = Assets.heart.height.toFloat()
        val spacing = 12f
        val paddingTop = 20f
        val paddingLeft = 20f
        for (i in 0 until player.health) {
            val x = camera.position.x - camera.viewportWidth / 2f + paddingLeft + i * (heartW + spacing)
            val y = camera.position.y + camera.viewportHeight / 2f - paddingTop - heartH
            batch.draw(Assets.heart, x, y)
        }

        batch.end()
    }

    private fun drawPauseScreen() {
        batch.begin()
        Assets.font.color = Color.WHITE
        val text = "PAUSED\nPress Q to quit to menu\nPress P to resume"
        val layout = GlyphLayout(Assets.font, text)
        Assets.font.draw(batch, text, camera.position.x - layout.width / 2, camera.position.y + layout.height / 2)
        batch.end()
    }
    // SPAWN
    private fun spawnNextPlatformChunk() {
        val cameraRightEdge = camera.position.x + camera.viewportWidth / 2f
        val chunk = PlatformPatterns.getRandomPattern()
        val chunkOffsetX = maxOf(nextChunkSpawnX, cameraRightEdge + 300f)

        for (plat in chunk) {
            val platY = maxOf(plat.y, groundY + Assets.platform.height + 50f)
            platforms.add(Platform(chunkOffsetX + plat.x, platY, plat.width, plat.height))
        }
        nextChunkSpawnX = chunkOffsetX + chunk.maxOf { it.x + it.width }
        chunksSpawned++
    }
    private fun spawnSkeleton() {
        if (Random.nextDouble() * 100 <= skeletonSpawnChance) {
            val spawnX = camera.position.x + camera.viewportWidth / 2f + 100f + randomFloat(0f, 200f)
            val type = SkeletonType.entries.random()
            val health = when (type) {
                SkeletonType.STANDARD -> 3
                SkeletonType.LIGHT -> 2
                SkeletonType.GRAY -> 4
            }
            skeletons.add(
                Skeleton(
                    x = spawnX,
                    y = groundY,
                    vx = -60f,
                    isLight = type == SkeletonType.LIGHT,
                    type = type,
                    health = health
                )
            )
        }
        // Update the next spawn X regardless of whether a skeleton was spawned
        nextSkeletonSpawnX += (Random.nextDouble() * 500.0 + 300.0).toFloat()
    }
    fun spawnCoin(rubyRate: Float, sapphireRate: Float) {
        val spawnX = camera.position.x + camera.viewportWidth / 2f + 150f + Random.nextFloat() * 200f
        val minY = groundY + 40f
        val maxY = groundY + 300f
        val spawnY = minY + Random.nextFloat() * (maxY - minY)
        coins.add(Coin(spawnX, spawnY, getRandomCoinTypeWithUpgrades(rubyRate, sapphireRate)))
    }
    private fun spawnHeart() {
        val spawnX = camera.position.x + camera.viewportWidth / 2f + 150f + randomFloat(0f, 300f)
        val minY = groundY + 40f
        val maxY = groundY + 320f
        val spawnY = minY + Random.nextFloat() * (maxY - minY)
        hearts.add(Heart(spawnX, spawnY))
    }
    private fun spawnPowerUp() {
        if (Random.nextDouble() * 100 <= powerUpSpawnChance) {
            val spawnX = camera.position.x + camera.viewportWidth / 2f + 150f + randomFloat(0f, 300f)
            val minY = groundY + 40f
            val maxY = groundY + 320f
            val spawnY = minY + Random.nextFloat() * (maxY - minY)

            val pickup = PowerUpPickup(spawnX, spawnY, PowerUpType.COIN_SPAWN_RATE_BOOST)
            pickup.vx = -50f  // Flying speed to the left
            powerUpPickups.add(pickup)
        }
    }


    //HANDLE
    private fun handlePlatformCollision() {
        val footHeight = 10f
        val footRect = Rectangle(player.x, player.y, player.width, footHeight)

        var standingOnPlatform: Platform? = null

        for (platform in platforms) {
            if (platform.y <= groundY) continue

            val horizontallyAligned = footRect.x + footRect.width > platform.x &&
                footRect.x < platform.x + platform.width

            val platformTop = platform.y + platform.height
            val playerFeetY = player.y

            val feetCloseToPlatform = playerFeetY >= platformTop - footHeight &&
                playerFeetY <= platformTop + 2f

            if (horizontallyAligned && player.verticalVelocity <= 0f && feetCloseToPlatform) {
                standingOnPlatform = platform
                break
            }
        }

        if (standingOnPlatform != null) {
            if (Gdx.input.isKeyPressed(Input.Keys.S) && player.y > groundY + 5f) {
                isPlayerStandingOnPlatform = false
            } else {
                player.y = standingOnPlatform.y + standingOnPlatform.height
                player.verticalVelocity = 0f
                player.jumpsDone = 0
                isPlayerStandingOnPlatform = true
            }
        } else {
            if (player.y < groundY) {
                player.y = groundY
                player.verticalVelocity = 0f
                player.jumpsDone = 0
                isPlayerStandingOnPlatform = true
            } else {
                isPlayerStandingOnPlatform = false
            }
        }
    }
    private fun handleShooting() {
        if ((Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.E)) && player.attackTimer <= 0f) {
            val slashX = player.x + 50f
            val slashY = player.y + 50f
            fireSlashes.add(FireSlash(slashX, slashY))
            player.isAttacking = true
            player.attackTimer = player.attackCooldown
        }
    }
    private fun handlePauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            game.setScreen(MainMenuScreen(game))
            dispose()
        }
    }
    private fun onPlayerHit() {
        player.onHit()
        if (!player.isAlive()) {
            println("GAME OVER!")
            gameOverFlag = true
        }
    }
    //MISC
    private fun activatePowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.COIN_SPAWN_RATE_BOOST -> {
                val originalInterval = coinSpawnInterval
                val effect = PowerUpEffect(
                    type = type,
                    durationSeconds = 10f,
                    onActivate = { coinSpawnInterval = 0.1f },
                    onDeactivate = { coinSpawnInterval = originalInterval }
                )
                effect.onActivate()
                activePowerUps.add(effect)
            }
            // Handle other power-up types similarly
        }
    }

    private fun applyUpgradesToGame() {
        val upgrades = upgradeManager.getAllUpgrades()
        player.applyUpgrades(
            maxJumps = upgrades.firstOrNull { it.name == "Jump Count" }?.currentValue?.toInt() ?: 2,
            attackCooldown = upgrades.firstOrNull { it.name == "Attack Speed" }?.currentValue ?: 1f,
            moveSpeed = upgrades.firstOrNull { it.name == "Moving Speed" }?.currentValue ?: 200f,
            maxHealth = upgrades.firstOrNull { it.name == "Max Health" }?.currentValue?.toInt() ?: 5
        )
        coinSpawnInterval = 2.0f / (upgrades.firstOrNull { it.name == "Coin Spawn Rate" }?.currentValue ?: 1f)
        rubySpawnRate = upgrades.firstOrNull { it.name == "Ruby Rate" }?.currentValue ?: 15f
        sapphireSpawnRate = upgrades.firstOrNull { it.name == "Sapphire Rate" }?.currentValue ?: 5f
        expGainMultiplier = upgrades.firstOrNull { it.name == "EXP Boost" }?.currentValue ?: 1f
    }
    private fun randomFloat(min: Float, max: Float): Float {
        return Random.nextFloat() * (max - min) + min
    }
    fun getRandomCoinTypeWithUpgrades(rubyRate: Float, sapphireRate: Float): CoinType {
        val chances = getUpgradedCoinProbabilities(rubyRate, sapphireRate)
        val rand = Random.nextFloat() * 100f
        var cumulative = 0f
        for ((type, chance) in chances) {
            cumulative += chance
            if (rand < cumulative) {
                return type
            }
        }
        return CoinType.GOLD // fallback, should not normally be reached if chances sum to 100
    }
    fun getUpgradedCoinProbabilities(rubyRate: Float, sapphireRate: Float): List<Pair<CoinType, Float>> {
        val total = rubyRate + sapphireRate
        val goldChance = if (total < 100f) 100f - total else 0f
        return listOf(
            CoinType.GOLD to goldChance,
            CoinType.RUBY to rubyRate,
            CoinType.SAPPHIRE to sapphireRate
        )
    }
    fun updateCoinSpawning(delta: Float) {
        coinSpawnTimer += delta
        if (coinSpawnTimer >= coinSpawnInterval) {
            spawnCoin(rubySpawnRate, sapphireSpawnRate)
            coinSpawnTimer = 0f
        }
    }
    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0f)
        camera.update()
    }
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    private fun resetGame() {
        player.reset(100f, groundY, 3)
        worldLeftEdge = 0f
        skeletons.clear()
        fireSlashes.clear()
        nextSkeletonSpawnX = 400f
        gameOverFlag = false
    }
    override fun dispose() {
        batch.dispose()
    }
}
