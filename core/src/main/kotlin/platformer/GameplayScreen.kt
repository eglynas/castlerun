package platformer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.random.Random

class GameplayScreen(private val game: PlatformerGame) : Screen {

    private lateinit var batch: SpriteBatch
    private lateinit var bg: Texture
    private lateinit var playerTexture: Texture
    private lateinit var playerAttackTexture: Texture
    private lateinit var skeletonTexture: Texture
    private lateinit var skeletonLightTexture: Texture
    private lateinit var skeletonGrayTexture: Texture
    private lateinit var fireSlashTexture: Texture
    private lateinit var heartTexture: Texture

    // Use your Player class!
    private val player = Player().apply {
        reset(startX = 100f, startY = groundY, startHealth = 3)
    }

    // Add textures for each coin type if needed
    private lateinit var goldCoinTexture: Texture
    private lateinit var rubyCoinTexture: Texture
    private lateinit var sapphireCoinTexture: Texture

    private lateinit var goldCoinAnimation: Animation<TextureRegion>
    private lateinit var rubyCoinAnimation: Animation<TextureRegion>
    private lateinit var sapphireCoinAnimation: Animation<TextureRegion>

    private lateinit var platformTexture: Texture

    private lateinit var font: BitmapFont
    private lateinit var camera: OrthographicCamera

    private val cutoffSpeed = 150f
    private val playerSpeed = 800f
    private val groundY = 100f

    // Mutable Lists
    private val fireSlashes = mutableListOf<FireSlash>()
    private val skeletons = mutableListOf<Skeleton>()
    private val coins = mutableListOf<Coin>()
    private val hearts = mutableListOf<Heart>()
    private val platforms = mutableListOf<Platform>()

    // Chunks variables
    private var nextChunkSpawnX = 0f // Tracks where to place the next chunk
    private var chunksSpawned = 0           // Tracks how many chunks have been (attempted) spawned
    private var platformSpawnTimer = 0f     // Timer for spawn delay
    private val platformSpawnDelay = 0f   // Minimum seconds between platform spawns

    private var nextSkeletonSpawnX = 400f
    private var nextCoinSpawnX = 400f
    private var nextHeartSpawnX = 1500f
    private var worldLeftEdge = 0f

    private var gameOverFlag = false
    private var paused = false
    private var isPlayerStandingOnPlatform = false  // track state across frames


    override fun show() {
        batch = SpriteBatch()
        bg = Texture("background.png")
        playerTexture = Texture("player_knight_1.png")
        playerAttackTexture = Texture("player_knight_1_slash_reduced.png")
        skeletonTexture = Texture("skeleton.png")
        skeletonLightTexture = Texture("skeleton_light.png")
        skeletonGrayTexture = Texture("skeleton_gray.png")
        fireSlashTexture = Texture("fire_slash.png")
        heartTexture = Texture("heart.png")
        platformTexture = Texture("platform.png")

        sapphireCoinTexture = Texture("ruby_coin.png")

        // Gold coin
        goldCoinTexture = Texture("gold_coin.png")
        val frameWidth = goldCoinTexture.width / 4
        val frameHeight = goldCoinTexture.height
        val coinFrames = Array(4) { i ->
            TextureRegion(goldCoinTexture, i * frameWidth, 0, frameWidth, frameHeight)
        }
        goldCoinAnimation = Animation(0.2f, *coinFrames)
        goldCoinAnimation.playMode = Animation.PlayMode.LOOP

        // Ruby coin
        rubyCoinTexture = Texture("ruby_coin.png")
        val rubyFrameWidth = rubyCoinTexture.width / 4
        val rubyFrameHeight = rubyCoinTexture.height
        val rubyCoinFrames = Array(4) { i ->
            TextureRegion(rubyCoinTexture, i * rubyFrameWidth, 0, rubyFrameWidth, rubyFrameHeight)
        }
        rubyCoinAnimation = Animation<TextureRegion>(0.2f, *rubyCoinFrames)
        rubyCoinAnimation.playMode = Animation.PlayMode.LOOP

        // Sapphire coin
        sapphireCoinTexture = Texture("sapphire_coin.png")
        val sapphireFrameWidth = sapphireCoinTexture.width / 4
        val sapphireFrameHeight = sapphireCoinTexture.height
        val sapphireCoinFrames = Array(4) { i ->
            TextureRegion(sapphireCoinTexture, i * sapphireFrameWidth, 0, sapphireFrameWidth, sapphireFrameHeight)
        }
        sapphireCoinAnimation = Animation<TextureRegion>(0.2f, *sapphireCoinFrames)
        sapphireCoinAnimation.playMode = Animation.PlayMode.LOOP

        font = BitmapFont()
        camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0f)
        camera.update()

