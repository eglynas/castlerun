package platformer.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils
import platformer.PlatformerGame

class GameOverScreen(private val game: PlatformerGame) : Screen {

    private val gameOverTexture = Texture("game_over.png")
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

    override fun show() {
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0f)
        camera.update()
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        // Calculate draw size and position to center and scale texture to screen
        val screenW = Gdx.graphics.width.toFloat()
        val screenH = Gdx.graphics.height.toFloat()
        val texW = gameOverTexture.width.toFloat()
        val texH = gameOverTexture.height.toFloat()

        val scale = minOf(screenW / texW, screenH / texH)
        val drawW = texW * scale
        val drawH = texH * scale
        val drawX = camera.position.x - drawW / 2f
        val drawY = camera.position.y - drawH / 2f

        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.draw(gameOverTexture, drawX, drawY, drawW, drawH)
        batch.end()

        handleInput()
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.setScreen(GameplayScreen(game))
            dispose()
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            game.setScreen(MainMenuScreen(game))
            dispose()
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            game.setScreen(ShopScreen(game))
            dispose()
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

    override fun dispose() {
        batch.dispose()
        gameOverTexture.dispose()
    }
}
