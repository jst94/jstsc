package net.runelite.client.plugins.jstaccbuilder.tasks.skills;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;

@Slf4j
public class SkillUtils {
    private final Client client;
    private final BankLocation[] FREE_BANKS = {BankLocation.AL_KHARID_BANK, BankLocation.DRAYNOR_BANK, BankLocation.FALADOR_WEST_BANK,
            BankLocation.FALADOR_EAST_BANK, BankLocation.LUMBRIDGE_BANK, BankLocation.VARROCK_EAST_BANK, BankLocation.VARROCK_WEST_BANK,
            BankLocation.EDGEVILLE_BANK, BankLocation.AL_KHARID_BANK, BankLocation.DUEL_ARENA_BANK};

    public SkillUtils(Client client) {
        this.client = client;
    }

    /**
     * Walks the player's character to a specific world point.
     *
     * @param position The world point to walk to.
     */
    public void walkTo(WorldPoint position) {
        Movement.walkTo(position);
        Time.sleepUntil(() -> !Movement.isWalking(), Movement.isRunEnabled() ? 2_500 : 4_500);
    }

    /**
     * Retrieves an item from an object in the world by interacting with it.
     *
     * @param objectFilter Predicate to filter relevant objects.
     * @param interaction  The interaction to perform on the object (e.g., "Take").
     * @param position     The world point to walk to if the object isn't nearby.
     * @param itemID       The ID of the item to retrieve.
     */
    public void getItemFromObject(java.util.function.Predicate<TileObject> objectFilter, String interaction, WorldPoint position, int itemID) {
        TileObject object = TileObjects.getNearest(objectFilter);
        if (object != null && object.distanceTo(Players.getLocal()) <= 15 && Reachable.isInteractable(object)) {
            int amountBefore = Inventory.getCount(itemID);
            object.interact(interaction);
            Time.sleepUntil(() -> Inventory.getCount(itemID) > amountBefore && !Players.getLocal().isAnimating(), 15_000);
            return;
        }
        Movement.walkTo(position);
        Time.sleepUntil(() -> !Movement.isWalking(), Movement.isRunEnabled() ? 2_500 : 4_500);
    }

    /**
     * Attempts to pick up a tile item. If the item is nearby and interactable,
     * it will be picked up. If the item is not immediately nearby, the method
     * guides the player towards the given position.
     *
     * @param position The WorldPoint position where the item is expected to be found.
     * @param lootID   The ID of the tile item to be picked up.
     */
    public void getTileItem(WorldPoint position, int lootID) {
        TileItem loot = TileItems.getNearest(lootID);
        if (loot != null && loot.distanceTo(Players.getLocal()) < 15 && Reachable.isInteractable(loot)) {
            int amountBefore = Inventory.contains(lootID) ? Inventory.getCount(lootID) : 0;
            loot.pickup();
            Time.sleepUntil(() -> Inventory.contains(lootID) && Inventory.getCount(lootID) > amountBefore, 10_000);
            return;
        }
        Movement.walkTo(position);
        Time.sleepUntil(() -> !Movement.isWalking(), 2500);
    }

    /**
     * Tries to open the nearest bank.
     */
    public void openBank() {
        TileObject bankObj = TileObjects.getNearest(o ->
                (o.getName().equals("Bank booth") && o.hasAction("Bank"))
                        || (o.getName().equals("Bank chest") && o.hasAction("Use"))

        );
        if (bankObj != null) {
            bankObj.interact("Bank", "Use");
            Time.sleepUntil(Bank::isOpen, 10000);
            return;
        }

        NPC bankNpc = NPCs.getNearest(n -> n.hasAction("Bank"));
        if (bankNpc != null) {
            bankNpc.interact("Bank");
            Time.sleepUntil(Bank::isOpen, 10000);
        }
    }

    /**
     * Retrieves the nearest bank for the given location.
     *
     * @return The nearest bank location.
     */
    public BankLocation getNearestBank(WorldPoint location) {
        BankLocation nearestBank = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (BankLocation bank : FREE_BANKS) {
            int distance = Movement.calculateDistance(location, bank.getArea());
            log.debug("Checking bank: " + bank + ", Distance: " + distance);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestBank = bank;
            }
        }
        log.debug("Nearest bank found: " + nearestBank);
        return nearestBank;
    }
}
