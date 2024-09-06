package net.runelite.client.plugins.jstfish;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("jstfisher")
public interface FisherConfig extends Config
{

    @ConfigItem(
            name = "Started",
            keyName = "started",
            description = "Starts the script"
    )
    default boolean started()
    {
        return false;
    }
}
