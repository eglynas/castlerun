package platformer

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import platformer.screens.MainMenuScreen
import platformer.managers.StatsManager

class PlatformerGame : Game() {
    // Current game resources (not statistics!)
    var collectedCoins: Int = 1  // Current coins in wallet
        private set
    var xp: Int = 0             // Current XP level
        private set

    val statsManager = StatsManager()

    fun addCoins(amount: Int = 1) {
        collectedCoins += amount
        statsManager.addCoins(amount)  // Track for stats
        saveGlobals()
    }

    fun spendCoins(cost: Int) {
        collectedCoins -= cost
        saveGlobals()
    }

    fun addXP(amount: Int = 1) {
        xp += amount
        saveGlobals()
    }

    fun getCollectedXP(): Int = xp

    fun loadGlobals() {
        val prefs = Gdx.app.getPreferences("platformer_prefs")
        collectedCoins = prefs.getInteger("coinCount", 0)
        xp = prefs.getInteger("xp", 0)
    }

    fun saveGlobals() {
        val prefs = Gdx.app.getPreferences("platformer_prefs")
        prefs.putInteger("coinCount", collectedCoins)
        prefs.putInteger("xp", xp)
        prefs.flush()
    }

    override fun create() {
        loadGlobals()
        statsManager.loadStats()       // Load stats separately
        setScreen(MainMenuScreen(this))
    }
}
