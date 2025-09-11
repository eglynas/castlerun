

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.example.platformer.PlatformerGame

/** Launches the desktop (LWJGL3) application. */
object Lwjgl3Launcher {
    @JvmStatic
    fun main(args: Array<String>) {
        if (StartupHelper.startNewJvmIfRequired()) return // This handles macOS support and helps on Windows.
        createApplication()
    }

    private fun createApplication(): Lwjgl3Application {
        return Lwjgl3Application(PlatformerGame(), getDefaultConfiguration())
    }

    private fun getDefaultConfiguration(): Lwjgl3ApplicationConfiguration {
        val configuration = Lwjgl3ApplicationConfiguration()
        configuration.setTitle("game-front")
        // Vsync limits the FPS and helps eliminate screen tearing.
        configuration.useVsync(true)
        // Limits FPS to the current monitor's refresh rate plus 1.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1)
        configuration.setWindowedMode(640, 480)
        // You can change these icon files if you want.
        configuration.setWindowIcon(
            "libgdx128.png",
            "libgdx64.png",
            "libgdx32.png",
            "libgdx16.png"
        )
        return configuration
    }
}
