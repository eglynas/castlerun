package platformer.entities

const val WIDTH = 120f
const val HEIGHT = 16f

object PlatformPatterns {
    // Original patterns shifted higher by +40 on Y

    val patternStairs = listOf(
        Platform(0f, 240f, WIDTH, HEIGHT),
        Platform(180f, 340f, WIDTH, HEIGHT),
        Platform(380f, 460f, WIDTH, HEIGHT)
    )

    // New patterns with multiple platforms:

    val patternLeapFrog = listOf(
        Platform(0f, 240f, WIDTH, HEIGHT),
        Platform(160f, 300f, WIDTH, HEIGHT),
        Platform(320f, 360f, WIDTH, HEIGHT),
        Platform(480f, 420f, WIDTH, HEIGHT),
        Platform(640f, 480f, WIDTH, HEIGHT)
    )

    val patternWideSpread = listOf(
        Platform(0f, 240f, WIDTH, HEIGHT),
        Platform(250f, 320f, WIDTH, HEIGHT),
        Platform(600f, 380f, WIDTH, HEIGHT),
        Platform(850f, 440f, WIDTH, HEIGHT)
    )


    val allPatterns = listOf(
        patternStairs, patternLeapFrog,
        patternWideSpread
    )

    fun getRandomPattern(): List<Platform> {
        return allPatterns.random()
    }
}
