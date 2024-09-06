package net.runelite.client.plugins.jstaccbuilder.tasks.utils;

import com.google.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.Widget;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.game.GameSettings;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.packets.DialogPackets;
import net.unethicalite.api.packets.WidgetPackets;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;

import java.util.*;
import java.util.function.Supplier;

@Getter
@Slf4j
@Singleton
public class SettingsCheck {

    private static final int ALL_SETTINGS_WIDGET_ID = 7602208;
    private static final int MENU_TAB_WIDGET_ID = 8781847;
    private static final int OPTION_WIDGET_ID = 8781843;
    private static final int CLOSE_BUTTON_WIDGET_ID = 8781828;
    /**
     * -- GETTER --
     * Checks if the game settings have been adjusted.
     *
     * @return True if settings have been adjusted, otherwise false
     */
    // Flag indicating if the settings have been adjusted
    private boolean settingsFixed = false;

    public SettingsCheck() {
    }

    /**
     * Queues actions on widgets to adjust game settings.
     *
     * @param menuTab  The menu tab widget ID
     * @param options  List of option widget IDs
     * @param widgetId The widget ID
     */
    private void queueWidgetAction(int menuTab, List<Integer> options, int widgetId) {
        log.debug("Menu tab: " + menuTab);
        GameThread.invoke(() ->
        {
            WidgetPackets.queueWidgetAction1Packet(ALL_SETTINGS_WIDGET_ID, -1, -1);
            WidgetPackets.queueWidgetAction1Packet(MENU_TAB_WIDGET_ID, -1, menuTab);
            for (int option : options) {
                log.debug("Option: " + option);
                WidgetPackets.queueWidgetAction1Packet(widgetId, -1, option);
            }
            WidgetPackets.queueWidgetAction1Packet(CLOSE_BUTTON_WIDGET_ID, -1, -1);
        });
    }

    /**
     * Adjusts various game settings for optimal script execution.
     *
     * @return Time to wait in milliseconds before the next operation
     */
    public int configureSettings() {
        log.debug("Configuring");

        if (!GameSettings.Audio.isFullMuted()) {
            if (Dialog.isOpen()) {
                DialogPackets.closeInterface();
                return 600;
            }
            log.debug("Muting");
            Widget widget;
            if (Vars.getVarp(168) != 0) {
                widget = Widgets.get(116, 93);
                widget.interact(Objects.requireNonNull(widget.getActions())[0]);
            }
            if (Vars.getVarp(169) != 0) {
                widget = Widgets.get(116, 107);
                widget.interact(Objects.requireNonNull(widget.getActions())[0]);
            }
            if (Vars.getVarp(872) != 0) {
                widget = Widgets.get(116, 122);
                widget.interact(Objects.requireNonNull(widget.getActions())[0]);
            }
            return 1000;
        }

        // Create and define the settings categories
        List<SettingsCategory> categories = Arrays.asList(
                new SettingsCategory(1, 8781845, new HashMap<Integer, Supplier<Boolean>>() {
                    {
                        put(21, () -> Vars.getVarp(168) != 0); // Disable Music
                        put(42, () -> Vars.getVarp(169) != 0); // Disable Sound effect
                        put(63, () -> Vars.getVarp(872) != 0); // Disable Area sound
                    }
                }),
                new SettingsCategory(2, OPTION_WIDGET_ID, new HashMap<Integer, Supplier<Boolean>>() {
                    {
                        put(1, () -> Vars.getVarp(1074) == 0); // Disable profanity filter
                    }
                }),
                new SettingsCategory(3, OPTION_WIDGET_ID, new HashMap<Integer, Supplier<Boolean>>() {
                    {
                        put(7, () -> Vars.getBit(5542) == 0); // Enable shift dropping
                        put(39, () -> Vars.getBit(4681) == 0); // Enable escape close interface
                    }
                }),
                new SettingsCategory(4, OPTION_WIDGET_ID, new HashMap<Integer, Supplier<Boolean>>() {
                    {
                        put(4, () -> Vars.getBit(12378) == 0); // Enable hide roofs
                    }
                }),
                new SettingsCategory(5, OPTION_WIDGET_ID, new HashMap<Integer, Supplier<Boolean>>() {
                    {
                        put(1, () -> Vars.getBit(4180) == 1); // Disable accept aid
                        put(5, () -> Vars.getBit(14819) == 1); // Disable Make-x darts
                        put(6, () -> Vars.getBit(5697) == 0); // Enable Ammo-picking behaviour
                        put(7, () -> Vars.getBit(5698) == 0); // Enable Rune-picking behaviour
                        put(17, () -> Vars.getBit(14197) == 0); // Disable baba yaga camera
                        put(18, () -> Vars.getBit(4814) == 0); // Disable fishing trawler came
                        put(19, () -> Vars.getBit(14198) == 0); // Disable barrows camera
                    }
                }),
                new SettingsCategory(6, OPTION_WIDGET_ID, new HashMap<Integer, Supplier<Boolean>>() {
                    {
                        put(14, () -> Vars.getBit(13037) == 1); // Disable store button
                        put(15, () -> Vars.getBit(Varbits.WIKI_ENTITY_LOOKUP) == 0); // Disable wiki lookup
                        put(16, () -> Vars.getBit(5368) == 0); // Disable  activity adviser
                        put(19, () -> Vars.getBit(13130) == 0); // Disable trade delays
                        put(21, () -> Vars.getBit(Varbits.DISABLE_LEVEL_UP_INTERFACE) == 0); // disable level up interface
                    }
                }),
                new SettingsCategory(7, OPTION_WIDGET_ID, new HashMap<Integer, Supplier<Boolean>>() {
                    {
                        put(32, () -> Vars.getBit(4100) == 0); // Disable world hop warning
                        put(39, () -> Vars.getBit(14700) == 0); // Disable GE Buy warning
                        put(40, () -> Vars.getBit(14701) == 0); // Disable GE Sell warning
                    }
                })
        );

        for (SettingsCategory category : categories) {
            if (Dialog.isOpen()) {
                log.debug("Closing interface");
                DialogPackets.closeInterface();
                Time.sleepTick();
            }
            category.applySettings();
        }

        log.debug("Settings fixed");
        settingsFixed = true;

        return 1000;
    }

    private class SettingsCategory {
        int menuTab;
        int widgetId;
        Map<Integer, Supplier<Boolean>> conditions;

        public SettingsCategory(int menuTab, int widgetId, Map<Integer, Supplier<Boolean>> conditions) {
            this.menuTab = menuTab;
            this.widgetId = widgetId;
            this.conditions = conditions;
        }

        public void applySettings() {
            List<Integer> optionsToQueue = new ArrayList<>();
            for (Map.Entry<Integer, Supplier<Boolean>> entry : conditions.entrySet()) {
                if (entry.getValue().get()) {
                    optionsToQueue.add(entry.getKey());
                }
            }

            if (!optionsToQueue.isEmpty()) {
                queueWidgetAction(menuTab, optionsToQueue, widgetId);
                for (Integer option : optionsToQueue) {
                    log.debug("Before: " + option);
                    Time.sleepUntil(() -> !conditions.get(option).get(), 5000);
                    log.debug("After");
                }
            }
        }
    }
}