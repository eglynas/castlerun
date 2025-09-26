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
                currentCost = 1000,
                baseCost = 1000,
                costScale = 5f,
                baseValue = 1f,
                valueScale = 1f,
                scalingType = ScalingType.ADDITIVE,
                increment = 1f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Attack Speed",
                level = 1,
                maxLevel = 11,
                currentCost = 150,
                baseCost = 300,
                costScale = 1.5f,
                baseValue = 0.4f,
                valueScale = 1.5f,
                scalingType = ScalingType.ADDITIVE,
                increment = -0.025f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Moving Speed",
                level = 1,
                maxLevel = 5,
                currentCost = 200,
                baseCost = 200,
                costScale = 1.5f,
                baseValue = 300f,
                valueScale = 1.5f,
                scalingType = ScalingType.ADDITIVE,
                increment = 50f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Max Health",
                level = 1,
                maxLevel = 8,
                currentCost = 200,
                baseCost = 200,
                costScale = 1.5f,
                currentValue = 3f,
                baseValue = 3f,
                valueScale = 1.5f,
                scalingType = ScalingType.ADDITIVE,
                increment = 1f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Coin Spawn Rate",
                level = 1,
                maxLevel = 10,
                currentCost = 300,
                baseCost = 300,
                costScale = 1.5f,
                baseValue = 1f,
                valueScale = 1.8f,
                scalingType = ScalingType.ADDITIVE,
                increment = 1f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Ruby Rate",
                level = 1,
                maxLevel = 25,
                currentCost = 180,
                baseCost = 180,
                costScale = 1.2f,
                baseValue = 1f,
                valueScale = 1.5f,
                scalingType = ScalingType.ADDITIVE,
                increment = 1f
            )
        )
        upgrades.add(
            Upgrade(
                name = "Sapphire Rate",
                level = 1,
                maxLevel = 15,
                currentCost = 180,
                baseCost = 180,
                costScale = 1.2f,
                baseValue = 1f,
                valueScale = 1.5f,
                scalingType = ScalingType.ADDITIVE,
                increment = 1f
            )
        )
        upgrades.add(
            Upgrade(
                name = "EXP Boost",
                level = 1,
                maxLevel = 6,
                currentCost = 200,
                baseCost = 200,
                costScale = 1.5f,
                baseValue = 1f,
                valueScale = 1.25f,
                scalingType = ScalingType.ADDITIVE,
                increment = 0.2f
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
        saveUpgrades()
    }

    fun levelUpUpgrade(name: String) {
        upgrades.firstOrNull { it.name == name }?.upgrade()
    }

    fun getAllUpgrades(): List<Upgrade> = upgrades

    fun hasSavedUpgrades(): Boolean = prefs.contains("upgrade_level_Jump Count")
}



