// PlatformPatterns.kt
package com.example.platformer

const val WIDTH = 120f
const val HEIGHT = 16f

object PlatformPatterns {
    // Each chunk is a list of relative Platform objects
    val patternFlat = listOf(
        Platform(0f, 100f, WIDTH, HEIGHT),              // Ground level
        Platform(300f, 100f, WIDTH, HEIGHT)
    )

    val patternStairs = listOf(
        Platform(0f, 100f, WIDTH, HEIGHT),              // Lower
        Platform(180f, 200f, WIDTH, HEIGHT),            // Up right
        Platform(380f, 320f, WIDTH, HEIGHT)             // Highest
    )

    val patternHighLow = listOf(
        Platform(0f, 220f, WIDTH, HEIGHT),
        Platform(250f, 100f, WIDTH, HEIGHT)
    )

    // Add as many as you want!

    val allPatterns = listOf(patternFlat, patternStairs, patternHighLow)

    // Helper function to pick a pattern (randomly, or you can decide the order)
    fun getRandomPattern(): List<Platform> {
        return allPatterns.random()
    }
}
