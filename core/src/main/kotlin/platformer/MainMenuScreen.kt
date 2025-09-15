package platformer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import kotlin.math.min

class MainMenuScreen(private val game: PlatformerGame) : Screen {

    private val batch = SpriteBatch()
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont() // Or use a custom font if you prefer
    private val mainMenuTexture = Texture("main_menu.png")

    private val menuAssetWidth = 1536f
    private val menuAssetHeight = 1024f

    private var menuScale = 1f
    private var menuDrawWidth = 0f
    private var menuDrawHeight = 0f
    private var menuX = 0f
    private var menuY = 0f

    private lateinit var newGameButtonRect: Rectangle
    private lateinit var loadGameButtonRect: Rectangle
    private lateinit var shopButtonRect: Rectangle
    private lateinit var closeGameButtonRect: Rectangle

    override fun show() {
        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        batch.draw(mainMenuTexture, menuX, menuY, menuDrawWidth, menuDrawHeight)
        // Draw coin count at desired screen location (for example, top left)
        font.color = Color.GOLD // Optional: change color
        font.data.setScale(2f) // Optional: make the text larger
        font.draw(batch, "Coin Count: ${game.collectedCoins}", menuX + 40f, menuY + menuDrawHeight - 40f)
        font.draw(batch, "XP Count: ${game.xp}", menuX + 40f, menuY + menuDrawHeight - 80f)
        batch.end()

        drawDebugButtonRects()

        handleInput()
    }

    override fun resize(width: Int, height: Int) {
        val scaleX = width / menuAssetWidth
        val scaleY = height / menuAssetHeight
        menuScale = min(scaleX, scaleY)

        menuDrawWidth = menuAssetWidth * menuScale
        menuDrawHeight = menuAssetHeight * menuScale

        menuX = (width - menuDrawWidth) / 2f
        menuY = (height - menuDrawHeight) / 2f

        // Helper lambda for y coordinate conversion:
        val convertY: (Float, Float) -> Float = { yTop, height ->
            menuAssetHeight - yTop - height
        }

        // New Game button coords
        val newGameX = 480f
        val newGameYTop = 295f
        val newGameW = 1060f - 480f
        val newGameH = 440f - newGameYTop
        val newGameYLibGDX = convertY(newGameYTop, newGameH)

        newGameButtonRect = Rectangle(
            menuX + newGameX * menuScale,
            menuY + newGameYLibGDX * menuScale,
            newGameW * menuScale,
            newGameH * menuScale
        )

        // Load Game button coords
        val loadGameX = 480f
        val loadGameYTop = 480f
        val loadGameW = 1060f - 480f
        val loadGameH = 625f - loadGameYTop
        val loadGameYLibGDX = convertY(loadGameYTop, loadGameH)

        loadGameButtonRect = Rectangle(
            menuX + loadGameX * menuScale,
            menuY + loadGameYLibGDX * menuScale,
            loadGameW * menuScale,
            loadGameH * menuScale
        )

        // Shop button coords
        val shopX = 480f
        val shopYTop = 670f
        val shopW = 1060f - 480f
        val shopH = 810f - shopYTop
        val shopYLibGDX = convertY(shopYTop, shopH)

        shopButtonRect = Rectangle(
            menuX + shopX * menuScale,
            menuY + shopYLibGDX * menuScale,
            shopW * menuScale,
            shopH * menuScale
        )

        // Close Game button coords
        val closeX = 970f
        val closeYTop = 836f
        val closeW = 1380f - 970f
        val closeH = 945f - closeYTop
        val closeYLibGDX = convertY(closeYTop, closeH)

        closeGameButtonRect = Rectangle(
            menuX + closeX * menuScale,
            menuY + closeYLibGDX * menuScale,
            closeW * menuScale,
            closeH * menuScale
        )
    }

    private fun handleInput() {
        if (Gdx.input.justTouched()) {
            val mouseX = Gdx.input.x.toFloat()
            val mouseY = (Gdx.graphics.height - Gdx.input.y).toFloat()

            when {
                newGameButtonRect.contains(mouseX, mouseY) -> {
                    game.setScreen(GameplayScreen(game))
                }
                loadGameButtonRect.contains(mouseX, mouseY) -> {
                    println("Load Game clicked - not implemented")
                }
                shopButtonRect.contains(mouseX, mouseY) -> {
                    game.setScreen(ShopScreen(game))
                }
                closeGameButtonRect.contains(mouseX, mouseY) -> {
                    Gdx.app.exit()
                }
            }
        }
    }

    private fun drawDebugButtonRects() {
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.color = Color.RED

        for (i in 0 until 3) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.rect(
                newGameButtonRect.x - i,
                newGameButtonRect.y - i,
                newGameButtonRect.width + 2 * i,
                newGameButtonRect.height + 2 * i
            )
            shapeRenderer.rect(
                loadGameButtonRect.x - i,
                loadGameButtonRect.y - i,
                loadGameButtonRect.width + 2 * i,
                loadGameButtonRect.height + 2 * i
            )
            shapeRenderer.rect(
                shopButtonRect.x - i,
                shopButtonRect.y - i,
                shopButtonRect.width + 2 * i,
                shopButtonRect.height + 2 * i
            )
            shapeRenderer.rect(
                closeGameButtonRect.x - i,
                closeGameButtonRect.y - i,
                closeGameButtonRect.width + 2 * i,
                closeGameButtonRect.height + 2 * i
            )
            shapeRenderer.end()
        }
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        batch.dispose()
        mainMenuTexture.dispose()
        shapeRenderer.dispose()
        font.dispose()
    }
}
