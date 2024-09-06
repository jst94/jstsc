package net.runelite.client.plugins.jstaccbuilder.tasks.quests;

import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
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
import net.unethicalite.api.widgets.Dialog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

@Singleton
@Slf4j
public class QuestUtils {

    private final BankLocation[] FREE_BANKS = {BankLocation.AL_KHARID_BANK, BankLocation.DRAYNOR_BANK, BankLocation.FALADOR_WEST_BANK,
            BankLocation.FALADOR_EAST_BANK, BankLocation.LUMBRIDGE_BANK, BankLocation.VARROCK_EAST_BANK, BankLocation.VARROCK_WEST_BANK,
            BankLocation.EDGEVILLE_BANK, BankLocation.AL_KHARID_BANK, BankLocation.DUEL_ARENA_BANK};
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Interacts with an NPC matching the provided filter.
     * If the NPC is within interaction distance and reachable, the method attempts to initiate dialogue.
     * If the NPC is not nearby, the method will walk to the provided NPC position.
     *
     * @param npcFilter   Predicate to filter the desired NPC.
     * @param npcPosition WorldPoint representing the NPC's position.
     */
    public void talkToNPC(Predicate<NPC> npcFilter, WorldPoint npcPosition, String[] dialogOptions) {
        NPC npc = NPCs.getNearest(npcFilter);
        if (npc != null && npc.distanceTo(Players.getLocal()) <= 15 && Reachable.isInteractable(npc)) {
            if (Dialog.isOpen()) {
                if (Dialog.canContinue()) {
                    log.debug("Dialog - Continue");
                    Dialog.continueSpace();
                } else {
                    log.debug("Dialog - Choose option");
                    Dialog.chooseOption(dialogOptions);
                }
                return;
            }
            log.debug("Talking to");
            npc.interact("Talk-to");
            Time.sleepUntil(Dialog::isOpen, 15_000);
            return;
        }
        if (npc != null && npc.distanceTo(Players.getLocal()) <= 15) {
            walkTo(npc.getWorldLocation());
            return;
        }
        if (!Players.getLocal().getWorldLocation().equals(npcPosition)) {
            walkTo(npcPosition);
        }
    }

    /**
     * Makes the player character walk to the specified WorldPoint.
     * The method ensures that the character stops walking before it concludes.
     *
     * @param npcPosition The WorldPoint destination to walk to.
     */
    public void walkTo(WorldPoint npcPosition) {
        Movement.walkTo(npcPosition);
        Time.sleepUntil(() -> !Movement.isWalking(), Movement.isRunEnabled() ? 2_500 : 4_500);
    }

    /**
     * Interacts with a game object matching the provided filter to retrieve an item.
     * If the object is within interaction distance and reachable, the method attempts to get the item.
     * If the object is not nearby, the method will walk to the provided position.
     *
     * @param objectFilter Predicate to filter the desired game object.
     * @param interaction  The type of interaction to perform on the object (e.g., "Open").
     * @param position     WorldPoint representing the object's position.
     * @param itemID       The ID of the item to be retrieved.
     */
    public void getItemFromObject(Predicate<TileObject> objectFilter, String interaction, WorldPoint position, int itemID) {
        TileObject object = TileObjects.getNearest(objectFilter);
        if (object != null && object.distanceTo(Players.getLocal()) <= 15 && Reachable.isInteractable(object)) {
            int amountBefore = Inventory.contains(itemID) ? Inventory.getCount(itemID) : 0;
            object.interact(interaction);
            Time.sleepUntil(() -> (Inventory.contains(itemID) && Inventory.getCount(itemID) > amountBefore) && !Players.getLocal().isAnimating(), 15_000);
            return;
        }
        walkTo(position);
    }

    /**
     * Attempts to obtain an item from an NPC through a specific interaction.
     * This method will first locate an NPC based on the provided filter. If the NPC
     * is found, close enough, and interactable, the method will initiate the
     * specified interaction with the NPC, then wait for the item to appear in the
     * inventory. If the NPC is not immediately nearby or not interactable, the
     * method will guide the player towards the given position.
     *
     * @param npcFilter   A filter to locate the desired NPC.
     * @param interaction The interaction (e.g., "Talk-to", "Shear") to be performed with the NPC.
     * @param position    The WorldPoint position where the NPC is expected to be found.
     *                    The player will walk to this position if the NPC is not immediately nearby.
     * @param itemID      The ID of the item expected to be received from the NPC.
     */
    public void getItemFromNPC(Predicate<NPC> npcFilter, String interaction, WorldPoint position, int itemID) {
        NPC npc = NPCs.getNearest(npcFilter);
        if (npc != null && npc.distanceTo(Players.getLocal()) <= 15 && Reachable.isInteractable(npc)) {
            int amountBefore = Inventory.contains(itemID) ? Inventory.getCount(itemID) : 0;
            npc.interact(interaction);
            Time.sleepUntil(() -> (Inventory.contains(itemID) && Inventory.getCount(itemID) > amountBefore) && !Players.getLocal().isAnimating(), 15_000);
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
     * Retrieves the nearest bank for the given location.
     *
     * @return The nearest bank location.
     */
    public BankLocation getNearestBank(WorldPoint location) {
        Future<BankLocation> future = executorService.submit(() -> {
            BankLocation nearestBank = null;
            int nearestDistance = Integer.MAX_VALUE;
            for (BankLocation bank : FREE_BANKS) {
                WorldPoint bankCenter = bank.getArea().getCenter();
                int distance = Movement.calculateDistance(location, bankCenter);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestBank = bank;
                }
            }
            return nearestBank;
        });

        try {
            return future.get();  // This will block until the calculation is done
        } catch (Exception e) {
            // Handle exceptions
        }
        return null;
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

}
