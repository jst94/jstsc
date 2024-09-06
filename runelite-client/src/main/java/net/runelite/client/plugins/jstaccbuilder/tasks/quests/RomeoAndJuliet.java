package net.runelite.client.plugins.jstaccbuilder.tasks.quests;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.jstaccbuilder.TradeUnlockerConfig;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.unethicalite.api.account.LocalPlayer;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.quests.Quests;
import net.unethicalite.api.widgets.Dialog;

import static net.unethicalite.api.quests.QuestVarPlayer.QUEST_ROMEO_AND_JULIET;

@Slf4j
public class RomeoAndJuliet implements Task {

    private static final WorldPoint JULIET = new WorldPoint(3158, 3426, 1);
    private static final WorldPoint ROMEO = new WorldPoint(3213, 3424, 0);
    private static final WorldPoint FATHER_LAWRENCE = new WorldPoint(3254, 3481, 0);
    private static final WorldPoint CADAVAS = new WorldPoint(3268, 3368, 0);
    private static final WorldPoint APOTHECARY = new WorldPoint(3193, 3403, 0);

    private final QuestUtils questUtils;
    private final TradeUnlockerConfig config;
    private boolean passedSermonCutscene;
    private boolean wait;
    private int idBefore;

    public RomeoAndJuliet(QuestUtils questUtils, TradeUnlockerConfig config) {
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
     * Checks if the Romeo and Juliet quest is complete.
     *
     * @return true if the quest is finished or player's quest points are >= 10; false otherwise.
     */
    public boolean isComplete() {
        return Quests.isFinished(Quest.ROMEO__JULIET) || LocalPlayer.getQuestPoints() >= config.getQuestPoints();
    }

    /**
     * Main method to run the quest tasks based on the current progress.
     */
    public void run() {
        int progress = Vars.getVarp(QUEST_ROMEO_AND_JULIET.getId());
        if (idBefore != progress) {
            idBefore = progress;
        }
        log.debug("Progress (" + progress + ")");

        switch (progress) {
            case 0:
                talkToRomeo();
                break;
            case 10:
                talkToJuliet();
                break;
            case 20:
                progressRomeoConversation();
                break;
            case 30:
                talkToFatherLawrence();
                break;
            case 40:
                talkToApothecary();
                break;
            case 50:
                handleCadavaPotionCutscene();
                break;
            case 60:
                concludeQuest();
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
     * Concludes the Romeo and Juliet quest by handling final interactions.
     */
    private void concludeQuest() {
        log.debug("Concluding Romeo and Juliet quest");
        if (Game.isInCutscene()) {
            wait = true;
            if (Dialog.isOpen() && Dialog.canContinue()) {
                Dialog.continueSpace();
            }
            return;
        }
        if (wait) {
            WorldPoint julietCheck = new WorldPoint(3157, 3425, 1);
            if (Players.getLocal().getWorldLocation().equals(julietCheck)) {
                wait = false;
            }
            return;
        }
        questUtils.talkToNPC(n -> n.getName().equals("Romeo"), ROMEO, Dialogs.CONCLUDE_QUEST);
    }

    /**
     * Handles cutscene interactions during the Cadava Potion segment of the quest.
     */
    private void handleCadavaPotionCutscene() {
        if (Game.isInCutscene()) {
            log.debug("Handling Cadava Potion cutscene");
            if (Dialog.isOpen() && Dialog.canContinue()) {
                Dialog.continueSpace();
            }
            return;
        }
        if (!Inventory.contains(ItemID.CADAVA_POTION)) {
            log.debug("Getting a cadava potion from Apothecary");
            questUtils.talkToNPC(n -> n.getName().equals("Apothecary"), APOTHECARY, Dialogs.TALK_TO_APOTHECARY);
            return;
        }

        log.debug("Giving potion to Juliet");
        questUtils.talkToNPC(n -> n.getName().equals("Juliet"), JULIET, null);
    }

    /**
     * Interacts with the Apothecary character during the quest.
     * Manages dialogue, checks if the player has Cadava berries, and enqueues necessary dialogues.
     */
    private void talkToApothecary() {
        if (!Inventory.contains(ItemID.CADAVA_BERRIES)) {
            log.debug("Getting a Cadava Berry");
            questUtils.getItemFromObject(o -> o.getId() == 23625 || o.getId() == 23626, "Pick-from", CADAVAS, ItemID.CADAVA_BERRIES);
            return;
        }

        if (Dialog.isOpen() && !Dialog.canContinue() && !Dialog.isViewingOptions()) {
            return;
        }

        log.debug("Interacting with Apothecary");
        questUtils.talkToNPC(n -> n.getName().equals("Apothecary"), APOTHECARY, Dialogs.TALK_TO_APOTHECARY);
    }

    /**
     * Interacts with the Father Lawrence character during the quest.
     * Manages dialogues during different stages of the quest and checks for cutscenes.
     */
    private void talkToFatherLawrence() {
        log.debug("Talking to Father Lawrence");
        if (Dialog.isOpen() && !Dialog.canContinue() && !Dialog.isViewingOptions()) {
            passedSermonCutscene = true;
            return;
        }
        if (passedSermonCutscene) {

            questUtils.talkToNPC(n -> n.getName().equals("Father Lawrence"), FATHER_LAWRENCE, null);
            return;
        }

        questUtils.talkToNPC(n -> n.getName().equals("Father Lawrence"), FATHER_LAWRENCE, null);
    }

    /**
     * Progresses the conversation with Romeo during the quest.
     * Enqueues dialogues and continues the conversation based on the quest progress.
     */
    private void progressRomeoConversation() {
        log.debug("Continuing conversation with Romeo");

        questUtils.talkToNPC(n -> n.getName().equals("Romeo"), ROMEO, Dialogs.TALK_TO_ROMEO);
    }

    /**
     * Interacts with Juliet during the quest.
     * Manages dialogues and enqueues necessary dialogues based on the quest progress.
     */
    private void talkToJuliet() {
        log.debug("Talking to Juliet");

        questUtils.talkToNPC(n -> n.getName().equals("Juliet"), JULIET, null);
    }

    /**
     * Initiates or continues the interaction with Romeo.
     * Manages dialogues and enqueues the necessary dialogues based on the quest progress.
     */
    private void talkToRomeo() {
        log.debug("Talking to Romeo");
        questUtils.talkToNPC(n -> n.getName().equals("Romeo"), ROMEO, Dialogs.TALK_TO_ROMEO);
    }

    public static final class Dialogs {
        public static final String[] TALK_TO_ROMEO = {"Perhaps I could help to find her for you?", "Yes.", "Yes, ok, I'll let her know.", "Ok, thanks."};
        public static final String[] TALK_TO_APOTHECARY = {"Talk about something else.", "Talk about Romeo & Juliet.", "Ok, thanks."};
        public static final String[] CONCLUDE_QUEST = {"What's Wrong?", "Can I help?"};
    }
}
