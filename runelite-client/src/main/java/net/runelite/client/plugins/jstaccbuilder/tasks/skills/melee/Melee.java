package net.runelite.client.plugins.jstaccbuilder.tasks.skills.melee;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
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
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;

@Slf4j
public class Melee implements Task, SkillingTask {

    private static final WorldPoint BRONZE_SCIM = new WorldPoint(3362, 3154, 0);
    private final TradeUnlockerConfig config;
    private final SkillUtils skillUtils;
    private PolyArea chickenArea;
    private WorldPoint chickenAreaPosition;
    private int taskLevel = 20, lastExp;
    private long lastCheck;
    private NPC mob;
    private boolean setVariables;


    public Melee(SkillUtils skillUtils, TradeUnlockerConfig config) {
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
     * Checks whether this task is complete
     *
     * @return true if task is complete.
     */
    public boolean isComplete() {
        int currentLevel = Skills.getLevel(Skill.DEFENCE);
        boolean check1 = currentLevel >= taskLevel;
        boolean check2 = currentLevel >= 20;
        boolean check3 = LocalPlayer.getTotalLevel() >= config.getTotalLevel();
        log.debug("MELEE CHECKS: " + check1 + " | " + check2 + " | " + check3);
        return check1 || check2 || check3;
    }

    /**
     * Main logic for the task
     */
    public void run() {

        if (!setVariables) {
            MeleeData meleeData = new MeleeData();
            chickenArea = new PolyArea(meleeData.getRandomChickenArea());
            chickenAreaPosition = chickenArea.getRandomWorldPoint();
            lastExp = Skills.getExperience(Skill.DEFENCE);
            setVariables = true;
            return;
        }
        if (!Equipment.contains(ItemID.BRONZE_SCIMITAR)) {
            getSword();
            return;
        }

        if (Combat.getAttackStyle() != Combat.AttackStyle.THIRD) {
            log.debug("Setting attack style to shared :)");
            Combat.setAttackStyle(Combat.AttackStyle.THIRD);
            return;
        }

        if (!chickenArea.contains(Players.getLocal())) {
            log.debug("Walking to chicken coop CHICKEN FUCKING COOP");
            skillUtils.walkTo(chickenAreaPosition);
            return;
        }

        if (isIdle()) {
            killChickens();
        }
    }

    // Kills chicken NPCs in the designated chicken area. Looks for a suitable chicken NPC, then interacts to attack it.
    private void killChickens() {
        // find a suitable chicken npc to attack
        mob = Combat.getAttackableNPC(n -> n.getName() != null && n.getName().equals("Chicken") && !n.isDead() && chickenArea.contains(n) && Reachable.isInteractable(n));
        if (mob == null) {
            log.debug("Waiting for chickens---");
            return;
        }

        if (!Reachable.isInteractable(mob)) {
            log.debug("Walking to chicken - not reachable");
            skillUtils.walkTo(mob.getWorldLocation());
            return;
        }

        lastCheck = System.currentTimeMillis();
        lastExp = Skills.getExperience(Skill.HITPOINTS);
        log.debug("Attack da chickkken");
        mob.interact("Attack");
        Time.sleepUntil(() -> Players.getLocal().isAnimating(), 5000);
    }

    //Checks if the player character is idle, indicating readiness to attack a chicken.
    private boolean isIdle() {
        return (Players.getLocal().getInteracting() == null && !Players.getLocal().isAnimating()) ||
                (mobNotInteracting() && !gainedExp());
    }

    //helper method to check if the mob is not interacting
    private boolean mobNotInteracting() {
        return mob == null || mob.isDead() || mob.getInteracting() == null || mob.getInteracting() != Players.getLocal();
    }

    //checks if the player has gained xp in the last 30 seconds - additional in combat check.
    private boolean gainedExp() {
        if (Skills.getExperience(Skill.HITPOINTS) > lastExp) {
            lastExp = Skills.getExperience(Skill.HITPOINTS);
            lastCheck = System.currentTimeMillis();
            return true;
        }
        return System.currentTimeMillis() - lastCheck < 30_000;
    }

    //function to handle obtaining a bronze sword
    private void getSword() {
        if (Inventory.contains(ItemID.BRONZE_SCIMITAR)) {
            log.debug("Wield bronze sword");
            Inventory.getFirst(ItemID.BRONZE_SCIMITAR).interact("Wield");
            Time.sleepUntil(() -> Equipment.contains(ItemID.BRONZE_SCIMITAR), 5000);
            return;
        }

        log.debug("Looting Bronze Scimitar spawn");
        skillUtils.getTileItem(BRONZE_SCIM, ItemID.BRONZE_SCIMITAR);
    }


    //Resets the ending level for the task
    @Override
    public void resetEndLevel() {
        int currentLevel = getCurrentSkillLevel();
        int startLevel = Math.max(currentLevel + 1, 11);
        if (startLevel >= 20) {
            taskLevel = 20;
        } else {
            taskLevel = Rand.nextInt(startLevel, 20);
        }
    }

    @Override
    public int getCurrentSkillLevel() {
        return Skills.getLevel(Skill.DEFENCE);
    }

    @Override
    public int getTaskLevel() {
        return taskLevel;
    }

    @Override
    public Skill getSkill() {
        return Skill.DEFENCE;
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
