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
            keyName = "runFullDay",
            name = "Run Full Day",
            description = "End only at 24hr run time"
    )
    default boolean shouldRunFullDay() {
        return true;
    }

}
