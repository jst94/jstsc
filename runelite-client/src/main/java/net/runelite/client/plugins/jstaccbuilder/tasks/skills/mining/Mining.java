package net.runelite.client.plugins.jstaccbuilder.tasks.skills.mining;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
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
import net.unethicalite.api.movement.pathfinder.model.BankLocation;

@Slf4j
public class Mining implements Task, SkillingTask {
    private static final int MAX_LEVEL = 20;
    private final SkillUtils skillUtils;
    private final TradeUnlockerConfig config;
    private final MiningData miningData = new MiningData();
    private WorldPoint miningPosition;
    private WorldPoint bankWalkPosition;
    private int taskLevel = 20;
    private int amountBefore;
    private long lastAnimation;
    private boolean impossibleToComplete;
    private boolean setVariables;

    public Mining(SkillUtils skillUtils, TradeUnlockerConfig config) {
        RuneLite.getInjector().getInstance(EventBus.class).register(this);
        this.skillUtils = skillUtils;
        this.config = config;
        resetEndLevel();

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
     * Checks if the mining task is complete.
     *
     * @return True if the task is complete, otherwise false.
     */
    public boolean isComplete() {
        int currentLevel = Skills.getLevel(Skill.MINING);
        return currentLevel >= taskLevel || currentLevel >= MAX_LEVEL || LocalPlayer.getTotalLevel() >= config.getTotalLevel() || impossibleToComplete;
    }

    /**
     * Main execution method for the mining task.
     */
    public void run() {

        if (!setVariables) {
            miningPosition = miningData.getRandomMiningPosition();
            BankLocation bankArea = skillUtils.getNearestBank(miningPosition);
            bankWalkPosition = bankArea.getArea().getCenter();
            setVariables = true;
            return;
        }

        if (!Equipment.contains(ItemID.BRONZE_PICKAXE)) {
            getPickaxe();
            return;
        }

        if (!Players.getLocal().getWorldLocation().equals(miningPosition)) {
            log.debug("Walking to mining location!");
            skillUtils.walkTo(miningPosition);
            return;
        }

        if (Inventory.isFull()) {
            dropMiningLoot();
            return;
        }

        mineRocks();
    }

    /**
     * Attempts to acquire a Bronze Pickaxe for the player.
     * This method checks if the pickaxe is in the player's inventory, or in the bank,
     * and attempts to wield or withdraw it as necessary.
     */
    private void getPickaxe() {
        if (Inventory.contains(ItemID.BRONZE_PICKAXE)) {
            log.debug("Wield Bronze Pickaxe");
            Inventory.getFirst(ItemID.BRONZE_PICKAXE).interact("Wield");
            Time.sleepUntil(() -> Equipment.contains(ItemID.BRONZE_PICKAXE), 5000);
            return;
        }

        if (Bank.isOpen()) {
            if (Bank.contains(ItemID.BRONZE_PICKAXE)) {
                log.debug("Withdrawing Bronze Pickaxe");
                Bank.withdraw(ItemID.BRONZE_PICKAXE, 1, Bank.WithdrawMode.ITEM);
                Time.sleepUntil(() -> Inventory.contains(ItemID.BRONZE_PICKAXE), 5000);
                return;
            }
            impossibleToComplete = true; // there are spawns i could go get an axe from actually.
            return;
        }

        if (BankLocation.getNearest().getArea().contains(Players.getLocal())) {
            log.debug("Opening bank");
            skillUtils.openBank();
            return;
        }

        log.debug("Walking to bank");
        skillUtils.walkTo(bankWalkPosition);
    }

    /**
     * Manages the logic for mining rocks at the designated mining position.
     * This method finds the nearest rock and interacts with it to mine. It also handles
     * any conditions for stopping or continuing the mining action.
     */
    private void mineRocks() {
        TileObject rock = TileObjects.getNearest(i -> i.getName().contains("rocks") && i.hasAction("Mine") && i.distanceTo(miningPosition) <= 1);
        if (rock != null) {
            if (Players.getLocal().isAnimating()) {
                log.debug("Mining rock...");
                Time.sleepUntil(() -> Inventory.getCount(i -> i.getName().toLowerCase().contains("ore")) > amountBefore || noLongerAnimating(), 60_000);
                return;
            }
            log.debug("Interacting with rock");
            amountBefore = Inventory.getCount(i -> i.getName().toLowerCase().contains("ore"));
            rock.interact("Mine");
            Time.sleepUntil(() -> Players.getLocal().isAnimating(), 5000);
        }
    }

    private void dropMiningLoot() {
        log.debug("Dropping loot");
        Inventory.getAll(miningData.getDroppables()).forEach(item -> item.interact("Drop"));
        Time.sleepUntil(() -> !Inventory.contains(miningData.getDroppables()), 5000);
    }

    private boolean noLongerAnimating() {
        if (Players.getLocal().isAnimating()) {
            lastAnimation = System.currentTimeMillis();
        }
        return System.currentTimeMillis() - lastAnimation > 5000;
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
        return Skills.getLevel(Skill.MINING);
    }

    @Override
    public int getTaskLevel() {
        return taskLevel;
    }

    @Override
    public Skill getSkill() {
        return Skill.MINING;
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
