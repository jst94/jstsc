package net.runelite.client.plugins.jstaccbuilder.tasks.skills;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.jstaccbuilder.TradeUnlockerConfig;
import net.runelite.client.plugins.jstaccbuilder.tasks.SkillingTask;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.unethicalite.api.account.LocalPlayer;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;

@Slf4j
public class Firemaking implements Task, SkillingTask {

    private static final WorldArea FIREMAKE_AREA = new WorldArea(3204, 3223, 7, 5, 2);
    private static final WorldPoint FIREMAKE_WALK = new WorldPoint(3207, 3224, 2);

    private final SkillUtils skillUtils;
    private final TradeUnlockerConfig config;
    private boolean impossibleToComplete;
    private int taskLevel = 20;

    public Firemaking(SkillUtils skillUtils, TradeUnlockerConfig config) {
        RuneLite.getInjector().getInstance(EventBus.class).register(this);
        this.skillUtils = skillUtils;
        this.config = config;
    }

    /**
     * Checks if the player is outside the designated firemaking area.
     *
     * @return True if the player is outside, otherwise false.
     */
    private static boolean isOutsideFiremakingArea() {
        return !FIREMAKE_AREA.contains(Players.getLocal());
    }

    /**
     * Determines if the player has a tinderbox in their inventory.
     *
     * @return True if the player has a tinderbox, otherwise false.
     */
    private static boolean hasTinderbox() {
        return Inventory.contains(ItemID.TINDERBOX);
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
     * Checks if the Firemaking task is complete.
     *
     * @return True if the player's Firemaking level is at or above the target level,
     * or if the task is marked as impossible to complete, or if the player's total level
     * is at or above 100. Otherwise, it returns false.
     */
    public boolean isComplete() {
        return Skills.getLevel(Skill.FIREMAKING) >= taskLevel || impossibleToComplete || LocalPlayer.getTotalLevel() >= config.getTotalLevel();
    }

    /**
     * The main logic of the Firemaking task. This method determines what action the script
     * should take based on the current state of the game and player.
     */
    public void run() {
        if (!hasTinderbox()) {
            getTinderbox();
            return;
        }
        if (isOutsideFiremakingArea()) {
            skillUtils.walkTo(FIREMAKE_WALK);
            return;
        }

        lightLogsOrHopWorlds();
    }

    /**
     * Attempts to light logs. If no suitable logs are found, the world is hopped.
     */
    private void lightLogsOrHopWorlds() {
        TileItem logs = TileItems.getNearest(i -> i.getId() == ItemID.LOGS && FIREMAKE_AREA.contains(i) && TileObjects.getNearest(o -> o.getName().equals("Fire") && o.getWorldLocation().equals(i.getWorldLocation())) == null);
        if (logs == null) {
            log.debug("Hopping worlds");
            Worlds.hopTo(Worlds.getRandom(w -> w.isNormal() && !w.isMembers() && w.getPlayerCount() < 750));
            return;
        }

        int xpBefore = Skills.getExperience(Skill.FIREMAKING);
        log.debug("Lighting logs");
        logs.interact("Light");
        Time.sleepUntil(() -> TileItems.getNearest(o -> o.getId() == ItemID.LOGS && o.getWorldLocation().equals(logs.getWorldLocation())) == null || Skills.getExperience(Skill.FIREMAKING) > xpBefore, 30_000);
    }

    /**
     * Retrieves a tinderbox from the bank or marks the task as impossible to complete if not found.
     */
    private void getTinderbox() {
        if (Bank.isOpen()) {
            if (Bank.contains(ItemID.TINDERBOX)) {
                log.debug("Withdrawing tinderbox");
                Bank.withdraw(ItemID.TINDERBOX, 1, Bank.WithdrawMode.ITEM);
                return;
            }
            // No tinderbox found sadge
            impossibleToComplete = true;
            return;
        }
        if (BankLocation.LUMBRIDGE_BANK.getArea().contains(Players.getLocal())) {
            log.debug("Open bank");
            openBank();
            return;
        }
        log.debug("Walking to lumbridge bank");
        skillUtils.walkTo(BankLocation.LUMBRIDGE_BANK.getArea().getCenter());
    }


    /**
     * Attempts to open the bank by interacting with a bank object or NPC.
     */
    private void openBank() {
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


    @Override
    public void resetEndLevel() {
        int currentLevel = getCurrentSkillLevel();
        int startLevel = Math.max(currentLevel + 5, 10);
        if (startLevel >= 20) {
            taskLevel = 20;
        } else {
            taskLevel = Rand.nextInt(startLevel, 20);
        }
    }

    @Override
    public int getCurrentSkillLevel() {
        return Skills.getLevel(Skill.FIREMAKING);
    }

    @Override
    public int getTaskLevel() {
        return taskLevel;
    }

    @Override
    public Skill getSkill() {
        return Skill.FIREMAKING;
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
