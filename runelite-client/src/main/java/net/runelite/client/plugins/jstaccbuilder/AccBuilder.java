package net.runelite.client.plugins.jstaccbuilder;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.jstaccbuilder.tasks.SkillingTask;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.runelite.client.plugins.jstaccbuilder.tasks.quests.*;
import net.runelite.client.plugins.jstaccbuilder.tasks.skills.Firemaking;
import net.runelite.client.plugins.jstaccbuilder.tasks.skills.Fishing;
import net.runelite.client.plugins.jstaccbuilder.tasks.skills.SkillUtils;
import net.runelite.client.plugins.jstaccbuilder.tasks.skills.melee.Melee;
import net.runelite.client.plugins.jstaccbuilder.tasks.skills.mining.Mining;
import net.runelite.client.plugins.jstaccbuilder.tasks.skills.woodcutting.Woodcutting;
import net.runelite.client.plugins.jstaccbuilder.tasks.utils.SettingsCheck;
import net.unethicalite.api.account.LocalPlayer;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main class for the Trade Unlocker plugin.
 * Automates tasks to achieve 10 quest points and a total level of 100 for an account.
 */
@Extension
@PluginDescriptor(
        name = "Jst Acc Builder",
        description = "Gets 10qp and 100 total",
        enabledByDefault = false
)
@Slf4j
@Singleton
public class AccBuilder extends Script {
    // Instance to manage and optimize Runescape account settings.
    private final SettingsCheck settingsFix = new SettingsCheck();

    // List to store the tasks for the account.
    private final List<Task> tasks = new ArrayList<>();
    private final AccLogger accountLogger = new AccLogger(getName());
    // The closest bank location to the player.
    private WorldPoint closestBank = null;
    // Threshold energy level to start running.
    private int nextEnergy;
    private boolean outOfTasks;
    @Inject
    private Client client;
    @Inject
    private TradeUnlockerConfig config;
    private long startTime;
    private long afkStartTime;
    private long afkEndTime;
    /**
     * -- GETTER --
     * Gets the current task being executed.
     */
    @Getter
    private Task currentTask;
    private SkillUtils skillUtils;
    @Inject
    private ConfigManager configManager;

    public AccBuilder() {
    }

