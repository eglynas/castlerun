package platformer

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.math.min

class ShopScreen(private val game: PlatformerGame) : Screen {

    private val shapeRenderer = ShapeRenderer()
    private val shopTexture = Texture("shop.png")
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(
        Gdx.graphics.width.toFloat(),
        Gdx.graphics.height.toFloat()
    )

    /* --- original asset size --- */
    private val shopAssetSide = 1024f

    /* --- drawing metrics (re-computed on resize) --- */
    private var shopScale = 1f
    private var shopDrawSize = 0f
    private var shopX = 0f
    private var shopY = 0f

    /* ---------- MAIN BUTTONS ---------- */
    private lateinit var backButtonRect: Rectangle
    private lateinit var upgradeButtonRect: Rectangle
    private lateinit var costBarRect: Rectangle

    /* ---------- UPGRADE PANELS ---------- */
    private lateinit var jumpCountRect: Rectangle
    private lateinit var attackSpeedRect: Rectangle
    private lateinit var movingSpeedRect: Rectangle
    private lateinit var maxHealthRect: Rectangle
    private lateinit var coinSpawnRateRect: Rectangle
    private lateinit var rubyRateRect: Rectangle
    private lateinit var sapphireRateRect: Rectangle
    private lateinit var expBoostRect: Rectangle

    /* ---------- VALUE FIELDS (“LVL:” & “Current:”) ---------- */
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

    /* ====================================================================== */
    override fun show() {
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0f)
        camera.update()
        recalculateRects(Gdx.graphics.width, Gdx.graphics.height)
    }


    private fun drawDebugButtonRects() {
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.color = Color.RED

        /* list every rectangle you want to visualise */
        val rects = arrayOf(
            backButtonRect, upgradeButtonRect, costBarRect,

            jumpCountRect, attackSpeedRect, movingSpeedRect, maxHealthRect,
            coinSpawnRateRect, rubyRateRect, sapphireRateRect, expBoostRect,

            jumpCountLvlRect, attackSpeedLvlRect, movingSpeedLvlRect, maxHealthLvlRect,
            coinSpawnRateLvlRect, rubyRateLvlRect, sapphireRateLvlRect, expBoostLvlRect,

            jumpCountCurrentRect, attackSpeedCurrentRect, movingSpeedCurrentRect, maxHealthCurrentRect,
            coinSpawnRateCurrentRect, rubyRateCurrentRect, sapphireRateCurrentRect, expBoostCurrentRect
        )

        for (i in 0 until 3) {                 // 3-pixel “border” effect
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            rects.forEach { r ->
                shapeRenderer.rect(
                    r.x - i,
                    r.y - i,
                    r.width  + 2 * i,
                    r.height + 2 * i
                )
            }
            shapeRenderer.end()
        }
    }
    /* ---------- RECTANGLE CALCULATION ---------- */
    private fun recalculateRects(width: Int, height: Int) {

        /* scale shop to fit screen while preserving aspect */
        shopScale = min(width, height) / shopAssetSide
        shopDrawSize = shopAssetSide * shopScale
        shopX = (width  - shopDrawSize) / 2f
        shopY = (height - shopDrawSize) / 2f

        /* helper: convert (assetX, assetY, assetW, assetH) to screen Rectangle */
        fun rect(ax1: Float, ay1: Float, ax2: Float, ay2: Float): Rectangle {
            val assetW = ax2 - ax1
            val assetH = ay2 - ay1
            /* convert from image-top-left origin → screen-bottom-left origin */
            val screenX = shopX + ax1 * shopScale
            val screenY = shopY + (shopAssetSide - ay2) * shopScale
            return Rectangle(screenX, screenY, assetW * shopScale, assetH * shopScale)
        }

        backButtonRect           = rect(73f, 932f, 285f, 990f)
        upgradeButtonRect        = rect(743f, 929f, 947f, 995f)
        costBarRect              = rect(445f, 956f, 651f, 975f)

        jumpCountRect            = rect(76f, 382f, 243f, 605f)
        attackSpeedRect          = rect(307f, 382f, 472f, 605f)
        movingSpeedRect          = rect(535f, 382f, 700f, 605f)
        maxHealthRect            = rect(766f, 382f, 932f, 605f)

        coinSpawnRateRect        = rect(76f, 645f, 243f, 871f)
        rubyRateRect             = rect(307f, 645f, 472f, 871f)
        sapphireRateRect         = rect(535f, 645f, 700f, 871f)
        expBoostRect             = rect(766f, 645f, 932f, 871f)

        jumpCountLvlRect         = rect(131f, 547f, 233f, 565f)
        attackSpeedLvlRect    = rect(362f, 547f, 464f, 565f)
        movingSpeedLvlRect    = rect(590f, 547f, 692f, 565f)
        maxHealthLvlRect      = rect(821f, 547f, 923f, 565f)

        coinSpawnRateLvlRect  = rect(128f, 813f, 233f, 832f)
        rubyRateLvlRect       = rect(362f, 813f, 464f, 832f)
        sapphireRateLvlRect   = rect(590f, 813f, 692f, 832f)
        expBoostLvlRect       = rect(821f, 813f, 923f, 832f)

        // Current value rectangles
        jumpCountCurrentRect      = rect(165f, 575f, 233f, 593f)
        attackSpeedCurrentRect    = rect(396f, 575f, 464f, 593f)
        movingSpeedCurrentRect    = rect(624f, 575f, 692f, 593f)
        maxHealthCurrentRect      = rect(855f, 575f, 923f, 593f)

        coinSpawnRateCurrentRect  = rect(163f, 841f, 233f, 859f)
        rubyRateCurrentRect       = rect(396f, 841f, 464f, 859f)
        sapphireRateCurrentRect   = rect(624f, 841f, 692f, 859f)
        expBoostCurrentRect       = rect(855f, 841f, 923f, 859f)

    }

    /* ====================================================================== */
    override fun render(delta: Float) {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(shopTexture, shopX, shopY, shopDrawSize, shopDrawSize)
        batch.end()
        drawDebugButtonRects()

        handleInput()
    }

    /* ---------- INPUT & NAVIGATION ---------- */
    private fun handleInput() {
        /* quick keyboard exit */
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            game.setScreen(MainMenuScreen(game))
            dispose(); return
        }

        /* touch / click handling */
        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.graphics.height - Gdx.input.y.toFloat()

            when {
                backButtonRect.contains(touchX, touchY)    -> {
                    game.setScreen(MainMenuScreen(game)); dispose()
                }
                upgradeButtonRect.contains(touchX, touchY) -> {
                    // TODO: perform upgrade purchase
                }
                /* sample: highlight jump-count panel */
                jumpCountRect.contains(touchX, touchY)     -> {
                    // TODO: open details or queue upgrade
                }
                /* add similar checks for other panels … */
            }
        }
    }

    /* ====================================================================== */
    override fun resize(width: Int, height: Int) {
        camera.viewportWidth  = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.position.set(width / 2f, height / 2f, 0f)
        camera.update()
        recalculateRects(width, height)
    }

    override fun pause()  {}
    override fun resume() {}
    override fun hide()   {}

    override fun dispose() {
        batch.dispose()
        shopTexture.dispose()
    }
}