        resetGame()
    }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            paused = !paused
        }

        if (!paused && !gameOverFlag) {
            updateGame(delta)
        }

        drawGame()

        if (paused) {
            drawPauseScreen()
            handlePauseInput()
        } else if (gameOverFlag) {
            game.setScreen(GameOverScreen(game))
        }
    }

    private fun updateGame(delta: Float) {

        // Player horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.x -= playerSpeed * delta
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.x += playerSpeed * delta

        // Spawn platforms
        platforms.forEach { plat ->
            plat.x -= cutoffSpeed * delta
        }
        // Remove platforms
        platforms.removeAll { it.x + it.width < worldLeftEdge - 300f }

        // Gravity, jump, and invincibility handled in Player
        val isJumpPressed = Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
        val isDownPressed = Gdx.input.isKeyPressed(Input.Keys.S)
        player.update(delta, isJumpPressed, isDownPressed)
        handlePlatformCollision(delta)

        // Fire slash attack (left mouse or E)
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            val slashX = player.x + 50f
            val slashY = player.y + 50f
            fireSlashes.add(FireSlash(slashX, slashY))

            player.isAttacking = true
            player.attackTimer = player.attackDuration
        }

        platformSpawnTimer += delta
        if (platformSpawnTimer >= platformSpawnDelay) {
            // Only spawn a chunk if player is getting near the next chunk's X
            val playerRightEdge = player.x + player.width
            if (playerRightEdge > nextChunkSpawnX) {
                spawnNextPlatformChunk()
                platformSpawnTimer = 0f
            }
        }

        // Auto-runner world edge movement
        worldLeftEdge += cutoffSpeed * delta

        // Spawn skeletons as usual
        if (player.x > nextSkeletonSpawnX) {
            spawnSkeleton()
            nextSkeletonSpawnX += Random.nextFloat() * 500f + 300f
        }
        // Remove offscreen skeletons
        skeletons.removeAll { it.x < worldLeftEdge - 500f }

        // Spawn coins and increment their animation state
        for (coin in coins) {
            coin.stateTime += delta
        }
        if (player.x > nextCoinSpawnX) {
            spawnCoin()
            nextCoinSpawnX += Random.nextFloat() * 500f + 300f
        }
        coins.removeAll { it.x < worldLeftEdge - 500f }

        // Hearts spawn more rarely than coins
        if (player.x > nextHeartSpawnX) {
            spawnHeart()
            nextHeartSpawnX += Random.nextFloat() * 1200f + 800f  // much rarer than coins
        }
        hearts.removeAll { it.x < worldLeftEdge - 500f }

        // Camera tracking player
        val halfViewportWidth = camera.viewportWidth / 2f
        val minCameraCenterX = worldLeftEdge + halfViewportWidth
        camera.position.x = (player.x + player.width / 2f).coerceAtLeast(minCameraCenterX)
        camera.position.y = camera.viewportHeight / 2f
        camera.update()

        // Update blinking state of skeletons
        skeletons.forEach { it.updateBlink(delta) }

        // --- Player collision with skeletons ---
        val playerBounds = player.bounds
        val skelIter = skeletons.iterator()
        while (skelIter.hasNext()) {
            val skel = skelIter.next()
            skel.x += skel.vx * delta
            val textureRef = if (skel.isLight) skeletonLightTexture else skeletonTexture
            val skelBounds = Rectangle(skel.x, skel.y, textureRef.width.toFloat(), textureRef.height.toFloat())
            if (playerBounds.overlaps(skelBounds)) {
                onPlayerHit()
            }
        }

        // --- Fire slash hits on skeletons ---
        val slashIter = fireSlashes.iterator()
        while (slashIter.hasNext()) {
            val slash = slashIter.next()
            slash.x += slash.vx * delta
            val slashBounds = Rectangle(slash.x, slash.y, fireSlashTexture.width.toFloat(), fireSlashTexture.height.toFloat())

            val hitSkeleton = skeletons.firstOrNull { skel ->
                val textureRef = if (skel.isLight) skeletonLightTexture else skeletonTexture
                val skelBounds = Rectangle(skel.x, skel.y, textureRef.width.toFloat(), textureRef.height.toFloat())
                slashBounds.overlaps(skelBounds)
            }
            if (hitSkeleton != null) {
                // Deal damage based on FireSlash.damage (default to 1 if not explicit)
                val damage = slash.damage.takeIf { it > 0 } ?: 1
                val isDead = hitSkeleton.takeDamage(damage)

                // Make skeleton blink on hit (handled inside takeDamage by setting blink state)
                // Update blink timer will be called in updateGame loop via updateBlink(delta)

                if (isDead) {
                    skeletons.remove(hitSkeleton)
                }
                slashIter.remove()

                game.addXP(10)
                game.saveGlobals()
                continue
            }

            // Remove fire slash if off-screen (right side)
            if (slash.x > camera.position.x + camera.viewportWidth / 2f + 100f) slashIter.remove()
        }

        // --- Coin collection ---
        val coinIter = coins.iterator()
        while (coinIter.hasNext()) {
            val coin = coinIter.next()
            val texture = when (coin.type) {
                CoinType.GOLD -> goldCoinTexture
                CoinType.RUBY -> rubyCoinTexture
                CoinType.SAPPHIRE -> goldCoinTexture
            }
            val coinBounds = Rectangle(coin.x, coin.y, texture.width.toFloat(), texture.height.toFloat())
            if (playerBounds.overlaps(coinBounds)) {
                coinIter.remove()
                game.addCoins()
                game.saveGlobals()
            }
        }

        // --- Heart collection ---
        val heartIter = hearts.iterator()
        while (heartIter.hasNext()) {
            val heart = heartIter.next()
            val heartBounds = Rectangle(heart.x, heart.y, heartTexture.width.toFloat(), heartTexture.height.toFloat())
            if (player.bounds.overlaps(heartBounds)) {
                if (player.health < player.maxhealth) { // define MAX_HEALTH if you want a cap; otherwise just increase health
                    player.health += 1
                }
                heartIter.remove()
                // Optional: play SFX or show popup effect
            }
        }



        // Player attacking
        if (player.isAttacking) {
            player.attackTimer -= delta
            if (player.attackTimer <= 0f) {
                player.isAttacking = false
                player.attackTimer = 0f
            }
        }

        // Clamp player.x after all
        player.x = player.x.coerceAtLeast(worldLeftEdge)
    }

    private fun drawGame() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f)
        batch.projectionMatrix = camera.combined
        batch.begin()

        // Background looping draw - unchanged
        val bgWidth = bg.width.toFloat()
        val halfViewportWidth = camera.viewportWidth / 2f
        val viewportLeft = camera.position.x - halfViewportWidth
        val viewportRight = camera.position.x + halfViewportWidth
        var startX = (viewportLeft / bgWidth).toInt() * bgWidth
        while (startX < viewportRight) {
            batch.draw(bg, startX, 0f, bgWidth, Gdx.graphics.height.toFloat())
            startX += bgWidth
        }

        // Player (with blinking if invincible and attack texture if attacking)
        val currentPlayerTexture = if (player.isAttacking) playerAttackTexture else playerTexture

        if (player.isInvincible) {
            batch.setColor(1f, 1f, 1f, if (player.isVisible) 1f else 0.3f)
        } else {
            batch.setColor(1f, 1f, 1f, 1f)
        }
        batch.draw(currentPlayerTexture, player.x, player.y)
        batch.setColor(1f, 1f, 1f, 1f)

        // --- Draw skeletons ---
        for (skel in skeletons) {
            // Pick texture based on skeleton type
            val textureRef = when (skel.type) {
                SkeletonType.STANDARD -> skeletonTexture
                SkeletonType.LIGHT -> skeletonLightTexture
                SkeletonType.GRAY -> skeletonGrayTexture
            }
            if (skel.isBlinking) {
                batch.setColor(1f, 1f, 1f, 0.3f)
            } else {
                batch.setColor(1f, 1f, 1f, 1f)
            }
            batch.draw(textureRef, skel.x, skel.y)
        }
        batch.setColor(1f, 1f, 1f, 1f) // Always reset the color!

        // --- Draw coins ---
        for (coin in coins) {
            val frame = when (coin.type) {
                CoinType.GOLD -> goldCoinAnimation.getKeyFrame(coin.stateTime, true)
                CoinType.RUBY -> rubyCoinAnimation.getKeyFrame(coin.stateTime, true)
                CoinType.SAPPHIRE -> sapphireCoinAnimation.getKeyFrame(coin.stateTime, true)
            }
            batch.draw(frame, coin.x, coin.y)
        }

        // --- Draw platforms ---
        for (platform in platforms) {
            batch.draw(platformTexture, platform.x, platform.y)
        }
        // --- Draw fire slashes ---
        fireSlashes.forEach { slash ->
            batch.draw(fireSlashTexture, slash.x, slash.y)
        }
        //  --- Draw hearts ---
        for (heart in hearts) {
            batch.draw(heartTexture, heart.x, heart.y)
        }

        // --- Draw Player's Health at Top Left ---
        val heartIconW = heartTexture.width.toFloat()
        val heartIconH = heartTexture.height.toFloat()
        val spacing = 12f // adjust as you like
        val topPadding = 20f
        val leftPadding = 20f
        for (i in 0 until player.health) {
            val drawX = camera.position.x - camera.viewportWidth / 2f + leftPadding + i * (heartIconW + spacing)
            val drawY = camera.position.y + camera.viewportHeight / 2f - topPadding - heartIconH
            batch.draw(heartTexture, drawX, drawY)
        }
        batch.end()
    }

    fun getRandomCoinType(): CoinType {
        val rand = Math.random() * 100
        var cumulative = 0.0

        for (type in CoinType.entries) {
            cumulative += type.spawnChance
            if (rand < cumulative) {
                return type
            }
        }
        return CoinType.GOLD // fallback default
    }

    private fun spawnCoin() {
        val spawnX = camera.position.x + camera.viewportWidth / 2f + 150f + Random.nextFloat() * 200f
        // Define your min/max coin spawn heights:
        val minY = groundY + 40f           // just above ground
        val maxY = groundY + 300f          // high in air/platforms
        val spawnY = minY + Random.nextFloat() * (maxY - minY)

        // Pick coin type randomly if you want:
        val type = getRandomCoinType()
        coins.add(Coin(spawnX, spawnY, type))
    }

    private fun spawnHeart() {
        val spawnX = camera.position.x + camera.viewportWidth / 2f + 150f + Random.nextFloat() * 300f
        val minY = groundY + 40f
        val maxY = groundY + 320f
        val spawnY = minY + Random.nextFloat() * (maxY - minY)

        hearts.add(Heart(spawnX, spawnY))
    }

    private fun spawnSkeleton() {
        val spawnX = camera.position.x + camera.viewportWidth / 2f + 100f + Random.nextFloat() * 200f
        val types = SkeletonType.entries.toTypedArray()
        val randomType = types.random()  // Randomly pick one type from enum values

        // Set default health based on type (you can customize as you want)
        val health = when(randomType) {
            SkeletonType.STANDARD -> 3
            SkeletonType.LIGHT -> 2
            SkeletonType.GRAY -> 4
        }

        skeletons.add(Skeleton(spawnX, groundY, vx = -60f, type = randomType, health = health))
    }

    private fun onPlayerHit() {
        player.onHit()
        if (!player.isAlive()) gameOver()
    }

    private fun resetGame() {
        player.reset(startX = 100f, startY = groundY, startHealth = 3)
        worldLeftEdge = 0f
        skeletons.clear()
        fireSlashes.clear()
        nextSkeletonSpawnX = 400f
        gameOverFlag = false
    }

    private fun gameOver() {
        println("GAME OVER!")
        gameOverFlag = true
    }

    private fun drawPauseScreen() {
        batch.begin()
        font.color = Color.WHITE
        val text = "PAUSED\nPress Q to quit to menu\nPress P to resume"
        val layout = GlyphLayout(font, text)
        font.draw(batch, text, camera.position.x - layout.width/2, camera.position.y + layout.height/2)
        batch.end()
    }

    private fun handlePauseInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            game.setScreen(MainMenuScreen(game))
            dispose()
        }
    }

    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0f)
        camera.update()
    }

    private fun spawnNextPlatformChunk() {
        // Always spawn new chunks beyond the visible area
        val cameraRightEdge = camera.position.x + camera.viewportWidth / 2f
        val chunk = PlatformPatterns.getRandomPattern()
        val chunkOffsetX = maxOf(nextChunkSpawnX, cameraRightEdge + 300f)

        for (plat in chunk) {
            val platY = maxOf(plat.y, groundY + platformTexture.height.toFloat() + 50f)
            platforms.add(
                Platform(
                    x = chunkOffsetX + plat.x,
                    y = platY,
                    width = plat.width,
                    height = plat.height
                )
            )
        }
        nextChunkSpawnX = chunkOffsetX + chunk.maxOf { it.x + it.width }
        chunksSpawned++
    }

    // Platform collision logic:
    private fun handlePlatformCollision(delta: Float) {
        val footHeight = 10f
        val footRect = Rectangle(player.x, player.y, player.width, footHeight)

        var standingOnPlatform: Platform? = null

        for (platform in platforms) {
            if (platform.y <= groundY) continue // skip ground platform, handled below

            val horizontallyAligned = footRect.x + footRect.width > platform.x &&
                footRect.x < platform.x + platform.width

            // The platform's top edge y position
            val platformTop = platform.y + platform.height

            // Player's feet vertical position (assumed at player.y)
            val playerFeetY = player.y

            // Check if player's feet are within a small range of the platform top (e.g., +/- 2 pixels)
            val feetCloseToPlatform = playerFeetY >= platformTop - footHeight && playerFeetY <= platformTop + 2f

            // Allow platform collision only if horizontally aligned and feet close vertically
            if (horizontallyAligned && player.verticalVelocity <= 0f && feetCloseToPlatform) {
                standingOnPlatform = platform
                break
            }
        }

        if (standingOnPlatform != null) {
            if (Gdx.input.isKeyPressed(Input.Keys.S) && player.y > groundY + 5f) {
                // Drop-through behavior
                isPlayerStandingOnPlatform = false
            } else {
                // Snap player to platform top and reset jump and velocity
                player.y = standingOnPlatform.y + standingOnPlatform.height
                player.verticalVelocity = 0f
                player.jumpsDone = 0
                isPlayerStandingOnPlatform = true
            }
        } else {
            // Ground collision check
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

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        batch.dispose()
        bg.dispose()
        playerTexture.dispose()
        playerAttackTexture.dispose()
        skeletonTexture.dispose()
        skeletonLightTexture.dispose()
        skeletonGrayTexture.dispose()
        fireSlashTexture.dispose()
        heartTexture.dispose()
        goldCoinTexture.dispose()
        rubyCoinTexture.dispose()
        sapphireCoinTexture.dispose()
        platformTexture.dispose()

        font.dispose()
    }
}
