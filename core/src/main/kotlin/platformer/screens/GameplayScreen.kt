package platformer.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.ScreenUtils
import platformer.PlatformerGame
import platformer.Player
import platformer.managers.UpgradeManager
import platformer.entities.*
import platformer.managers.*

class GameplayScreen(private val game: PlatformerGame) : Screen {

    private lateinit var batch: SpriteBatch
    private lateinit var camera: OrthographicCamera

    private val player = Player().apply {
        reset(100f, 100f, 3)
    }

    private val upgradeManager = UpgradeManager()

    // Entity managers
    private val fireSlashManager = FireSlashManager()
    private val skeletonManager = SkeletonManager()
    private val batManager = BatManager()
    private val coinManager = CoinManager()
    private val heartManager = HeartManager()
    private val platformManager = PlatformManager()
    private val powerUpManager = PowerUpManager()
    private val rockManager = RockManager()

    private var worldLeftEdge = 0f
    private var gameOverFlag = false
    private var paused = false
    private var isPlayerStandingOnPlatform = false
    private var expGainMultiplier = 1f

    val moveDirection: Int
        get() = when {
            Gdx.input.isKeyPressed(Input.Keys.A) -> -1
            Gdx.input.isKeyPressed(Input.Keys.D) -> 1
            else -> 0
        }

    private val cutoffSpeed = 200f
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
        // Player movement and update
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.x -= player.moveSpeed * delta
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.x += player.moveSpeed * delta
        val isJumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
        val isDownPressed = Gdx.input.isKeyPressed(Input.Keys.S)
        player.update(delta, isJumpPressed, isDownPressed, moveDirection)

        handleShooting()
        handlePlatformCollision()

        // Update world scrolling
        worldLeftEdge += cutoffSpeed * delta
        val halfViewportWidth = camera.viewportWidth / 2f
        val minCameraCenterX = worldLeftEdge + halfViewportWidth
        camera.position.x = (player.x + player.width / 2f).coerceAtLeast(minCameraCenterX)
        camera.position.y = camera.viewportHeight / 2f
        camera.update()

        val playerBounds = player.bounds

        // Update all managers
        platformManager.update(delta, worldLeftEdge, cutoffSpeed, player, camera)

        skeletonManager.update(delta, worldLeftEdge, player.x, camera, playerBounds) {
            onPlayerHit()
        }

        batManager.update(delta, worldLeftEdge, player.x, camera, playerBounds, {
            onPlayerHit()
        }, rockManager)

        rockManager.update(delta, worldLeftEdge, playerBounds) { damage -> onPlayerHit() }

        fireSlashManager.updateWithManagers(
            delta, camera,
            managers = listOf(skeletonManager, batManager), // Add bat manager when you create it
            getTargetsFromManager = { manager ->
                when (manager) {
                    is SkeletonManager -> manager.getAll()
                    is BatManager -> manager.getAll() // When you create BatManager
                    else -> emptyList()
                }
            },
            getBoundsFromManager = { manager, target ->
                when (manager) {
                    is SkeletonManager -> manager.getBounds(target as Skeleton)
                    is BatManager -> manager.getBounds(target as Bat) // When you create BatManager
                    else -> Rectangle(0f, 0f, 0f, 0f)
                }
            },
            onManagerHit = { manager, target, damage ->
                when (manager) {
                    is SkeletonManager -> {
                        val isDead = manager.handleSkeletonHit(target as Skeleton, damage)
                        if (isDead) {
                            game.addXP((10 * expGainMultiplier).toInt())
                            game.saveGlobals()
                        }
                    }
                    is BatManager -> {
                        val isDead = manager.handleBatHit(target as Bat, damage) // When you create BatManager
                        if (isDead) {
                            game.addXP((5 * expGainMultiplier).toInt()) // Different XP for bats
                            game.saveGlobals()
                        }
                    }
                }
            }
        )

        coinManager.update(delta, worldLeftEdge, playerBounds, camera) { coinValue ->
            game.addCoins(coinValue)
            game.saveGlobals()
        }

        heartManager.update(delta, worldLeftEdge, player.x, playerBounds, camera) {
            if (player.health < player.maxhealth) player.health++
        }

        powerUpManager.update(delta, worldLeftEdge, player.x, playerBounds, camera) { powerUpType ->
            activatePowerUp(powerUpType)
        }

