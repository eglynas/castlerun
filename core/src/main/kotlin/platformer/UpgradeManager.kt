package platformer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

class UpgradeManager {

    private val upgrades = mutableListOf<Upgrade>()
    private val prefs: Preferences = Gdx.app.getPreferences("game-upgrades")

    fun initializeUpgrades() {
        upgrades.clear()
        upgrades.add(
            Upgrade(
                name = "Jump Count",
                level = 1,
                maxLevel = 3,
                cost = 100,
                baseCost = 100,
                baseValue = 1f,
                scalingType = ScalingType.ADDITIVE,
                increment = 1f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Attack Speed",
                level = 1,
                maxLevel = 10,
                cost = 150,
                baseCost = 150,
                baseValue = 0.25f,
                scalingType = ScalingType.MULTIPLICATIVE,
                valueScale = 0.9f,
                increment = 0f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Moving Speed",
                level = 1,
                maxLevel = 10,
                cost = 150,
                baseCost = 150,
                baseValue = 300f,
                scalingType = ScalingType.MULTIPLICATIVE,
                valueScale = 1.1f,
                increment = 0f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Max Health",
                level = 1,
                maxLevel = 10,
                cost = 200,
                baseCost = 200,
                baseValue = 3f,
                scalingType = ScalingType.ADDITIVE,
                increment = 1f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Coin Spawn Rate",
                level = 1,
                maxLevel = 10,
                cost = 120,
                baseCost = 120,
                baseValue = 2f,
                scalingType = ScalingType.MULTIPLICATIVE,
                valueScale = 1.1f,
                increment = 0f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Ruby Rate",
                level = 1,
                maxLevel = 50,
                cost = 180,
                baseCost = 180,
                baseValue = 15f,
                scalingType = ScalingType.MULTIPLICATIVE,
                valueScale = 1.1f,
                increment = 0f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Sapphire Rate",
                level = 1,
                maxLevel = 25,
                cost = 180,
                baseCost = 180,
                baseValue = 15f,
                scalingType = ScalingType.MULTIPLICATIVE,
                valueScale = 1.1f,
                increment = 0f
            )
        )
        upgrades.add(
            Upgrade(
                name = "EXP Boost",
                level = 1,
                maxLevel = 5,
                cost = 200,
                baseCost = 200,
                baseValue = 1f,
                scalingType = ScalingType.MULTIPLICATIVE,
                valueScale = 1.5f,
                increment = 0f
            )
        )
    }

    fun saveUpgrades() {
        for (upgrade in upgrades) {
            prefs.putInteger("upgrade_level_${upgrade.name}", upgrade.level)
        }
        prefs.flush()
    }

    fun loadUpgrades() {
        for (upgrade in upgrades) {
            val savedLevel = prefs.getInteger("upgrade_level_${upgrade.name}", upgrade.level)
            upgrade.level = savedLevel
            upgrade.refreshValue()
        }
    }

    fun resetUpgradesToLevelOne() {
        for (upgrade in upgrades) {
            upgrade.level = 1
            upgrade.refreshValue()
        }
        saveUpgrades()  // Save the reset state immediately
    }

    fun levelUpUpgrade(name: String) {
        upgrades.firstOrNull { it.name == name }?.upgrade()
    }

    fun getAllUpgrades(): List<Upgrade> = upgrades

    fun hasSavedUpgrades(): Boolean = prefs.contains("upgrade_level_Jump Count")
}



