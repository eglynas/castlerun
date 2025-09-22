package platformer
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    val monitor = Lwjgl3ApplicationConfiguration.getPrimaryMonitor()
    val displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode(monitor)

    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Platformer Game")
        setWindowedMode(displayMode.width, displayMode.height - 100)
        setDecorated(true)
        setResizable(true)
    }

    Lwjgl3Application(PlatformerGame(), config)
}