        // Keep player in bounds
        player.x = player.x.coerceAtLeast(worldLeftEdge)
    }

    private fun drawGame() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        batch.projectionMatrix = camera.combined
        batch.begin()

        // Draw background
        val bgWidth = AssetsManager.bg.width.toFloat()
        val halfViewportWidth = camera.viewportWidth / 2f
        val viewportLeft = camera.position.x - halfViewportWidth
        val viewportRight = camera.position.x + halfViewportWidth
        var startX = (viewportLeft / bgWidth).toInt() * bgWidth
        while (startX < viewportRight) {
            batch.draw(AssetsManager.bg, startX, 0f, bgWidth, Gdx.graphics.height.toFloat())
            startX += bgWidth
        }

        // Draw player
        val currentPlayerTexture = if (player.isAttacking) AssetsManager.playerAttack else AssetsManager.player
        if (player.isInvincible) {
            batch.setColor(1f, 1f, 1f, if (player.isVisible) 1f else 0.3f)
        } else {
            batch.setColor(1f, 1f, 1f, 1f)
        }
        batch.draw(currentPlayerTexture, player.x, player.y)
        batch.setColor(1f, 1f, 1f, 1f)

        // Draw all entities using managers
        skeletonManager.draw(batch)
        batManager.draw(batch)
        rockManager.draw(batch)
        coinManager.draw(batch)
        platformManager.draw(batch)
        fireSlashManager.draw(batch)
        heartManager.draw(batch)
        powerUpManager.draw(batch)

        // Draw player health
        val heartW = AssetsManager.heart.width.toFloat()
        val heartH = AssetsManager.heart.height.toFloat()
        val spacing = 12f
        val paddingTop = 20f
        val paddingLeft = 20f
        for (i in 0 until player.health) {
            val x = camera.position.x - camera.viewportWidth / 2f + paddingLeft + i * (heartW + spacing)
            val y = camera.position.y + camera.viewportHeight / 2f - paddingTop - heartH
            batch.draw(AssetsManager.heart, x, y)
        }

        batch.end()
    }

    private fun drawPauseScreen() {
        batch.begin()
        AssetsManager.font.color = Color.WHITE
        val text = "PAUSED\nPress Q to quit to menu\nPress P to resume"
        val layout = GlyphLayout(AssetsManager.font, text)
        AssetsManager.font.draw(batch, text, camera.position.x - layout.width / 2, camera.position.y + layout.height / 2)
        batch.end()
    }

    private fun handlePlatformCollision() {
        // Platform collision logic remains the same
        val footHeight = 10f
        val footRect = Rectangle(player.x, player.y, player.width, footHeight)
        var standingOnPlatform: Platform? = null

        for (platform in platformManager.getAll()) {
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
            fireSlashManager.createFireSlash(slashX, slashY)
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
            gameOverFlag = true
        }
    }

    private fun activatePowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.COIN_SPAWN_RATE_BOOST -> {
                val originalInterval = 2.0f / (upgradeManager.getAllUpgrades().firstOrNull { it.name == "Coin Spawn Rate" }?.currentValue ?: 1f)
                val effect = PowerUpEffect(
                    type = type,
                    durationSeconds = 10f,
                    onActivate = { coinManager.updateSpawnRates(0.1f, 15f, 5f) },
                    onDeactivate = {
                        val upgrades = upgradeManager.getAllUpgrades()
                        val ruby = upgrades.firstOrNull { it.name == "Ruby Rate" }?.currentValue ?: 15f
                        val sapphire = upgrades.firstOrNull { it.name == "Sapphire Rate" }?.currentValue ?: 5f
                        coinManager.updateSpawnRates(originalInterval, ruby, sapphire)
                    }
                )
                effect.onActivate()
                powerUpManager.addActivePowerUp(effect)
            }
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

        val coinInterval = 2.0f / (upgrades.firstOrNull { it.name == "Coin Spawn Rate" }?.currentValue ?: 1f)
        val rubyRate = upgrades.firstOrNull { it.name == "Ruby Rate" }?.currentValue ?: 15f
        val sapphireRate = upgrades.firstOrNull { it.name == "Sapphire Rate" }?.currentValue ?: 5f
        coinManager.updateSpawnRates(coinInterval, rubyRate, sapphireRate)

        expGainMultiplier = upgrades.firstOrNull { it.name == "EXP Boost" }?.currentValue ?: 1f
    }

    private fun resetGame() {
        player.reset(100f, groundY, 3)
        worldLeftEdge = 0f
        skeletonManager.reset()
        batManager.reset()
        fireSlashManager.clear()
        gameOverFlag = false
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

    override fun dispose() {
        batch.dispose()
    }
}
