package net.runelite.client.plugins.jstaccbuilder.tasks;

import net.runelite.api.Skill;

public interface SkillingTask {
    void resetEndLevel();

    int getCurrentSkillLevel();

    int getTaskLevel();

    Skill getSkill();
}
