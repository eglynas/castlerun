package platformer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.math.min

class ShopScreen(private val game: PlatformerGame) : Screen {

    data class UpgradeRects(
        val mainRect: Rectangle,
        val lvlRect: Rectangle,
        val currentRect: Rectangle
    )

    private var selectedUpgrade: Upgrade? = null
    private lateinit var upgradeRectsMap: Map<String, UpgradeRects>

    private val upgradeManager = UpgradeManager()
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
    private val font = BitmapFont()

    private val shopAssetSide = 1024f

    private var shopScale = 1f
    private var shopDrawSize = 0f
    private var shopX = 0f
    private var shopY = 0f

    private lateinit var backButtonRect: Rectangle
    private lateinit var upgradeButtonRect: Rectangle
    private lateinit var costBarRect: Rectangle

    private lateinit var jumpCountRect: Rectangle
    private lateinit var attackSpeedRect: Rectangle
    private lateinit var movingSpeedRect: Rectangle
    private lateinit var maxHealthRect: Rectangle
    private lateinit var coinSpawnRateRect: Rectangle
    private lateinit var rubyRateRect: Rectangle
    private lateinit var sapphireRateRect: Rectangle
    private lateinit var expBoostRect: Rectangle

    private lateinit var jumpCountLvlRect: Rectangle
    private lateinit var attackSpeedLvlRect: Rectangle
    private lateinit var movingSpeedLvlRect: Rectangle
    private lateinit var maxHealthLvlRect: Rectangle
    private lateinit var coinSpawnRateLvlRect: Rectangle
    private lateinit var rubyRateLvlRect: Rectangle
    private lateinit var sapphireRateLvlRect: Rectangle
    private lateinit var expBoostLvlRect: Rectangle

    private lateinit var jumpCountCurrentRect: Rectangle
    private lateinit var attackSpeedCurrentRect: Rectangle
    private lateinit var movingSpeedCurrentRect: Rectangle
    private lateinit var maxHealthCurrentRect: Rectangle
    private lateinit var coinSpawnRateCurrentRect: Rectangle
    private lateinit var rubyRateCurrentRect: Rectangle
    private lateinit var sapphireRateCurrentRect: Rectangle
    private lateinit var expBoostCurrentRect: Rectangle

    private fun initUpgradeRectsMap() {
        upgradeRectsMap = mapOf(
            "Jump Count" to UpgradeRects(jumpCountRect, jumpCountLvlRect, jumpCountCurrentRect),
            "Attack Speed" to UpgradeRects(attackSpeedRect, attackSpeedLvlRect, attackSpeedCurrentRect),
            "Moving Speed" to UpgradeRects(movingSpeedRect, movingSpeedLvlRect, movingSpeedCurrentRect),
            "Max Health" to UpgradeRects(maxHealthRect, maxHealthLvlRect, maxHealthCurrentRect),
            "Coin Spawn Rate" to UpgradeRects(coinSpawnRateRect, coinSpawnRateLvlRect, coinSpawnRateCurrentRect),
            "Ruby Rate" to UpgradeRects(rubyRateRect, rubyRateLvlRect, rubyRateCurrentRect),
            "Sapphire Rate" to UpgradeRects(sapphireRateRect, sapphireRateLvlRect, sapphireRateCurrentRect),
            "EXP Boost" to UpgradeRects(expBoostRect, expBoostLvlRect, expBoostCurrentRect)
        )
    }

