package net.runelite.client.plugins.jstaccbuilder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tradeunlocker")
public interface TradeUnlockerConfig extends Config {

    @ConfigItem(
            keyName = "totalLevel",
            name = "Total level",
            description = "What total level to end at"
    )
    default int getTotalLevel() {
        return 1000;
    }

    @ConfigItem(
            keyName = "questPoints",
            name = "Quest Points",
            description = "What QP to end at"
    )
    default int getQuestPoints() {
        return 100;
    }

    @ConfigItem(
            keyName = "attackLevel",
            name = "Attack Level",
            description = "What Attack level to end at"
    )
    default int getAttackLevel() {
        return 50;
    }

    @ConfigItem(
            keyName = "strengthLevel",
            name = "Strength Level",
            description = "What Strength level to end at"
    )
    default int getStrengthLevel() {
        return 50;
    }

    @ConfigItem(
            keyName = "defenceLevel",
            name = "Defence Level",
            description = "What Defence level to end at"
    )
    default int getDefenceLevel() {
        return 50;
    }

    @ConfigItem(
            keyName = "rangedLevel",
            name = "Ranged Level",
            description = "What Ranged level to end at"
    )
    default int getRangedLevel() {
        return 50;
    }

    @ConfigItem(
            keyName = "magicLevel",
            name = "Magic Level",
            description = "What Magic level to end at"
    )
    default int getMagicLevel() {
        return 50;
    }

    @ConfigItem(
            keyName = "runFullDay",
            name = "Run Full Day",
            description = "End only at 24hr run time"
    )
    default boolean shouldRunFullDay() {
        return true;
    }

}