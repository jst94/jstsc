package net.runelite.client.plugins.jsttut.tasks;

import net.runelite.api.Skill;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;

public class QuestGuideTask implements Task {
    public QuestGuideTask() {
    }

    public boolean validate() {
        return false;
    }

    public int execute() {
        return 0;
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

    public boolean isBlocking() {
        return false;
    }

    public boolean subscribe() {
        return false;
    }
}
