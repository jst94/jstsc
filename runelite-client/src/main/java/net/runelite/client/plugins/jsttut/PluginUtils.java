package net.runelite.client.plugins.jsttut;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.unethicalite.api.commons.Time;

public class PluginUtils {
    public PluginUtils() {
    }

    public static boolean isClientReady(Client client) {
        if (client == null) {
            return false;
        } else {
            return client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null;
        }
    }

    public static boolean waitForClientReady(Client client, int retries, int delay) {
        for (int i = 0; i < retries; ++i) {
            if (isClientReady(client)) {
                return true;
            }

            Time.sleep((long) delay);
        }

        return false;
    }
}
