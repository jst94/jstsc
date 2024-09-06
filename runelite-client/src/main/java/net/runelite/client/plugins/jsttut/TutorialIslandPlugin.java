package net.runelite.client.plugins.jsttut;

import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.runelite.client.plugins.jsttut.tasks.*;
import net.unethicalite.api.plugins.TaskPlugin;
import net.runelite.client.plugins.jsttut.tasks.WizardGuideTask;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Extension
@PluginDescriptor(
        name = "JstTut",
        description = "Handles Tutorial Island tasks",
        enabledByDefault = false
)
public class TutorialIslandPlugin extends TaskPlugin
{

    private static final Logger log = LoggerFactory.getLogger(TutorialIslandPlugin.class);
    private static final MainState DEFAULT_STATE = MainState.CREATE_CHARACTER_DESIGN;

    @Inject
    private net.runelite.api.Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private TutConfig config;

    @Getter
    private MainState mainState;
    private Task[] tasks;

    public TutorialIslandPlugin()
    {
        this.mainState = DEFAULT_STATE;
    }

    @Provides
    TutConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(TutConfig.class);
    }

    @Override
    public void startUp()
    {
        if (isClientReady())
        {
            initializeTasks();
            mainState = determineCurrentState();

            log.info("Set state to: {}", mainState);
            handleTasksBasedOnState();
        }
        else
        {
            log.warn("Client is not initialized or player is not logged in. Aborting startUp.");
        }
    }

    private boolean isClientReady()
    {
        return this.client != null && this.client.getGameState() == GameState.LOGGED_IN && this.client.getLocalPlayer() != null;
    }

    private void initializeTasks()
    {
        this.tasks = createTasksArray();
    }

    
    private Task[] createTasksArray() {
        return new Task[]{
                new DisplayNamePickerTask(this),
                new CharacterCreationTask(this),
                (Task) new GuilinorGuideTask(),
                (Task) new SurvivalExpertTask(),
                (Task) new CookingGuideTask(),
                (Task) new QuestGuideTask(),
                (Task) new MiningGuideTask(),
                (Task) new CombatGuideTask(),
                (Task) new BankGuideTask(),
                (Task) new ChurchGuideTask(),
                (Task) new WizardGuideTask()
        };
    }

    private void handleTasksBasedOnState()
    {
        handleDialogue();

        switch (mainState)
        {
            case PICK_NAME:
                handleNamePicker();
                break;
            case CREATE_CHARACTER_DESIGN:
                handleCharacterDesign();
                break;
            case GIELINOR_GUIDE:
                handleGielinorGuideInteraction();
                break;
            case OPEN_OPTIONS_TAB:
                handleOpenOptionsTab();
                break;
            case OPEN_DOOR_TO_SURVIVAL_EXPERT:
                interactWithDoor();
                break;
            case SURVIVAL_EXPERT:
                handleSurvivalExpert();
                break;
            case FISH_SHRIMPS:
                handleFishShrimps();
                break;
            case OPEN_SKILLS_TAB:
                handleOpenSkillsTab();
                break;
            case SURVIVAL_EXPERT_AGAIN:
                handleSurvivalExpertAgain();
                break;
            case CHOP_TREE:
                handleTreeInteraction();
                break;
            case MAKE_FIRE:
                handleMakeFire();
                break;
            case COOK_SHRIMPS:
                handleCookShrimps();
                break;
            case COOKING_GUIDE_SECTION:
                handleCookingGuideSection();
                break;
            default:
                log.warn("Unrecognized state: {}", mainState);
        }
    }

    @Override
    public void shutDown()
    {
        // Shutdown logic here
    }

    @Override
    public net.unethicalite.api.plugins.Task[] getTasks()
    {
        return (net.unethicalite.api.plugins.Task[]) tasks;
    }

    public void setMainState(MainState mainState) {
    }

    public enum MainState
    {
        PICK_NAME,
        CREATE_CHARACTER_DESIGN,
        GIELINOR_GUIDE,
        OPEN_OPTIONS_TAB,
        OPEN_DOOR_TO_SURVIVAL_EXPERT,
        SURVIVAL_EXPERT,
        FISH_SHRIMPS,
        OPEN_SKILLS_TAB,
        SURVIVAL_EXPERT_AGAIN,
        CHOP_TREE,
        MAKE_FIRE,
        COOK_SHRIMPS,
        COOKING_GUIDE_SECTION,
        QUEST_SECTION,
        MINING_SECTION,
        COMBAT_SECTION,
        BANKING_AREA_SECTION,
        CHURCH_GUIDE_SECTION,
        WIZARD_GUIDE_SECTION,
        IN_LUMBRIDGE
    }

    private MainState determineCurrentState()
    {
        // Implement logic to determine the current state
        return DEFAULT_STATE;
    }

    public void handleDialogue()
    {
        Widget npcTextWidget = client.getWidget(ComponentID.DIALOG_NPC_TEXT);
        Widget playerTextWidget = client.getWidget(ComponentID.DIALOG_PLAYER_TEXT);
        Widget playerOptionsWidget = client.getWidget(ComponentID.DIALOG_OPTION_OPTIONS);

        if (npcTextWidget != null && !npcTextWidget.getText().isEmpty())
        {
            String npcText = npcTextWidget.getText();

            if (npcText.contains("What is your name, adventurer?"))
            {
                setCurrentState(MainState.PICK_NAME);
            }
            else if (npcText.contains("Please choose a name for your character"))
            {
                setCurrentState(MainState.PICK_NAME);
            }
            else if (npcText.contains("Welcome to Gielinor!"))
            {
                setCurrentState(MainState.GIELINOR_GUIDE);
            }
            else if (npcText.contains("And that's the basics of the interface."))
            {
                setCurrentState(MainState.OPEN_OPTIONS_TAB);
            }
        }
        else if (playerTextWidget != null && !playerTextWidget.getText().isEmpty())
        {
            String playerText = playerTextWidget.getText();

            if (playerText.contains("Hi, I'm the Gielinor Guide."))
            {
                setCurrentState(MainState.GIELINOR_GUIDE);
            }
        }
        else if (playerOptionsWidget != null && playerOptionsWidget.getChildren().length > 0)
        {
            String[] options = new String[playerOptionsWidget.getChildren().length];

            for (int i = 0; i < options.length; i++)
            {
                options[i] = playerOptionsWidget.getChildren()[i].getText();
            }

            if (options.length == 2 && options[0].contains("Open the door") && options[1].contains("Open the door"))
            {
                setCurrentState(MainState.OPEN_DOOR_TO_SURVIVAL_EXPERT);
            }
            else if (options.length == 2 && options[0].contains("Talk-to") && options[1].contains("Talk-to"))
            {
                setCurrentState(MainState.SURVIVAL_EXPERT);
            }
        }
    }

    private void setCurrentState(MainState state)
    {
        // Implement logic to set the current state
    }

    private void handleNamePicker()
    {
        // Implement name picker handling logic
    }

    private void handleCharacterDesign()
    {
        // Implement character design handling logic
    }

    private void handleGielinorGuideInteraction()
    {
        // Implement Gielinor Guide interaction logic
    }

    private void handleOpenOptionsTab()
    {
        // Implement opening options tab logic
    }

    private void interactWithDoor()
    {
        // Implement door interaction logic
    }

    private void handleSurvivalExpert()
    {
        // Implement Survival Expert handling logic
    }

    private void handleFishShrimps()
    {
        // Implement fishing shrimps logic
    }

    private void handleOpenSkillsTab()
    {
        // Implement opening skills tab logic
    }

    private void handleSurvivalExpertAgain()
    {
        // Implement handling Survival Expert again logic
    }

    private void handleTreeInteraction()
    {
        // Implement tree interaction logic
    }

    private void handleMakeFire()
    {
        // Implement making fire logic
    }

    private void handleCookShrimps()
    {
        // Implement cooking shrimps logic
    }

    private void handleCookingGuideSection()
    {
        // Implement Cooking Guide section handling logic
    }

    private static class DisplayNamePickerTask implements Task {
        public DisplayNamePickerTask(TutorialIslandPlugin sTutorialIslandPlugin) {
        }

        @Override
        public boolean validate() {
            return false;
        }

        @Override
        public int execute() {
            return 0;
        }

        @Override
        public boolean isComplete() {
            return false;
        }

        @Override
        public void run() {

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
    }
}