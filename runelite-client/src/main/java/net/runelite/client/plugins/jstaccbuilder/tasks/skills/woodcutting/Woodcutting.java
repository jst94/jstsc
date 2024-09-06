package net.runelite.client.plugins.jstaccbuilder.tasks.skills.woodcutting;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.jstaccbuilder.TradeUnlockerConfig;
import net.runelite.client.plugins.jstaccbuilder.tasks.SkillingTask;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.runelite.client.plugins.jstaccbuilder.tasks.skills.SkillUtils;
import net.unethicalite.api.account.LocalPlayer;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class Woodcutting implements Task, SkillingTask {

    // Maximum level for the woodcutting task
    private static final int MAX_LEVEL = 20;
    private final SkillUtils skillUtils;
    private final TradeUnlockerConfig config;
    // Data related to woodcutting
    private final WoodcuttingData woodcuttingData = new WoodcuttingData();
    // A list of tree names that give normal logs
    private final Set<String> validTreeNames = new HashSet<>();
    // Area for woodcutting
    private final WorldArea woodcutArea = woodcuttingData.getRandomWoodcuttingArea();
    // Position within woodcutWalkArea to walk to.
    private WorldPoint woodcutWalkPosition;
    // Position within bankArea to walk to.
    private WorldPoint bankWalkPosition;
    // Level for the current woodcutting task
    private int taskLevel = 20;
    private boolean setVariables;

    public Woodcutting(SkillUtils skillUtils, TradeUnlockerConfig config) {
        RuneLite.getInjector().getInstance(EventBus.class).register(this);
        this.skillUtils = skillUtils;
        this.config = config;

        resetEndLevel();
        validTreeNames.add("Tree");
        validTreeNames.add("Evergreen tree");
        validTreeNames.add("Jungle tree");
        validTreeNames.add("Dying tree");
        validTreeNames.add("Dead tree");
        setVariables = false;
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
     * Checks if the woodcutting task is complete.
     *
     * @return True if the task is complete, otherwise false.
     */
    public boolean isComplete() {
        int currentLevel = Skills.getLevel(Skill.WOODCUTTING);
        return currentLevel >= taskLevel || currentLevel >= MAX_LEVEL || LocalPlayer.getTotalLevel() >= config.getTotalLevel();
    }

    /**
     * Executes the woodcutting actions based on the current game state.
     */
    public void run() {

        if (!setVariables) {
            // Smaller area within woodcutArea that will be used to walk to
            WorldArea woodcutWalkArea = woodcuttingData.getWalkArea(woodcutArea);
            // Bank location that is closest to generated woodcutArea
            BankLocation bankArea = skillUtils.getNearestBank(woodcutWalkArea.getCenter());
            bankWalkPosition = bankArea.getArea().getCenter();
            woodcutWalkPosition = woodcutWalkArea.getRandom();
            setVariables = true;
            return;
        }

        if (Skills.getLevel(Skill.ATTACK) < 10) {
            if (WoodcuttingData.Lumbridge.AREA.equals(woodcutArea)) {
                setVariables = false;
                return;
            }
        }

        if (!Equipment.contains(ItemID.BRONZE_AXE)) {
            getAxe();
            return;
        }

        if (Inventory.isFull()) {
            log.debug("Droppping dem logs");
            List<Item> invItems = Inventory.getAll(ItemID.LOGS);
            invItems.forEach(Item::drop);
            Time.sleepUntil(() -> !Inventory.contains(ItemID.LOGS), 3_000);
            return;
        }

        if (woodcutArea.contains(Players.getLocal())) {

            TileObject tree = TileObjects.getNearest(o -> woodcutArea.contains(o) && validTreeNames.contains(o.getName()) && o.hasAction("Chop down") && noPlayersChopping(o) && Reachable.isInteractable(o));
            if (tree != null) {
                log.debug("Chopping tree");
                int amountBefore = Inventory.contains(ItemID.LOGS) ? Inventory.getCount(ItemID.LOGS) : 0;
                tree.interact("Chop down");
                Time.sleepUntil(() -> Inventory.contains(ItemID.LOGS) && Inventory.getCount(ItemID.LOGS) > amountBefore ||
                        TileObjects.getNearest(o -> o.getName().equals("Tree stump") && o.getWorldLocation().equals(tree.getWorldLocation())) != null, 30_000);
                return;
            }
            log.debug("Looking for new trees");
            skillUtils.walkTo(woodcutWalkPosition);
            return;
        }
        log.debug("Walking to woodcutting area");
        skillUtils.walkTo(woodcutWalkPosition);
    }


    /**
     * Checks if no players are chopping the specified tree.
     *
     * @param o The tree to check.
     * @return True if no players are chopping the tree, otherwise false.
     */
    private boolean noPlayersChopping(TileObject o) {
        List<Player> players = Players.getAll(p -> o.getWorldLocation().createWorldArea(1, 1).contains(p) && !p.getName().equals(Players.getLocal().getName()));
        for (Player player : players) {
            if (player != null) {
                if (player.getAnimation() == 879) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ensures the player has an axe, and if not, fetches one.
     */
    private void getAxe() {
        if (Inventory.contains(ItemID.BRONZE_AXE)) {
            log.debug("Wield bronze axe");
            Inventory.getFirst(ItemID.BRONZE_AXE).interact("Wield");
            Time.sleepUntil(() -> Equipment.contains(ItemID.BRONZE_AXE), 5000);
            return;
        }

        if (Bank.isOpen()) {
            if (Bank.contains(ItemID.BRONZE_AXE)) {
                log.debug("Withdraw bronze axe");
                Bank.withdraw(ItemID.BRONZE_AXE, 1, Bank.WithdrawMode.ITEM);
                Time.sleepUntil(() -> Inventory.contains(ItemID.BRONZE_AXE), 5000);
                return;
            }
            return;
        }

        if (BankLocation.getNearest().getArea().contains(Players.getLocal())) {
            log.debug("Opening bank");
            skillUtils.openBank();
            return;
        }

        log.debug("Walk to bank");
        skillUtils.walkTo(bankWalkPosition);
    }


    @Override
    public void resetEndLevel() {
        int currentLevel = getCurrentSkillLevel();
        int startLevel = Math.max(currentLevel + 1, 10);
        if (startLevel >= 20) {
            taskLevel = 20;
        } else {
            taskLevel = Rand.nextInt(startLevel, 20);
        }
    }

    @Override
    public int getCurrentSkillLevel() {
        return Skills.getLevel(Skill.WOODCUTTING);
    }

    @Override
    public int getTaskLevel() {
        return taskLevel;
    }

    @Override
    public Skill getSkill() {
        return Skill.WOODCUTTING;
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
}
