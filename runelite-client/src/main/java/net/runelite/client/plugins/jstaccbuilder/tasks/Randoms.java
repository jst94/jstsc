package net.runelite.client.plugins.jstaccbuilder.tasks;

import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.jstaccbuilder.AccBuilder;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;

public class Randoms implements Task {
    private final AccBuilder main;

    public Randoms(AccBuilder main) {
        RuneLite.getInjector().getInstance(EventBus.class).register(this);
        this.main = main;
    }

    @Override
    public Skill getSkill() {
        return null; // or return an appropriate Skill if applicable
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

    /**
     * @return
     */
    @Override
    public boolean validate() {
        return false;
    }

    /**
     * @return
     */
    @Override
    public int execute() {
        return 0;
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    public void run() {
        // Flippa

        if (Dialog.isOpen()) {
            GameThread.invoke(() -> {
                Dialog.invokeDialog(DialogOption.NPC_CONTINUE);
                Dialog.invokeDialog(DialogOption.CHAT_OPTION_ONE);
                Dialog.invokeDialog(DialogOption.PLAYER_CONTINUE);
            });
        }

        Widget flippaScore = Widgets.get(542, 3);
        if (flippaScore != null) {
            int score = Integer.parseInt(flippaScore.getText().split(": ")[1]);
            if (score == 10) {
                TileObject cave = TileObjects.getNearest("Cave Exit");
                if (cave != null) {
                    cave.interact("Exit");
                    Time.sleepUntil(() -> Widgets.get(542, 3) == null, 10_000);
                }
                return;
            }
            TileObject targetPost = TileObjects.getNearest(o -> {
                if (o instanceof GameObject) {
                    GameObject gameObject = (GameObject) o;
                    Renderable renderable = gameObject.getRenderable();
                    if (renderable instanceof DynamicObject) {
                        DynamicObject dynamicObject = (DynamicObject) renderable;
                        int animationID = dynamicObject.getAnimationID();
                        return o.getName().equals("Pinball Post") && animationID == 4005;
                    }
                }
                return false;
            });

            if (targetPost != null) {
                targetPost.interact("Tag");
                Time.sleepUntil(() -> {
                    TileObject tileObject = TileObjects.getNearest(o -> {
                        if (o instanceof GameObject) {
                            GameObject gameObject = (GameObject) o;
                            Renderable renderable = gameObject.getRenderable();
                            if (renderable instanceof DynamicObject) {
                                DynamicObject dynamicObject = (DynamicObject) renderable;
                                int animationID = dynamicObject.getAnimationID();
                                return o.getName().equals("Pinball Post") && animationID == 4005;
                            }
                        }
                        return false;
                    });

                    if (tileObject != null) {
                        Renderable renderable = ((GameObject) tileObject).getRenderable();
                        if (renderable instanceof DynamicObject) {
                            DynamicObject dynamicObject = (DynamicObject) renderable;
                            return dynamicObject.getAnimationID() != 4005;  // Replace 4005 with the initial animation ID if it can be different
                        }
                    }
                    return false;
                }, 30000);  // timeout after 30,000 milliseconds (30 seconds)
            }
        }

    }
}
