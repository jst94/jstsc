package net.runelite.client.plugins.jstfish;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.Skills;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.Plugins;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Dialog;
import org.pf4j.Extension;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;

@Extension
@PluginDescriptor(
        name = "Jst Fisher",
        enabledByDefault = false
)
public class Fisher extends Script
{
    @Inject
    private net.runelite.client.plugins.jstfish.FisherConfig config;    @Inject
    private ConfigManager configManager;
    @Inject
    private Client client;
    @Inject
    private ItemManager itemManager;
    private boolean fishAgain;
    private boolean inDialog;
    private final int[] UNDROPPABLES = new int[]{303, 314, 309};
    private int nextEnergy;
    private SecureRandom secureRandom = new SecureRandom();
    private final WorldPoint netSW = new WorldPoint(3261, 3136, 0);
    private final WorldPoint netNE = new WorldPoint(3281, 3153, 0);
    private final WorldArea netArea;

    private final WorldPoint barbSE;
    private final WorldPoint barbNE;
    private final WorldArea barbArea;
    private final WorldPoint barbWalkSE;
    private final WorldPoint barbWalkNE;
    private final WorldArea barbWalkArea;
    private final int SMALL_FISHING_NET;

    public Fisher()
    {
        this.netArea = new WorldArea(this.netSW, this.netNE);
        //this.webhook = "https://discordapp.com/api/webhooks/1138381859882016828/ALz8nqgjmvYCLjUySUareeXja6DfGLqefNSuM1J2RUUTh0ljYmC9p9NPLfdo9UcRpFt_";
        this.barbSE = new WorldPoint(3099, 3422, 0);
        this.barbNE = new WorldPoint(3109, 3440, 0);
        this.barbArea = new WorldArea(this.barbSE, this.barbNE);
        this.barbWalkSE = new WorldPoint(3100, 3430, 0);
        this.barbWalkNE = new WorldPoint(3104, 3435, 0);
        this.barbWalkArea = new WorldArea(this.barbWalkSE, this.barbWalkNE);
        this.SMALL_FISHING_NET = 303;
    }

    @Provides
    public net.runelite.client.plugins.jstfish.FisherConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(net.runelite.client.plugins.jstfish.FisherConfig.class);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() == ChatMessageType.GAMEMESSAGE && event.getMessage().contains("Oh dear, you are dead"))
        {
        }
    }

    protected int loop()
    {
        try
        {
            if (!Game.isLoggedIn())
            {
                return 1000;
            }

            if (Inventory.isFull())
            {
                MessageUtils.addMessage("Dropping inventory");
                Inventory.getAll(new int[]{317, 321, 331, 335}).forEach(Item::drop);
                this.fishAgain = true;
                return 250;
            }

            if (!Movement.isRunEnabled() && Movement.getRunEnergy() > this.nextEnergy)
            {
                Movement.toggleRun();
                this.nextEnergy = this.secureRandom.nextInt(31) + 30;
                return 1000;
            }

            this.inDialog = Dialog.canLevelUpContinue();
            if (!this.inDialog && Players.getLocal().isAnimating() && !this.fishAgain || Movement.isWalking())
            {
                return 1000;
            }

            int fishLvl = Skills.getLevel(Skill.FISHING);
            if (fishLvl < 20)
            {
                this.fishShrimp();
            }
            else if (fishLvl < 82)
            {
                this.fishTrout();
            }
            else
            {
                // Remove Discord-related code as it cannot be resolved
                SwingUtilities.invokeLater(() ->
                {
                    Plugins.stopPlugin(this);
                });
                System.exit(0);
            }
        }
        catch (Exception var4)
        {
            Exception e = var4;
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);
            printWriter.flush();
            System.out.println(writer.toString());
        }

        return 1000;
    }

    public void onStart(String... args)
    {
        this.setNextEnergy();
    }

    private void fishTrout()
    {
        if (Inventory.contains(new int[]{309}) && Inventory.contains(new int[]{314}))
        {
            NPC fishSpot = NPCs.getNearest((n) ->
            {
                return n.getName().equals("Rod Fishing spot") && n.hasAction(new String[]{"Lure"});
            });
            if (this.inDialog)
            {
                Time.sleep(1000, 2000);
            }

            if (fishSpot != null)
            {
                fishSpot.interact("Lure");
                this.fishAgain = false;
                Time.sleepUntil(() ->
                {
                    return Players.getLocal().isMoving();
                }, 5000);
                Time.sleepUntil(() ->
                {
                    return Players.getLocal().isAnimating() || Players.getLocal().getInteracting() == null;
                }, 10000);
            }
            else if (!this.barbArea.contains(Players.getLocal()))
            {
                Movement.walkTo(this.barbWalkArea);
            }
        }
    }

    private void fishShrimp()
    {
        if (Inventory.contains(new int[]{303}))
        {
            NPC fishSpot = NPCs.getNearest((n) ->
            {
                return n.getName().equals("Fishing spot") && n.hasAction(new String[]{"Small Net"});
            });
            if (this.inDialog)
            {
                Time.sleep(500, 1500);
            }

            if (fishSpot != null)
            {
                fishSpot.interact("Small Net");
                this.fishAgain = false;
                Time.sleepUntil(() ->
                {
                    return Players.getLocal().isMoving();
                }, 5000);
            }
            else if (!this.netArea.contains(Players.getLocal()))
            {
                Movement.walkTo(this.netArea);
            }
        }
    }

    private void setNextEnergy()
    {
        this.nextEnergy = this.secureRandom.nextInt(31) + 30;
    }
}
