package net.runelite.client.plugins.jsttut.tasks;

import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.runelite.client.plugins.jsttut.TutorialIslandPlugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Tab;
import net.unethicalite.api.widgets.Tabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuilinorGuideTask implements Task {
    public static final int GIELINOR_GUIDE_ID = 3308;
    private static final Logger log = LoggerFactory.getLogger(GuilinorGuideTask.class);
    private TutorialIslandPlugin plugin = new TutorialIslandPlugin();

    public GuilinorGuideTask() {
        this.plugin = plugin;
    }

    public boolean validate() {
        return this.plugin.getMainState() == TutorialIslandPlugin.MainState.GIELINOR_GUIDE;
    }

    public int execute() {
        log.info("Starting Gielinor Guide task...");
        if (this.plugin.getMainState() == TutorialIslandPlugin.MainState.GIELINOR_GUIDE) {
            this.handleGielinorGuideInteraction();
        }

        return 1000;
    }

    /**
     * @return
     */
    @Override
    public boolean isComplete() {
        return false;
    }

    /**
     *
     */
    @Override
    public void run() {

    }

    /**
     * @return
     */
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

    private void handleGielinorGuideInteraction() {
        log.info("Interacting with Gielinor Guide...");
        if (!Tabs.isOpen(Tab.OPTIONS)) {
            log.info("Opening Settings tab...");
            Tabs.open(Tab.OPTIONS);
            Time.sleepUntil(() -> {
                return Tabs.isOpen(Tab.OPTIONS);
            }, 3000);
        } else {
            NPC guide = NPCs.getNearest(new int[]{3308});
            if (guide != null) {
                guide.interact("Talk-to");
                log.info("Attempted interaction with the Gielinor Guide. Waiting for dialogue...");
                if (Time.sleepUntil(Dialog::isOpen, 3000)) {
                    log.info("Dialogue opened successfully.");
                    this.handleDialogue();
                } else {
                    log.warn("Dialogue did not open after interacting with the Gielinor Guide.");
                }
            } else {
                log.warn("Gielinor Guide not found.");
            }

        }
    }

    private void handleDialogue() {
        if (Dialog.isOpen()) {
            log.info("Dialogue detected.");

            while(Dialog.canContinue()) {
                log.info("Continuing dialogue...");
                Dialog.continueSpace();
                Time.sleep(500L);
            }

            if (Dialog.isViewingOptions()) {
                log.info("Selecting option: 'I am an experienced player'.");
                Dialog.chooseOption(new String[]{"I am an experienced player."});
                Time.sleep(500L);
            }

            if (!Dialog.isOpen()) {
                log.info("Dialogue complete. Proceeding to interact with the door.");
                this.interactWithDoor();
            } else {
                log.warn("Dialogue is still open. Waiting for further interaction.");
            }
        } else {
            log.warn("Dialogue not detected.");
        }

    }

    private void interactWithDoor() {
    }
}
