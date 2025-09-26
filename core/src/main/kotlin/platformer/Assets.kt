package platformer

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.BitmapFont

object Assets {
    val manager = AssetManager()

    // Texture paths (adjust as needed, ensure files exist in your assets directory)
    private const val BG = "background.png"
    private const val PLAYER = "player_knight_1.png"
    private const val PLAYER_ATTACK = "player_knight_1_slash_reduced.png"
    private const val SKELETON = "skeleton.png"
    private const val SKELETON_LIGHT = "skeleton_light.png"
    private const val SKELETON_GRAY = "skeleton_gray.png"
    private const val FIRE_SLASH = "fire_slash.png"
    private const val HEART = "heart.png"
    private const val GOLD_COIN = "gold_coin.png"
    private const val RUBY_COIN = "ruby_coin.png"
    private const val SAPPHIRE_COIN = "sapphire_coin.png"
    private const val PLATFORM = "platform.png"
    private const val MAIN_MENU = "main_menu.png"
    private const val SHOP = "shop.png"
    private const val COIN_BONUS = "coin_bonus.png"

    var bg: Texture = Texture(BG)
    lateinit var player: Texture
    lateinit var playerAttack: Texture
    lateinit var skeleton: Texture
    lateinit var skeletonLight: Texture
    lateinit var skeletonGray: Texture
    lateinit var fireSlash: Texture
    lateinit var heart: Texture
    lateinit var goldCoin: Texture
    lateinit var rubyCoin: Texture
    lateinit var sapphireCoin: Texture
    lateinit var platform: Texture
    lateinit var mainMenu: Texture
    lateinit var shop: Texture
    lateinit var coin_bonus: Texture

    lateinit var font: BitmapFont


    lateinit var goldCoinAnimation: Animation<TextureRegion>
    lateinit var rubyCoinAnimation: Animation<TextureRegion>
    lateinit var sapphireCoinAnimation: Animation<TextureRegion>

    fun loadAll() {
        val textures = listOf(
            PLAYER, PLAYER_ATTACK, SKELETON, SKELETON_LIGHT, SKELETON_GRAY,
            FIRE_SLASH, HEART, GOLD_COIN, RUBY_COIN, SAPPHIRE_COIN, PLATFORM,
            MAIN_MENU, SHOP, COIN_BONUS
        )
        textures.forEach { manager.load(it, Texture::class.java) }
        manager.finishLoading()

        bg = Texture(BG) // Loaded directly as it's used for repeating background
        player = manager.get(PLAYER, Texture::class.java)
        playerAttack = manager.get(PLAYER_ATTACK, Texture::class.java)
        skeleton = manager.get(SKELETON, Texture::class.java)
        skeletonLight = manager.get(SKELETON_LIGHT, Texture::class.java)
        skeletonGray = manager.get(SKELETON_GRAY, Texture::class.java)
        fireSlash = manager.get(FIRE_SLASH, Texture::class.java)
        heart = manager.get(HEART, Texture::class.java)
        goldCoin = manager.get(GOLD_COIN, Texture::class.java)
        rubyCoin = manager.get(RUBY_COIN, Texture::class.java)
        sapphireCoin = manager.get(SAPPHIRE_COIN, Texture::class.java)
        platform = manager.get(PLATFORM, Texture::class.java)
        mainMenu = manager.get(MAIN_MENU, Texture::class.java)
        shop = manager.get(SHOP, Texture::class.java)
        coin_bonus = manager.get(COIN_BONUS, Texture::class.java)
        font = BitmapFont()

        // Create coin animations
        goldCoinAnimation = createAnimation(goldCoin)
        rubyCoinAnimation = createAnimation(rubyCoin)
        sapphireCoinAnimation = createAnimation(sapphireCoin)
    }

    private fun createAnimation(texture: Texture): Animation<TextureRegion> {
        val frameWidth = texture.width / 4
        val frameHeight = texture.height
        val frames = Array(4) { i -> TextureRegion(texture, i * frameWidth, 0, frameWidth, frameHeight) }
        return Animation(0.2f, *frames).apply { playMode = Animation.PlayMode.LOOP }
    }

    fun disposeAll() {
        bg.dispose()
        font.dispose()
        manager.dispose()
    }
}
