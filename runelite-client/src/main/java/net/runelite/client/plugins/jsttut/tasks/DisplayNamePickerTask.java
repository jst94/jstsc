package net.runelite.client.plugins.jsttut.tasks;

import net.runelite.api.Skill;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.runelite.client.plugins.jsttut.TutorialIslandPlugin;
import net.unethicalite.api.commons.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DisplayNamePickerTask implements Task {
    private static final Logger log = LoggerFactory.getLogger(DisplayNamePickerTask.class);
    private final TutorialIslandPlugin plugin;

    public DisplayNamePickerTask(TutorialIslandPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean validate() {
        return this.plugin.getMainState() == TutorialIslandPlugin.MainState.PICK_NAME;
    }

    public int execute() {
        log.info("Handling display name picker...");
        if (!this.isDisplayNamePickerOpen()) {
            log.warn("Display name picker is not open.");
            return 1000;
        } else {
            Widget nameInputWidget = this.findChildWidget(10747920, "Enter name");
            if (nameInputWidget == null) {
                log.warn("Name input widget not found.");
                return 1000;
            } else {
                String randomName = this.generateRandomName();
                nameInputWidget.setText(randomName);
                log.info("Generated random name: {}", randomName);
                Widget lookupNameButton = this.findChildWidget(10747920, "Look up name");
                if (lookupNameButton == null) {
                    log.warn("Look up name button not found.");
                    return 1000;
                } else {
                    lookupNameButton.interact("Look up name");
                    log.info("Clicked 'Look up name' button.");
                    Time.sleep(3000L);
                    if (this.isNameAvailable()) {
                        this.confirmNameSelection();
                        log.info("Name selected and confirmed: {}", randomName);
                        this.plugin.setMainState(TutorialIslandPlugin.MainState.CREATE_CHARACTER_DESIGN);
                    } else {
                        log.info("Name is not available, generating a new one...");
                    }

                    return 1000;
                }
            }
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

    private boolean isDisplayNamePickerOpen() {
        return true;
    }

    private Widget findChildWidget(int id, String name) {
        return null;
    }

    private String generateRandomName() {
        return "TestName";
    }

    private boolean isNameAvailable() {
        return true;
    }

    private void confirmNameSelection() {
    }

    public boolean isBlocking() {
        return true;
    }

    public boolean subscribe() {
        return false;
    }
}
