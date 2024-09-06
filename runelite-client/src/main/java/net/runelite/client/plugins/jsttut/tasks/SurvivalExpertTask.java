package net.runelite.client.plugins.jsttut.tasks;

import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.runelite.client.plugins.jsttut.TutorialIslandPlugin;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.widgets.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurvivalExpertTask implements Task {
    private static final Logger log = LoggerFactory.getLogger(SurvivalExpertTask.class);
    public static final int SURVIVAL_EXPERT_ID = 8503;
    public static final int FISHING_SPOT_ID = 3317;
    private TutorialIslandPlugin plugin;
    private SurvivalExpertStep currentStep;

    public SurvivalExpertTask() {
        this.currentStep = SurvivalExpertStep.TALK_TO_EXPERT;
        this.plugin = plugin;
    }

    public boolean validate() {
        return this.plugin.getMainState() == TutorialIslandPlugin.MainState.SURVIVAL_EXPERT;
    }

    public int execute() {
        log.info("Starting Survival Expert task...");
        if (Dialog.isOpen()) {
            this.plugin.handleDialogue();
            return 1000;
        } else {
            switch (this.currentStep) {
                case TALK_TO_EXPERT:
                    this.interactWithSurvivalExpert();
                    break;
                case CATCH_SHRIMP:
                    this.catchShrimp();
                    break;
                case TALK_TO_EXPERT_AGAIN:
                    this.interactWithSurvivalExpert();
                    break;
                case CHOP_TREE:
                    this.chopTree();
                    break;
                case USE_TINDERBOX_ON_LOGS:
                    this.useTinderboxOnLogs();
                    break;
                case COOK_SHRIMP:
                    this.cookShrimp();
                    break;
                default:
                    log.warn("Unknown step in Survival Expert task.");
            }

            return 1000;
        }
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

    private void interactWithSurvivalExpert() {
        NPC expert = NPCs.getNearest(new int[]{8503});
        if (expert != null) {
            expert.interact("Talk-to");
            log.info("Attempted interaction with the Survival Expert.");
            if (Time.sleepUntil(Dialog::isOpen, 3000)) {
                log.info("Dialogue opened successfully.");
                this.plugin.handleDialogue();
                this.advanceStep();
            } else {
                log.warn("Dialogue did not open after interacting with the Survival Expert.");
            }
        } else {
            log.warn("Survival Expert NPC not found.");
        }

    }

    private void advanceStep() {
        switch (this.currentStep) {
            case TALK_TO_EXPERT:
                this.currentStep = SurvivalExpertTask.SurvivalExpertStep.CATCH_SHRIMP;
                break;
            case CATCH_SHRIMP:
                this.currentStep = SurvivalExpertTask.SurvivalExpertStep.TALK_TO_EXPERT_AGAIN;
                break;
            case TALK_TO_EXPERT_AGAIN:
                this.currentStep = SurvivalExpertTask.SurvivalExpertStep.CHOP_TREE;
                break;
            case CHOP_TREE:
                this.currentStep = SurvivalExpertTask.SurvivalExpertStep.USE_TINDERBOX_ON_LOGS;
                break;
            case USE_TINDERBOX_ON_LOGS:
                this.currentStep = SurvivalExpertTask.SurvivalExpertStep.COOK_SHRIMP;
                break;
            case COOK_SHRIMP:
                log.info("Survival Expert task completed.");
                this.plugin.setMainState(TutorialIslandPlugin.MainState.COOKING_GUIDE_SECTION);
                break;
            default:
                log.warn("Cannot advance step from unknown step.");
        }

    }

    private void catchShrimp() {
        if (Inventory.contains(new String[]{"Raw shrimps"})) {
            log.info("Shrimps caught. Moving to next step.");
            this.advanceStep();
        } else {
            NPC fishingSpot = NPCs.getNearest(new int[]{3317});
            if (fishingSpot != null && fishingSpot.hasAction(new String[]{"Net"})) {
                fishingSpot.interact("Net");
                log.info("Interacting with the fishing spot.");
                if (Time.sleepUntil(() -> {
                    return Inventory.contains(new String[]{"Raw shrimps"});
                }, 5000)) {
                    log.info("Shrimps caught successfully.");
                    this.advanceStep();
                } else {
                    log.warn("Failed to catch shrimp.");
                }
            } else {
                log.warn("Fishing spot not found or doesn't have 'Net' action.");
            }
        }

    }

    private void chopTree() {
        if (Inventory.contains(new String[]{"Logs"})) {
            log.info("Logs obtained. Moving to next step.");
            this.advanceStep();
        } else {
            TileObject tree = TileObjects.getNearest(new String[]{"Tree"});
            if (tree != null && tree.hasAction(new String[]{"Chop down"})) {
                tree.interact("Chop down");
                if (Time.sleepUntil(() -> {
                    return Inventory.contains(new String[]{"Logs"});
                }, 5000)) {
                    log.info("Logs obtained successfully.");
                    this.advanceStep();
                } else {
                    log.warn("Failed to chop tree.");
                }
            } else {
                log.warn("Tree not found or doesn't have 'Chop down' action.");
            }
        }

    }

    private void useTinderboxOnLogs() {
        TileObject fire = TileObjects.getNearest(new String[]{"Fire"});
        if (fire != null) {
            log.info("Fire created. Moving to next step.");
            this.advanceStep();
        } else if (Inventory.contains(new String[]{"Tinderbox"}) && Inventory.contains(new String[]{"Logs"})) {
            Inventory.getFirst(new String[]{"Tinderbox"}).useOn(Inventory.getFirst(new String[]{"Logs"}));
            if (Time.sleepUntil(() -> {
                return TileObjects.getNearest(new String[]{"Fire"}) != null;
            }, 5000)) {
                log.info("Fire created successfully.");
                this.advanceStep();
            } else {
                log.warn("Failed to create fire.");
            }
        } else {
            log.warn("Tinderbox or Logs not found in inventory.");
        }

    }

    private void cookShrimp() {
        TileObject fire = TileObjects.getNearest(new String[]{"Fire"});
        if (fire != null && Inventory.contains(new String[]{"Raw shrimps"})) {
            Inventory.getFirst(new String[]{"Raw shrimps"}).useOn(fire);
            if (Time.sleepUntil(() -> {
                return Inventory.contains(new String[]{"Shrimps"}) || Inventory.contains(new String[]{"Burnt shrimps"});
            }, 5000)) {
                if (Inventory.contains(new String[]{"Shrimps"})) {
                    log.info("Shrimps cooked successfully. Task complete.");
                    this.advanceStep();
                } else {
                    log.warn("Failed to cook shrimps.");
                }
            } else {
                log.warn("Failed to cook shrimps.");
            }
        } else {
            log.warn("Fire or Raw shrimps not found.");
        }

    }

    public static enum SurvivalExpertStep {
        TALK_TO_EXPERT,
        CATCH_SHRIMP,
        TALK_TO_EXPERT_AGAIN,
        CHOP_TREE,
        USE_TINDERBOX_ON_LOGS,
        COOK_SHRIMP;

        private SurvivalExpertStep() {
        }
    }
}
