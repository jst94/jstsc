package net.runelite.client.plugins.jsttut.tasks;

import net.runelite.api.Skill;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;

import net.runelite.client.plugins.jsttut.TutorialIslandPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterCreationTask implements Task {
    private static final String CHARACTER_CREATION_MESSAGE = "Handling character creation...";
    private final TutorialIslandPlugin tutorialIslandPlugin;
    private static final Logger logger = LoggerFactory.getLogger(CharacterCreationTask.class);

    public CharacterCreationTask(TutorialIslandPlugin tutorialIslandPlugin) {
        this.tutorialIslandPlugin = tutorialIslandPlugin;
    }

    @Override
    public boolean validate() {
        if (this.tutorialIslandPlugin == null) {
            logger.debug("TutorialIslandPlugin is null");
            return false;
        }

        boolean isValid = this.tutorialIslandPlugin.getMainState() == TutorialIslandPlugin.MainState.CREATE_CHARACTER_DESIGN;
        logger.debug("Validation result: {}", isValid);
        return isValid;
    }

    @Override
    public int execute() {
        System.out.println(CHARACTER_CREATION_MESSAGE);
        setNextState();
        return 0;
    }

    private void setNextState() {
        this.tutorialIslandPlugin.setMainState(TutorialIslandPlugin.MainState.GIELINOR_GUIDE);
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