    @Provides
    public TradeUnlockerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(TradeUnlockerConfig.class);
    }

    @Override
    protected int loop() {
        if (!Game.isLoggedIn() || Game.isOnLoginScreen() || Players.getLocal().getName() == null) {
            return 1000;
        }

        if (Skills.getLevel(Skill.ATTACK) == 0) {
            return 3000;
        }

        if (!settingsFix.isSettingsFixed()) {
            return settingsFix.configureSettings();
        }

        if (startTime - System.currentTimeMillis() > 90_000_000) {
            System.exit(8);
        }

        if (!Dialog.isOpen() && Movement.getRunEnergy() > nextEnergy && !Movement.isRunEnabled()) {
            log.debug("Toggle run");
            Movement.toggleRun();
            nextEnergy = Rand.nextInt(30, 60);
        }

        if (outOfTasks || LocalPlayer.getQuestPoints() >= config.getQuestPoints() && LocalPlayer.getTotalLevel() >= config.getTotalLevel()) {
            walkToBankAndStop();
            return -1;
        }

        if (Bank.isOpen()) {
            Widget tut = Widgets.get(664, 29, 0);
            Widget jagAcc = Widgets.get(289, 7);
            if (tut != null && tut.isVisible()) {
                log.debug("Closing tut");
                tut.interact(0);
                return -1;
            }
            if (jagAcc != null && jagAcc.isVisible()) {
                log.debug("Closing jag acc");
                jagAcc.interact(0);
                return -1;
            }
        }

        if (currentTask != null && !currentTask.isComplete()) {
            currentTask.run();
            return -1;
        }

        if (!Inventory.isEmpty() || !Equipment.getAll().isEmpty()) {
            getReadyForTask();
            return -1;
        }

        selectNewTask();
        return 1000;
    }


    /**
     * If out of tasks to do will walk to bank and stop the plugin :)
     */
    private void walkToBankAndStop() {
        log.debug("Completed tasks - walking to bank and waiting");
        if (BankLocation.getNearest().getArea().contains(Players.getLocal())) {
            if (config.shouldRunFullDay()) {
                hideWidgets(true);
                // if (startTime - System.currentTimeMillis() > 86_700_000) {
                finish();
                //}
                return;
            }
            log.debug("In Bank. Stopping plugin");
            System.exit(0);
            return;
        }
        if (closestBank == null) {
            closestBank = skillUtils.getNearestBank(Players.getLocal().getWorldLocation()).getArea().getCenter();
            Time.sleepUntil(() -> closestBank != null, 30_000);
            return;
        }
        skillUtils.walkTo(closestBank);
    }

    private void hideWidgets(boolean hide) {
        Widget map = Widgets.get(WidgetInfo.FIXED_VIEWPORT_MINIMAP);
        Widget chat = Widgets.get(WidgetInfo.CHATBOX_PARENT);

        if (map == null || chat == null) {
            log.debug("Widgets null");
            return;
        }

        if ((hide && !map.isHidden()) || (!hide && map.isHidden())) {
            log.debug("Set widget enabled status to: " + hide);
            map.setHidden(hide);
        }

        if ((hide && !chat.isHidden()) || (!hide && chat.isHidden())) {
            chat.setHidden(hide);
        }
    }


    /**
     * Sets a new random uncompleted task.
     * Can be a questing task if < 10qp and quest not done.
     * Can be a skilling task if < 100 total and skill not reached lvl cap
     */
    private void selectNewTask() {
        closestBank = null;

        if (Skills.getLevel(Skill.ATTACK) >= 10) {
            removeLastTaskIfMelee();
        }

        List<Task> incompleteTasks = tasks.stream().filter(t -> !t.isComplete()).collect(Collectors.toList());
        if (incompleteTasks.isEmpty()) {
            System.out.println("Out of eligible tasks. Running to bank");
            outOfTasks = true;
            return;
        }

        if (currentTask instanceof SkillingTask) {
            ((SkillingTask) currentTask).resetEndLevel();
        }

        if (incompleteTasks.size() == 1) {
            currentTask = incompleteTasks.get(0);
        } else {
            currentTask = incompleteTasks.get(Rand.nextInt(0, incompleteTasks.size() - 1));
        }
        if (currentTask instanceof SkillingTask) {
            ((SkillingTask) currentTask).resetEndLevel();
        }
    }

    private void finish() {
        if (accountLogger.writeToFile("tradeunlocks")) {
            System.exit(5);
        }
    }

    private void getReadyForTask() {
        if (Bank.isOpen()) {
            log.debug("Depositing inventory");
            Bank.depositInventory();
            log.debug("Depositing equipment");
            Bank.depositEquipment();
            return;
        }
        if (BankLocation.getNearest().getArea().contains(Players.getLocal())) {
            if (System.currentTimeMillis() > afkEndTime) {
                log.debug("Open bank");
                skillUtils.openBank();
            }
            return;
        }

        if (closestBank == null) {
            afkStartTime = System.currentTimeMillis();
            long afkTime = Rand.nextInt(1_200_000, 2_400_000);
            afkEndTime = afkStartTime + afkTime;
            closestBank = skillUtils.getNearestBank(Players.getLocal().getWorldLocation()).getArea().getCenter();
            Time.sleepUntil(() -> closestBank != null, 60_000);
        }


        log.debug("Walk to bank");
        skillUtils.walkTo(closestBank);
    }


    private void removeLastTaskIfMelee() {
        if (!tasks.isEmpty() && tasks.get(tasks.size() - 1) instanceof Melee) {
            tasks.remove(tasks.size() - 1);
        }
    }

    private <T extends Task> void addTask(List<Task> taskList, T task) {
        taskList.add(task);
    }

    /**
     * Initializes tasks and other necessary components when the script starts.
     */
    @Override
    public void onStart(String... args) {
        configManager.setConfiguration("unethicalite", "fpsLimit", 50);
        configManager.setConfiguration("unethicalite", "useTeleports", false);

        // set a random end point
        int endTotalLevel = Rand.nextInt(100, 193);
        configManager.setConfiguration("tradeunlocker", "totalLevel", endTotalLevel);

        //quests
        QuestUtils questUtils = new QuestUtils();
        addTask(tasks, new CooksAssistant(questUtils, config));
        addTask(tasks, new RomeoAndJuliet(questUtils, config));
        addTask(tasks, new RuneMysteries(questUtils, config));
        addTask(tasks, new SheepShearer(questUtils, config));
        addTask(tasks, new TheRestlessGhost(questUtils, config));
        addTask(tasks, new WitchesPotion(questUtils, config));

        //skills
        this.skillUtils = new SkillUtils(client);
        addTask(tasks, new Melee(skillUtils, config));
        addTask(tasks, new Firemaking(skillUtils, config));
        addTask(tasks, new Fishing(skillUtils, config));
        addTask(tasks, new Woodcutting(skillUtils, config));
        addTask(tasks, new Mining(skillUtils, config));
        addTask(tasks, new Melee(skillUtils, config));

        // Set the current task to melee to get easy levels and be safe elsewhere.
        // currentTask = tasks.get(10);
        nextEnergy = Rand.nextInt(30, 60);
        startTime = System.currentTimeMillis();

        client.setLowCpu(true);
        client.changeMemoryMode(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        hideWidgets(false);
    }
}