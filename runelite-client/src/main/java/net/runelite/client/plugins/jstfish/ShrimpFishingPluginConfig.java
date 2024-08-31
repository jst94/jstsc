package net.runelite.client.plugins.jstfish;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("shrimpfishing")
public interface ShrimpFishingPluginConfig extends Config
{
    @ConfigItem(
            keyName = "enableFishing",
            name = "Enable Fishing",
            description = "Enable or disable automatic shrimp fishing."
    )
    default boolean enableFishing()
    {
        return true;
    }
}
