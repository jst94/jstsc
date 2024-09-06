package net.runelite.client.plugins.jstaccbuilder.tasks.quests;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.jstaccbuilder.TradeUnlockerConfig;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.unethicalite.api.account.LocalPlayer;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Inventory;

import static net.unethicalite.api.quests.QuestVarPlayer.QUEST_COOKS_ASSISTANT;

@Slf4j
public class CooksAssistant implements Task {

    private static final WorldPoint COOK_ROOM = new WorldPoint(3209, 3215, 0);
    private static final WorldPoint BASEMENT = new WorldPoint(3216, 9624, 0);
    private static final WorldPoint COW = new WorldPoint(3253, 3273, 0);
    private static final WorldPoint CHICKEN = new WorldPoint(3230, 3298, 0);
    private static final WorldPoint WHEAT_FIELD = new WorldPoint(3160, 3296, 0);
    private static final WorldPoint UPPER = new WorldPoint(3164, 3306, 2);
    private static final WorldPoint BIN = new WorldPoint(3165, 3306, 0);

    private final QuestUtils questUtils;
    private final TradeUnlockerConfig config;
    private boolean isGrainPut = false, isHopperOperated = false;
    private int idBefore;

    public CooksAssistant(QuestUtils questUtils, TradeUnlockerConfig config) {
        RuneLite.getInjector().getInstance(EventBus.class).register(this);
        this.questUtils = questUtils;
        this.config = config;
        idBefore = -1;
    }

    /**
     * @return
     */
    @Override
    public boolean validate() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public int execute() {
        return 0;
    }

    /**
     * Checks if the Cooks Assistant quest is complete.
     *
     * @return true if the quest is finished or player's quest points are >= 10; false otherwise.
     */
    public boolean isComplete() {
        return Vars.getVarp(QUEST_COOKS_ASSISTANT.getId()) == 2 || LocalPlayer.getQuestPoints() >= config.getQuestPoints();
    }

    /**
     * Main method to run the quest tasks based on the current progress.
     */
    public void run() {
        int progress = Vars.getVarp(QUEST_COOKS_ASSISTANT.getId());
        if (idBefore != progress) {
            idBefore = progress;
        }
        log.debug("Progress (" + progress + ")");

        switch (progress) {
            case 0:
                if (hasRequiredItems()) {
                    handInQuest();
                } else {
                    getRequiredItems();
                }
                break;
            case 1:
                handInQuest();
                break;
        }
    }

    @Override
    public Skill getSkill() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public boolean isBlocking() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public boolean subscribe() {
        return false;
    }

    private void getRequiredItems() {
        if (!Inventory.contains(ItemID.POT, ItemID.POT_OF_FLOUR)) {
            log.debug("Getting a Pot");
            questUtils.getTileItem(COOK_ROOM, ItemID.POT);
            return;
        }

        if (!Inventory.contains(ItemID.BUCKET, ItemID.BUCKET_OF_MILK)) {
            log.debug("Getting a Bucket");
            questUtils.getTileItem(BASEMENT, ItemID.BUCKET);
            return;
        }

        if (Inventory.contains(ItemID.BUCKET) && !Inventory.contains(ItemID.BUCKET_OF_MILK)) {
            log.debug("Milking a cow");
            questUtils.getItemFromObject(o -> o.getName().equals("Dairy cow"), "Milk", COW, ItemID.BUCKET_OF_MILK);
            return;
        }

        if (!Inventory.contains(ItemID.EGG)) {
            log.debug("Getting an Egg");
            questUtils.getTileItem(CHICKEN, ItemID.EGG);
            return;
        }

        if (!Inventory.contains(ItemID.POT_OF_FLOUR)) {
            if (!isGrainPut && !Inventory.contains(ItemID.GRAIN)) {
                log.debug("Picking some Wheat");
                questUtils.getItemFromObject(o -> o.getName().equals("Wheat"), "Pick", WHEAT_FIELD, ItemID.GRAIN);
                return;
            }

            if (!isGrainPut && !isHopperOperated && Inventory.contains(ItemID.GRAIN)) {
                fillHopper();
                return;
            }

            if (!isHopperOperated && isGrainPut) {
                operateHopper();
                return;
            }

            if (isHopperOperated && isGrainPut) {
                log.debug("Emptying Flour bin");
                questUtils.getItemFromObject(o -> o.getName().equals("Flour bin"), "Empty", BIN, ItemID.POT_OF_FLOUR);
            }
        }

    }

    private void operateHopper() {
        TileObject controls = TileObjects.getNearest(o -> o.getName().equals("Hopper controls") && o.hasAction("Operate"));
        if (controls != null) {
            log.debug("Operating hopper controls");
            controls.interact("Operate");
            Time.sleepUntil(() -> isHopperOperated && !Players.getLocal().isAnimating(), 15_000);
            return;
        }
        log.debug("Walking to top of mill");
        questUtils.walkTo(UPPER);
    }

    private void fillHopper() {
        TileObject hopper = TileObjects.getNearest(o -> o.getName().equals("Hopper") && o.hasAction("Fill"));
        if (hopper != null) {
            log.debug("Filling hopper");
            hopper.interact("Fill");
            Time.sleepUntil(() -> isGrainPut && !Players.getLocal().isAnimating(), 15_000);
            return;
        }
        log.debug("Walking to top of mill");
        questUtils.walkTo(UPPER);
    }

    private void handInQuest() {
        log.debug("Giving ingredients to Cook");

        questUtils.talkToNPC(n -> n.getName().equals("Cook"), COOK_ROOM, Dialogs.HAND_IN);
    }

    /**
     * Determines if the player has all the required items for completing the quest.
     *
     * @return True if the player has all the required items, otherwise false.
     */
    private boolean hasRequiredItems() {
        return Inventory.contains(ItemID.POT_OF_FLOUR) && Inventory.contains(ItemID.EGG) && Inventory.contains(ItemID.BUCKET_OF_MILK);
    }

    /**
     * Listens to in-game chat messages to track the status of certain quest-related actions.
     */
    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM) {
            return;
        }

        final String message = event.getMessage();
        if (message.startsWith("You put the grain in the hopper") || message.startsWith("There is already grain in the hopper")) {
            log.debug("Grain has been put in the hopper");
            isGrainPut = true;
        }
        if (message.startsWith("You operate the hopper")) {
            log.debug("Hopper has been operated");
            isHopperOperated = true;
        }

    }

    public static class Dialogs {
        public static final String[] HAND_IN = {"What's wrong?", "Can I help?", "Yes."};
    }
}