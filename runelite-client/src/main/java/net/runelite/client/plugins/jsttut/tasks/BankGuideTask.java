package net.runelite.client.plugins.jsttut.tasks;

import net.unethicalite.api.plugins.Task;

public class BankGuideTask implements Task {
    public BankGuideTask() {
    }

    public boolean validate() {
        return false;
    }

    public int execute() {
        return 0;
    }

    public boolean isBlocking() {
        return Task.super.isBlocking();
    }

    public boolean subscribe() {
        return Task.super.subscribe();
    }
}
