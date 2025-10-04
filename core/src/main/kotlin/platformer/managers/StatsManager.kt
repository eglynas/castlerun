package platformer.managers

import com.badlogic.gdx.Gdx

class StatsManager {
    // Persistent stats (saved to preferences)
    var totalDeaths: Int = 0
        private set
    var totalCoinsEarned: Int = 0
        private set
    var totalKills: Int = 0
        private set
    var totalPlaytimeSeconds: Float = 0f
        private set

    // Session stats (reset each game)
    var sessionKills: Int = 0
        private set
    var sessionCoinsEarned: Int = 0
        private set
    var sessionPlaytime: Float = 0f
        private set

    // Functions for updating stats
    fun addDeath() {
        totalDeaths++
        saveStats()
    }

    fun addKill() {
        sessionKills++
        totalKills++
        saveStats()
    }

    fun addCoins(amount: Int) {
        sessionCoinsEarned += amount
        totalCoinsEarned += amount
        saveStats()
    }

    fun addPlaytime(deltaSeconds: Float) {
        sessionPlaytime += deltaSeconds
        totalPlaytimeSeconds += deltaSeconds
        saveStats()
    }

    // Session management
    fun startNewSession() {
        sessionKills = 0
        sessionCoinsEarned = 0
        sessionPlaytime = 0f
    }

    // Persistence
    fun loadStats() {
        val prefs = Gdx.app.getPreferences("platformer_stats")
        totalDeaths = prefs.getInteger("totalDeaths", 0)
        totalCoinsEarned = prefs.getInteger("totalCoins", 0)
        totalKills = prefs.getInteger("totalKills", 0)
        totalPlaytimeSeconds = prefs.getFloat("totalPlaytime", 0f)
    }

    private fun saveStats() {
        val prefs = Gdx.app.getPreferences("platformer_stats")
        prefs.putInteger("totalDeaths", totalDeaths)
        prefs.putInteger("totalCoins", totalCoinsEarned)
        prefs.putInteger("totalKills", totalKills)
        prefs.putFloat("totalPlaytime", totalPlaytimeSeconds)
        prefs.flush()
    }
}
