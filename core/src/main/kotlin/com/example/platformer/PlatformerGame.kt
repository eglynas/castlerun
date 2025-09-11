package com.example.platformer

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx

class PlatformerGame : Game() {
    var collectedCoins: Int = 0
        private set
    var xp: Int = 0
        private set

    fun addCoins(amount: Int = 1) {
        collectedCoins += amount
        saveGlobals()
    }
    fun addXP(amount: Int = 1) {
        xp += amount
        saveGlobals()
    }

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
        // Start the game with the main menu screen
        loadGlobals()
        setScreen(MainMenuScreen(this))
    }
}
