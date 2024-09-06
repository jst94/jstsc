package net.runelite.client.plugins.jstaccbuilder.tasks.skills;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
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
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Inventory;

@Slf4j
public class Fishing implements Task, SkillingTask {

    private static final WorldPoint NET_LOCATION = new WorldPoint(3243, 3159, 0);

    private static final WorldArea LUMMY_AREA = new WorldArea(3238, 3145, 11, 13, 0);
    private static final WorldPoint LUMMY_WALK = new WorldPoint(3241, 3153, 0);

    private static final WorldArea AL_KARID_AREA = new WorldArea(3264, 3137, 15, 16, 0);
    private static final WorldPoint AL_KHARID_WALK = new WorldPoint(3272, 3146, 0);

    private static final WorldArea DRAYNOR_AREA = new WorldArea(3079, 3217, 14, 21, 0);
    private static final WorldPoint DRAYNOR_WALK = new WorldPoint(3087, 3229, 0);

    private static final WorldArea PORT_SARIM_AREA = new WorldArea(2979, 3150, 24, 33, 0);
    private static final WorldPoint PORT_SARIM_WALK = new WorldPoint(2993, 3166, 0);

    private static final WorldArea[] AREAS = {
            LUMMY_AREA,
            AL_KARID_AREA,
            PORT_SARIM_AREA,
            DRAYNOR_AREA
    };

    private static final WorldPoint[] WALKS = {
            LUMMY_WALK,
            AL_KHARID_WALK,
            PORT_SARIM_WALK,
            DRAYNOR_WALK
    };
    private static final int MAX_LEVEL = 20;
    private final TradeUnlockerConfig config;
    private final SkillUtils skillUtils;
    private WorldArea fishingArea;
    private WorldPoint fishingAreaWalk;
    private boolean setVariables;
    private long lastAnimation;
    private int taskLevel = 20;

    public Fishing(SkillUtils skillUtils, TradeUnlockerConfig config) {
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
     * Checks if the fishing task is complete.
     *
     * @return True if the fishing task is complete, false otherwise.
     */
    public boolean isComplete() {
        int currentLevel = Skills.getLevel(Skill.FISHING);
        return currentLevel >= taskLevel || currentLevel >= MAX_LEVEL || LocalPlayer.getTotalLevel() >= config.getTotalLevel();
    }

    /**
     * The main execution method for the Fishing task.
     */
    public void run() {
        if (!setVariables) {
            // Generate a random fishing area and accompanying walk position :
            // only allow draynor areas if cb lvl >= 14
            int cbLvl = Players.getLocal().getCombatLevel();

            int maxIndex = cbLvl >= 14 ? AREAS.length : AREAS.length - 1; // Draynor will always be last in array so avoid if cb lvl too low
            int chosenIndex = Rand.nextInt(0, maxIndex - 1);

            fishingArea = AREAS[chosenIndex];
            fishingAreaWalk = WALKS[chosenIndex];
            setVariables = true;
            return;
        }

        if (!Inventory.contains(ItemID.SMALL_FISHING_NET)) {
            getSmallFishingNet();
            return;
        }

        if (!fishingArea.contains(Players.getLocal())) {
            walkToFishingArea();
            return;
        }

        if (Inventory.isFull()) {
            dropAllFish();
            return;
        }

        startFishing();
    }

    private void startFishing() {
        NPC spot = NPCs.getNearest(n -> n.getName().equals("Fishing spot") && n.hasAction("Net", "Small Net") && fishingArea.contains(n));
        if (spot != null) {
            log.debug("Interacting with fish spot");
            lastAnimation = System.currentTimeMillis();
            int randSleep = Rand.nextInt(3500, 5500);
            spot.interact("Net", "Small Net");
            Time.sleepUntil(() -> doneFishing(randSleep), 180_000);
        }
    }

    private void dropAllFish() {
        log.debug("Inv full - dropping");
        Inventory.getAll(ItemID.RAW_SHRIMPS, ItemID.RAW_ANCHOVIES).forEach(item -> item.drop());
        Time.sleepUntil(() -> !Inventory.contains(ItemID.RAW_SHRIMPS, ItemID.RAW_ANCHOVIES), 5_000);
    }

    @Override
    public Skill getSkill() {
        return net.runelite.api.Skill.FISHING;
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

    private void walkToFishingArea() {
        log.debug("Walking to fish location");
        skillUtils.walkTo(fishingAreaWalk);
    }

    private void getSmallFishingNet() {
        log.debug("Getting small fishing net");
        skillUtils.getItemFromObject(o -> o.getId() == 674, "Take", NET_LOCATION, ItemID.SMALL_FISHING_NET);
    }

    private boolean doneFishing(int randSleep) {
        if (Inventory.isFull()) {
            return true;
        }
        if (Players.getLocal().isAnimating()) {
            lastAnimation = System.currentTimeMillis();
        }
        return System.currentTimeMillis() - lastAnimation > randSleep;
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
        return Skills.getLevel(Skill.FISHING);
    }

    @Override
    public int getTaskLevel() {
        return taskLevel;
    }
}