    override fun show() {
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0f)
        camera.update()
        recalculateRects(Gdx.graphics.width, Gdx.graphics.height)
        initUpgradeRectsMap()
        if (!upgradeManager.hasSavedUpgrades()) {
            upgradeManager.initializeUpgrades()
            upgradeManager.saveUpgrades()
        } else {
            upgradeManager.initializeUpgrades()
            upgradeManager.loadUpgrades()
        }
        selectedUpgrade = null  // Start with no upgrade selected
        //upgradeManager.resetUpgradesToLevelOne()
    }

    fun drawThickRectangleOutline(
        shapeRenderer: ShapeRenderer,
        x: Float, y: Float, width: Float, height: Float,
        thickness: Float,
        color: Color
    ) {
        shapeRenderer.color = color
        shapeRenderer.rect(x, y + height - thickness, width, thickness)   // Top border
        shapeRenderer.rect(x, y, width, thickness)                        // Bottom border
        shapeRenderer.rect(x, y, thickness, height)                       // Left border
        shapeRenderer.rect(x + width - thickness, y, thickness, height)  // Right border
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0f, 0f, 0f, 1f)
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(Assets.shop, shopX, shopY, shopDrawSize, shopDrawSize)
        font.color = Color.BLACK

        val upgrades = upgradeManager.getAllUpgrades()
        for (upgrade in upgrades) {
            val rects = upgradeRectsMap[upgrade.name] ?: continue

            if (upgrade == selectedUpgrade) {
                batch.end()
                shapeRenderer.projectionMatrix = camera.combined
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
                drawThickRectangleOutline(
                    shapeRenderer,
                    rects.mainRect.x,
                    rects.mainRect.y,
                    rects.mainRect.width,
                    rects.mainRect.height,
                    3f,
                    Color.GREEN
                )
                shapeRenderer.end()
                batch.begin()
            }

            val levelText = upgrade.level.toString()
            val levelLayout = GlyphLayout(font, levelText)
            val levelX = rects.lvlRect.x + (rects.lvlRect.width - levelLayout.width) / 2
            val levelY = rects.lvlRect.y + (rects.lvlRect.height + levelLayout.height) / 2
            font.draw(batch, levelLayout, levelX, levelY)

            val valueText = if (upgrade.level >= upgrade.maxLevel) {
                "MAX"
            } else {
                when (upgrade.name) {
                    "Attack Speed" -> String.format("%.2f ms", upgrade.currentValue)
                    "EXP Boost" -> String.format("%.1fx", upgrade.currentValue)
                    "Ruby Rate" -> String.format("%.0f%%", upgrade.currentValue)
                    "Sapphire Rate" -> String.format("%.0f%%", upgrade.currentValue)
                    else -> upgrade.currentValue.toInt().toString()
                }
            }
            val valueLayout = GlyphLayout(font, valueText)
            val valueX = rects.currentRect.x + (rects.currentRect.width - valueLayout.width) / 2
            val valueY = rects.currentRect.y + (rects.currentRect.height + valueLayout.height) / 2
            font.draw(batch, valueLayout, valueX, valueY)
        }

        // Draw cost text with color indicating affordability
        selectedUpgrade?.let { sel ->
            val canUpgrade = sel.canUpgrade()
            font.color = when {
                !canUpgrade -> Color.GRAY
                game.collectedCoins >= sel.cost -> Color.GREEN
                else -> Color.RED
            }

            val costText = if (sel.canUpgrade()) sel.cost.toString() else "-"
            val costLayout = GlyphLayout(font, costText)
            val costX = costBarRect.x + (costBarRect.width - costLayout.width) / 2
            val costY = costBarRect.y + (costBarRect.height + costLayout.height) / 2
            font.draw(batch, costLayout, costX, costY)

            font.color = Color.BLACK // Reset after cost text
        }
        batch.end()
        handleInput()
    }

    private fun handleInput() {
        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()
            when {
                backButtonRect.contains(touchX, touchY) -> {
                    game.setScreen(MainMenuScreen(game))
                    dispose()
                }
                upgradeButtonRect.contains(touchX, touchY) -> {
                    selectedUpgrade?.let { sel ->
                        if (game.collectedCoins >= sel.cost && sel.canUpgrade()) {
                            game.spendCoins(sel.cost)
                            upgradeManager.levelUpUpgrade(sel.name)
                            upgradeManager.saveUpgrades()
                            selectedUpgrade = upgradeManager.getAllUpgrades().firstOrNull { it.name == sel.name }
                        }
                    }
                }
                else -> {
                    for (upgrade in upgradeManager.getAllUpgrades()) {
                        val rects = upgradeRectsMap[upgrade.name] ?: continue
                        if (rects.mainRect.contains(touchX, touchY)) {
                            selectedUpgrade = upgrade
                            break
                        }
                    }
                }
            }
        }
    }

    private fun recalculateRects(width: Int, height: Int) {
        shopScale = min(width, height) / shopAssetSide
        shopDrawSize = shopAssetSide * shopScale
        shopX = (width - shopDrawSize) / 2f
        shopY = (height - shopDrawSize) / 2f

        fun rect(ax1: Float, ay1: Float, ax2: Float, ay2: Float): Rectangle {
            val assetW = ax2 - ax1
            val assetH = ay2 - ay1
            val screenX = shopX + ax1 * shopScale
            val screenY = shopY + (shopAssetSide - ay2) * shopScale
            return Rectangle(screenX, screenY, assetW * shopScale, assetH * shopScale)
        }

        backButtonRect = rect(73f, 932f, 285f, 990f)
        upgradeButtonRect = rect(743f, 929f, 947f, 995f)
        costBarRect = rect(445f, 956f, 651f, 975f)

        jumpCountRect = rect(76f, 382f, 243f, 605f)
        attackSpeedRect = rect(307f, 382f, 472f, 605f)
        movingSpeedRect = rect(535f, 382f, 700f, 605f)
        maxHealthRect = rect(766f, 382f, 932f, 605f)

        coinSpawnRateRect = rect(76f, 645f, 243f, 871f)
        rubyRateRect = rect(307f, 645f, 472f, 871f)
        sapphireRateRect = rect(535f, 645f, 700f, 871f)
        expBoostRect = rect(766f, 645f, 932f, 871f)

        jumpCountLvlRect = rect(131f, 547f, 233f, 565f)
        attackSpeedLvlRect = rect(362f, 547f, 464f, 565f)
        movingSpeedLvlRect = rect(590f, 547f, 692f, 565f)
        maxHealthLvlRect = rect(821f, 547f, 923f, 565f)

        coinSpawnRateLvlRect = rect(128f, 813f, 233f, 832f)
        rubyRateLvlRect = rect(362f, 813f, 464f, 832f)
        sapphireRateLvlRect = rect(590f, 813f, 692f, 832f)
        expBoostLvlRect = rect(821f, 813f, 923f, 832f)

        jumpCountCurrentRect = rect(165f, 575f, 233f, 593f)
        attackSpeedCurrentRect = rect(396f, 575f, 464f, 593f)
        movingSpeedCurrentRect = rect(624f, 575f, 692f, 593f)
        maxHealthCurrentRect = rect(855f, 575f, 923f, 593f)

        coinSpawnRateCurrentRect = rect(163f, 841f, 233f, 859f)
        rubyRateCurrentRect = rect(396f, 841f, 464f, 859f)
        sapphireRateCurrentRect = rect(624f, 841f, 692f, 859f)
        expBoostCurrentRect = rect(855f, 841f, 923f, 859f)
    }

    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.position.set(width / 2f, height / 2f, 0f)
        camera.update()
        recalculateRects(width, height)
        initUpgradeRectsMap()
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
        font.dispose()
    }
}
