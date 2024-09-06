package net.runelite.client.plugins.jstaccbuilder.tasks.quests;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.jstaccbuilder.TradeUnlockerConfig;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.unethicalite.api.account.LocalPlayer;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.quests.Quests;

import static net.unethicalite.api.quests.QuestVarPlayer.QUEST_RUNE_MYSTERIES;

@Slf4j
public class RuneMysteries implements Task {

    private static final WorldPoint DUKE = new WorldPoint(3209, 3221, 1);
    private static final WorldPoint MAGE = new WorldPoint(3104, 9571, 0);
    private static final WorldPoint AUBURY = new WorldPoint(3253, 3401, 0);
    private static final WorldPoint DARK_WIZARDS_SW = new WorldPoint(3221, 3363, 0);
    private static final WorldPoint DARK_WIZARDS_NE = new WorldPoint(3235, 3375, 0);
    private static final WorldArea DARK_WIZARDS = new WorldArea(DARK_WIZARDS_SW, DARK_WIZARDS_NE);
    private static final int LOWER_Y_BOUND = 3355;
    private static final int UPPER_Y_BOUND = 4000;
    private final QuestUtils questUtils;
    private final TradeUnlockerConfig config;
    private int idBefore;
    private boolean handedIn;

    public RuneMysteries(QuestUtils questUtils, TradeUnlockerConfig config) {
        RuneLite.getInjector().getInstance(EventBus.class).register(this);
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
     * Checks if the Rune Mysteries quest is complete.
     * A quest is considered complete if it's marked as finished or the player has acquired a certain number of quest points.
     *
     * @return true if the quest is complete, false otherwise.
     */
    public boolean isComplete() {
        return Quests.isFinished(Quest.RUNE_MYSTERIES) || LocalPlayer.getQuestPoints() >= config.getQuestPoints();
    }

    /**
     * Executes the logic associated with the Rune Mysteries quest.
     * This method is responsible for managing interactions, dialogues, and movements based on the quest's progress.
     */
    public void run() {

        int progress = Vars.getVarp(QUEST_RUNE_MYSTERIES.getId());
        if (idBefore != progress) {
            idBefore = progress;
        }
        log.debug("Progress (" + progress + ")");

        switch (progress) {
            case 0:
                startQuest();
                break;
            case 1:
                obtainAirTalisman();
                break;
            case 2:
                deliverTalismanToSedridor();
                break;
            case 3:
                getResearchPackage();
                break;
            case 4:
                getNotesFromAubury();
                break;
            case 5:
                completeQuestWithSedridor();
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

    private void completeQuestWithSedridor() {
        if (!Inventory.contains(ItemID.RESEARCH_NOTES)) {
            questUtils.talkToNPC(n -> n.getName().equals("Aubury"), AUBURY, null);
            return;
        }
        NPC mage = NPCs.getNearest("Archmage Sedridor");
        if (mage != null) {
            handedIn = true;
        }
        questUtils.talkToNPC(n -> n.getName().equals("Archmage Sedridor"), MAGE, Dialogs.ARCHMAGE_SEDRIDOR);
    }

    private void getNotesFromAubury() {
        // Get the notes from Aubury
        if (Players.getLocal().getWorldLocation().getY() <= 3398) {
            questUtils.walkTo(AUBURY);
            return;
        }
        questUtils.talkToNPC(n -> n.getName().equals("Aubury"), AUBURY, Dialogs.AUBURY);
    }

    private void getResearchPackage() {
        // Talk to archmage sedridor to get a research package
        if (!Inventory.contains(ItemID.RESEARCH_PACKAGE)) {
            questUtils.talkToNPC(n -> n.getName().equals("Archmage Sedridor"), MAGE, Dialogs.ARCHMAGE_SEDRIDOR);
            return;
        }
        // Delivery the package to aubury in varrock
        if (Players.getLocal().getWorldLocation().getY() <= 3398) {
            questUtils.walkTo(AUBURY);
            return;
        }
        questUtils.talkToNPC(n -> n.getName().equals("Aubury"), AUBURY, Dialogs.AUBURY);
    }

    private void deliverTalismanToSedridor() {
        // Take the talsiman to archmage sedridor
        questUtils.talkToNPC(n -> n.getName().equals("Archmage Sedridor"), MAGE, Dialogs.ARCHMAGE_SEDRIDOR);
    }

    private void obtainAirTalisman() {
        // Talk to Duke  Horacio to obtain an air talisman
        if (!Inventory.contains(ItemID.AIR_TALISMAN)) {
            questUtils.talkToNPC(n -> n.getName().equals("Duke Horacio"), DUKE, Dialogs.DUKE_HORACIO);
            return;
        }
        questUtils.talkToNPC(n -> n.getName().equals("Archmage Sedridor"), MAGE, Dialogs.ARCHMAGE_SEDRIDOR);
    }

    private void startQuest() {
        // Start the Rune Mysteries quest by talking to Duke Horacio
        questUtils.talkToNPC(n -> n.getName().equals("Duke Horacio"), DUKE, Dialogs.DUKE_HORACIO);
    }

    public static final class Dialogs {
        public static final String[] DUKE_HORACIO = {"Have you any quests for me?", "Yes."};
        public static final String[] ARCHMAGE_SEDRIDOR = {"I'm looking for the head wizard.", "Okay, here you are.", "Yes, certainly.", "Go ahead."};
        public static final String[] AUBURY = {"here with a package for you"};
    }

}
