package net.runelite.client.plugins.jsttut.tasks;

import net.runelite.api.Skill;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;

public class CookingGuideTask implements Task {
    public CookingGuideTask() {
    }

    public boolean validate() {
        return false;
    }

    public int execute() {
        return 0;
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    public boolean isBlocking() {
        return false;
    }

    @Override
    public void run() {
        // Implement the run method
    }

    /**
     * @return
     */
    @Override
    public Skill getSkill() {
        return null;
    }

    public boolean subscribe() {
        return true; // or false, depending on your requirements
    }
}
