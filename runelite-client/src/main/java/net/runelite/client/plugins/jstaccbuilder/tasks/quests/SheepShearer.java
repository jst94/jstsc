package net.runelite.client.plugins.jstaccbuilder.tasks.quests;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.jstaccbuilder.TradeUnlockerConfig;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.unethicalite.api.account.LocalPlayer;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.quests.Quests;
import net.unethicalite.api.widgets.Production;

import static net.unethicalite.api.quests.QuestVarPlayer.QUEST_SHEEP_SHEARER;

@Slf4j
public class SheepShearer implements Task {

    // Quest positions and areas.
    private static final WorldPoint FRED = new WorldPoint(3189, 3274, 0);
    private static final WorldPoint SHEEP = new WorldPoint(3201, 3267, 0);
    private final WorldArea SPINNING_ROOM = new WorldArea(3208, 3212, 3, 3, 1);
    private final WorldPoint SPINNING_ROOM_WALK = new WorldPoint(3209, 3213, 1);

    private final QuestUtils questUtils;
    private final TradeUnlockerConfig config;
    private int idBefore;

    public SheepShearer(QuestUtils questUtils, TradeUnlockerConfig config) {
        RuneLite.getInjector().getInstance(EventBus.class).register(this);
        idBefore = -1;
        this.questUtils = questUtils;
        this.config = config;
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
     * Checks if the "Sheep Shearer" quest is complete.
     *
     * @return true if the quest is complete or if player has acquired a certain number of quest points, false otherwise.
     */
    public boolean isComplete() {
        return Quests.isFinished(Quest.SHEEP_SHEARER) || LocalPlayer.getQuestPoints() >= config.getQuestPoints();
    }

    /**
     * Executes the logic associated with different stages of the "Sheep Shearer" quest.
     * Manages interactions, dialogues, and actions based on the quest's progress.
     */
    public void run() {

        int progress = Vars.getVarp(QUEST_SHEEP_SHEARER.getId());
        if (idBefore != progress) {
            idBefore = progress;
        }
        log.debug("Progress (" + progress + ")");

        switch (progress) {
            case 0:
                startTheQuest();
                break;
            case 1:
                completeTheQuest();
                break;
            case 20:
                questUtils.talkToNPC(n -> n.getName().equals("Fred the Farmer"), FRED, Dialogs.FRED);
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

    /**
     * Completes the "Sheep Shearer" quest by obtaining wool, spinning it into balls, and then handing them in.
     */
    private void completeTheQuest() {
        if (Inventory.contains(ItemID.SHEARS)) {
            if (Inventory.contains(ItemID.BALL_OF_WOOL) && Inventory.getCount(ItemID.BALL_OF_WOOL) >= 20) {
                handInQuest();
                return;
            }
            getBallsOfWool();
            return;
        }
        // get Shears by talking to Fred the Farmer
        questUtils.talkToNPC(n -> n.getName().equals("Fred the Farmer"), FRED, Dialogs.FRED);
    }

    /**
     * Shears sheep to obtain wool. If the player has enough wool, it then
     * triggers the wool spinning process to turn the wool into balls.
     */
    private void getBallsOfWool() {
        if (shouldShear()) {
            // Obtain wool by shearing sheep
            log.debug("Shearing sheep");
            questUtils.getItemFromNPC(n -> n.getName().equals("Sheep") && n.hasAction("Shear") && !n.hasAction("Talk-to"), "Shear", SHEEP, ItemID.WOOL);
            return;
        }

        // Spin the wool into balls
        spinBallsOfWool();
    }

    /**
     * Spins wool into balls of wool using the spinning wheel. This method
     * handles movement to the spinning room, interaction with the spinning wheel,
     * and monitoring the production window to ensure the wool is spun.
     */
    private void spinBallsOfWool() {
        if (!SPINNING_ROOM.contains(Players.getLocal())) {
            log.debug("Walk to spinning room");
            questUtils.walkTo(SPINNING_ROOM_WALK);
            return;
        }

        if (Production.isOpen()) {
            log.debug("Spin balls of wool");
            Production.chooseOption(1);
            Time.sleepUntil(() -> !Inventory.contains(ItemID.WOOL), 60_000);
            return;
        }

        log.debug("Interact with spinning wheel");
        TileObject spinningWheel = TileObjects.getNearest("Spinning wheel");
        if (spinningWheel != null) {
            spinningWheel.interact("Spin");
            Time.sleepUntil(Production::isOpen, 10_000);
        }
    }

    /**
     * Hands in the balls of wool to complete the quest.
     */
    private void handInQuest() {
        questUtils.talkToNPC(n -> n.getName().equals("Fred the Farmer"), FRED, Dialogs.FRED);
    }

    /**
     * Starts the "Sheep Shearer" quest by initiating a conversation with Fred the Farmer.
     */
    private void startTheQuest() {
        log.debug("Start quest");
        questUtils.talkToNPC(n -> n.getName().equals("Fred the Farmer"), FRED, Dialogs.FRED);
    }

    /**
     * Checks if the player should shear more sheep based on the current amount of wool and balls of wool in inventory.
     *
     * @return true if more shearing is needed, false otherwise.
     */
    private boolean shouldShear() {
        int wool = Inventory.getCount(ItemID.WOOL);
        int balls = Inventory.getCount(ItemID.BALL_OF_WOOL);
        return wool + balls < 20;
    }

    public static final class Dialogs {
        public static final String[] FRED = {"looking for a quest", "Yes."};
    }


}
