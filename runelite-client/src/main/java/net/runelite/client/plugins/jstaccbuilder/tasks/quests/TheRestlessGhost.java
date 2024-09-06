package net.runelite.client.plugins.jstaccbuilder.tasks.quests;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.jstaccbuilder.TradeUnlockerConfig;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.unethicalite.api.account.LocalPlayer;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.quests.Quests;

import static net.unethicalite.api.quests.QuestVarPlayer.QUEST_THE_RESTLESS_GHOST;

@Slf4j
public class TheRestlessGhost implements Task {

    private static final WorldPoint CHURCH = new WorldPoint(3241, 3206, 0);
    private static final WorldPoint URHNEY = new WorldPoint(3147, 3175, 0);
    private static final WorldPoint GRAVE = new WorldPoint(3248, 3193, 0);
    private static final WorldPoint ALTAR = new WorldPoint(3119, 9566, 0);

    private static final WorldPoint GRAVE_SW = new WorldPoint(3247, 3190, 0);
    private static final WorldPoint GRAVE_NE = new WorldPoint(3252, 3195, 0);
    private static final WorldArea GRAVE_AREA = new WorldArea(GRAVE_SW, GRAVE_NE);

    private final QuestUtils questUtils;
    private final TradeUnlockerConfig config;
    private int idBefore;

    public TheRestlessGhost(QuestUtils questUtils, TradeUnlockerConfig config) {
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
     * Checks if the "The Restless Ghost" quest is complete.
     *
     * @return true if the quest is complete or if player has acquired a certain number of quest points, false otherwise.
     */
    public boolean isComplete() {
        return Quests.isFinished(Quest.THE_RESTLESS_GHOST) || LocalPlayer.getQuestPoints() >= config.getQuestPoints();
    }

    /**
     * Executes the logic associated with different stages of "The Restless Ghost" quest.
     * Manages interactions, dialogues, and actions based on the quest's progress.
     */
    public void run() {
        int progress = Vars.getVarp(QUEST_THE_RESTLESS_GHOST.getId());
        if (idBefore != progress) {
            idBefore = progress;
        }
        log.debug("Progress (" + progress + ")");
        switch (progress) {
            case 0:
                startQuest();
                break;
            case 1:
                obtainGhostspeakAmulet();
                break;
            case 2:
                talkToGhost();
                break;
            case 3:
                getGhostsSkull();
                break;
            case 4:
                completeQuestByUsingSkull();
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
     * Uses the ghost's skull on the coffin, completing the quest.
     */
    private void completeQuestByUsingSkull() {
        if (!GRAVE_AREA.contains(Players.getLocal())) {
            questUtils.walkTo(GRAVE);
            return;
        }
        TileObject coffinFinal = TileObjects.getNearest("Coffin");
        if (coffinFinal != null) {
            if (coffinFinal.hasAction("Open")) {
                coffinFinal.interact("Open");
                Time.sleepUntil(() -> TileObjects.getNearest(o -> o.getName().equals("Coffin") && o.hasAction("Search")) != null && Players.getLocal().isAnimating(), 5000);
                return;
            }
            Inventory.getFirst(ItemID.GHOSTS_SKULL).useOn(coffinFinal);
            Time.sleepUntil(Game::isInCutscene, 10_000);
        }
    }

    /**
     * Retrieves the ghost's skull from the Wizards' Tower basement.
     */
    private void getGhostsSkull() {
        questUtils.getItemFromObject(o -> o.getName().equals("Altar") && o.hasAction("Search"), "Search", ALTAR, ItemID.GHOSTS_SKULL);
    }

    /**
     * Equips the Ghostspeak amulet, opens the coffin, and initiates a conversation with the ghost.
     */
    private void talkToGhost() {
        if (Inventory.contains(ItemID.GHOSTSPEAK_AMULET)) {
            Inventory.getFirst(ItemID.GHOSTSPEAK_AMULET).interact("Wear");
            Time.sleepUntil(() -> !Inventory.contains(ItemID.GHOSTSPEAK_AMULET), 3000);
            return;
        }
        NPC ghost = NPCs.getNearest(n -> !n.getName().equals("Restless ghost") && !n.isAnimating() && n.isMoving());
        if (ghost != null) {
            questUtils.talkToNPC(n -> n.getName().equals("Restless ghost"), GRAVE, Dialogs.RESTLESS_GHOST);
            return;
        }

        TileObject coffin = TileObjects.getNearest(o -> o.getName().equals("Coffin") && o.hasAction("Open") || (o.hasAction("Search") && o.hasAction("Close")));
        if (coffin != null && coffin.distanceTo(Players.getLocal()) < 15 && Reachable.isInteractable(coffin)) {
            coffin.interact(0);
            Time.sleepUntil(() -> NPCs.getNearest(n -> !n.getName().equals("Restless ghost") && !n.isAnimating() && n.isMoving()) != null, 5000);
            return;
        }
        questUtils.walkTo(GRAVE);
    }

    /**
     * Obtains the Ghostspeak amulet from Father Urhney.
     */
    private void obtainGhostspeakAmulet() {

        questUtils.talkToNPC(n -> n.getName().equals("Father Urhney"), URHNEY, Dialogs.FATHER_URHNEY);
    }

    /**
     * Starts "The Restless Ghost" quest by initiating a conversation with Father Aereck.
     */
    private void startQuest() {
        questUtils.talkToNPC(n -> n.getName().equals("Father Aereck"), CHURCH, Dialogs.FATHER_AERECK);
    }

    public static final class Dialogs {
        public static final String[] FATHER_AERECK = {"looking for a quest", "Yes."};
        public static final String[] FATHER_URHNEY = {"Father Aereck sent me to talk to you", "got a ghost haunting his graveyard"};
        public static final String[] RESTLESS_GHOST = {"now tell me what the problem is"};
    }
}
