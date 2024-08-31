package net.runelite.client.plugins.jstfish;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.packets.MovementPackets;
import net.unethicalite.api.packets.NPCPackets;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "Lumbridge Shrimp Fishing",
        description = "Automatically fishes shrimps at Lumbridge using packets.",
        tags = {"fishing", "lumbridge", "shrimp", "packets"}
)
public class ShrimpFishingPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ShrimpFishingPluginConfig config;

    private static final WorldPoint FISHING_SPOT_LOCATION = new WorldPoint(3243, 3150, 0); // Adjust coordinates as needed
    private static final int SHRIMP_ITEM_ID = 317; // Item ID for shrimp, adjust if necessary

    @Override
    protected void startUp() throws Exception
    {
        // Plugin startup logic if needed
    }

    @Override
    protected void shutDown() throws Exception
    {
        // Plugin shutdown logic if needed
    }

    @Provides
    ShrimpFishingPluginConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ShrimpFishingPluginConfig.class);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (!config.enableFishing())
        {
            return;
        }

        // Drop shrimps if inventory is full
        if (isInventoryFull())
        {
            dropAllShrimps();
            return;
        }

        // Walk to the fishing spot if the player is not nearby
        if (client.getLocalPlayer().getWorldLocation().distanceTo(FISHING_SPOT_LOCATION) > 1)
        {
            walkToFishingSpot(FISHING_SPOT_LOCATION);
            return;
        }

        // Find the fishing spot NPCs
        List<NPC> fishingSpots = client.getNpcs()
                .stream()
                .filter(npc -> npc.getName().equalsIgnoreCase("Fishing spot"))
                .filter(npc -> npc.getWorldLocation().distanceTo(FISHING_SPOT_LOCATION) <= 1)
                .collect(Collectors.toList());

        // Interact with the first shrimp spot found
        if (!fishingSpots.isEmpty())
        {
            NPC shrimpSpot = fishingSpots.get(0);
            interactWithShrimpSpot(shrimpSpot);
        }
    }

    private boolean isInventoryFull()
    {
        return client.getItemContainer(InventoryID.INVENTORY).getItems().length >= 28;
    }

    private void dropAllShrimps()
    {
        Item[] items = client.getItemContainer(InventoryID.INVENTORY).getItems();
        for (Item item : items)
        {
            if (item.getId() == SHRIMP_ITEM_ID)
            {
                dropItem(item);
            }
        }
    }

    private void dropItem(Item item)
    {
        // Invoke the "Drop" action on the shrimp item
        client.invokeMenuAction(
                "Drop",
                "Shrimp",
                item.getId(),
                MenuAction.ITEM_FIRST_OPTION.getId(),
                item.getSlot(),
                9764864
        );
    }

    private void walkToFishingSpot(WorldPoint destination)
    {
        // Use the Unethical API to send a packet to walk to the specified location
        MovementPackets.sendMovement(destination.getX(), destination.getY());
    }

    private void interactWithShrimpSpot(NPC shrimpSpot)
    {
        // Send a packet to interact with the shrimp fishing spot using the "Net" action
        NPCPackets.npcAction(shrimpSpot, "Net");
    }
}
