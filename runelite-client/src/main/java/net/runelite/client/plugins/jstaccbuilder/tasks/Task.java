package net.runelite.client.plugins.jstaccbuilder.tasks;

public interface Task {
    boolean validate();

    int execute();

    boolean isComplete();

    void run();

    net.runelite.api.Skill getSkill();

    boolean isBlocking();

    boolean subscribe();
}
