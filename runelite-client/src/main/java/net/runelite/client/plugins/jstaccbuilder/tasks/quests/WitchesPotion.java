package net.runelite.client.plugins.jstaccbuilder.tasks.quests;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.jstaccbuilder.TradeUnlockerConfig;
import net.runelite.client.plugins.jstaccbuilder.tasks.Task;
import net.unethicalite.api.account.LocalPlayer;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Vars;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.items.Shop;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.quests.Quests;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Production;

import static net.unethicalite.api.quests.QuestVarPlayer.QUEST_WITCHS_POTION;

@Slf4j
public class WitchesPotion implements Task {
    private static final WorldPoint HETTY = new WorldPoint(2969, 3205, 0);
    private static final WorldArea RAT_TAIL_AREA = new WorldArea(2952, 3202, 9, 4, 0);
    private static final WorldPoint RAT_TAIL_POSITION = new WorldPoint(2956, 3204, 0);
    private static final WorldArea RAT_AREA = new WorldArea(2988, 3191, 17, 9, 0);
    private static final WorldPoint RAT_POSITION = new WorldPoint(2998, 3195, 0);
    private static final WorldPoint BETTY = new WorldPoint(3012, 3258, 0);
    private static final WorldPoint RANGE = new WorldPoint(2969, 3210, 0);
    private static final WorldPoint ONION = new WorldPoint(2954, 3253, 0);
    private final QuestUtils questUtils;
    private final TradeUnlockerConfig config;
    private int idBefore;
    private WorldPoint nearestBank;
    private long lastAnimation;

    public WitchesPotion(QuestUtils questUtils, TradeUnlockerConfig config) {
        RuneLite.getInjector().getInstance(EventBus.class).register(this);
        this.questUtils = questUtils;
        idBefore = -1;
        this.config = config;
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

    /**
     * Checks if the Witch's Potion quest is complete.
     *
     * @return true if the quest is finished or player's quest points are >= 10; false otherwise.
     */
    public boolean isComplete() {
        return Quests.isFinished(Quest.WITCHS_POTION) || LocalPlayer.getQuestPoints() >= config.getQuestPoints() || Players.getLocal().getCombatLevel() < 4;
    }

    /**
     * Main method to run the quest tasks based on the current progress.
     */
    public void run() {
        int progress = Vars.getVarp(QUEST_WITCHS_POTION.getId());
        if (idBefore != progress) {
            idBefore = progress;
        }
        log.debug("Progress (" + progress + ")");

        //67
        switch (progress) {
            case 0:
                if (!Inventory.contains(ItemID.EYE_OF_NEWT)) {
                    getEyeOfNewt();
                    return;
                }
                questUtils.talkToNPC(n -> n.getName().equals("Hetty"), HETTY, Dialogs.HETTY);
                break;
            case 1:
                if (!Inventory.contains(ItemID.RATS_TAIL) || !Inventory.contains(ItemID.BURNT_MEAT) || !Inventory.contains(ItemID.ONION)) {
                    getQuestItems();
                    return;
                }
                log.debug("Talk to hetty");
                questUtils.talkToNPC(n -> n.getName().equals("Hetty"), HETTY, Dialogs.HETTY);
                break;
            case 2:
                if (Dialog.isOpen()) {
                    log.debug("Continue dialog");
                    Dialog.continueSpace();
                    return;
                }
                TileObject cauldron = TileObjects.getNearest("Cauldron");
                log.debug("Drink from Cauldron");
                if (cauldron != null) {
                    cauldron.interact("Drink From");
                }
                break;
        }
    }

    @Override
    public Skill getSkill() {
        return null;
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

    private void getQuestItems() {
        if (!Inventory.contains(ItemID.RATS_TAIL)) {
            getRatsTail();
            return;
        }
        if (!Inventory.contains(ItemID.BURNT_MEAT)) {
            getBurntMeat();
            return;
        }
        if (!Inventory.contains(ItemID.ONION)) {
            getOnion();
            return;
        }
    }

    private void getOnion() {
        log.debug("Picking an onion");
        questUtils.getItemFromObject(o -> o.getName().equals("Onion"), "Pick", ONION, ItemID.ONION);
    }

    private void getBurntMeat() {
        if (Inventory.contains(ItemID.RAW_RAT_MEAT) || Inventory.contains(ItemID.COOKED_MEAT)) {
            TileObject range = TileObjects.getNearest(n -> n.hasAction("Cook") && n.getName().equals("Range") && Reachable.isInteractable(n) && n.distanceTo(Players.getLocal()) < 15);
            if (range != null) {
                if (Production.isOpen()) {
                    log.debug("Burning the meat");
                    Production.chooseOption(0);
                    return;
                }
                log.debug("Cooking the meat");
                boolean raw = Inventory.contains(ItemID.RAW_RAT_MEAT);
                range.interact("Cook");
                Time.sleepUntil(() -> Inventory.contains(ItemID.BURNT_MEAT) || (raw && Inventory.contains(ItemID.COOKED_MEAT)), 10_000);
                return;
            }
            questUtils.walkTo(RANGE);
            return;
        }
        TileItem meat = TileItems.getNearest(n -> n.getId() == ItemID.RAW_RAT_MEAT && Reachable.isInteractable(n) && Players.getLocal().distanceTo(n) < 15);
        if (meat != null) {
            log.debug("Picking up rat meat");
            meat.pickup();
            Time.sleepUntil(() -> Inventory.contains(ItemID.RAW_RAT_MEAT), 5000);
            return;
        }
        NPC rat = Combat.getAttackableNPC(n -> n.getName() != null && n.getName().equals("Giant rat") && !n.isDead() && RAT_AREA.contains(n) && Reachable.isInteractable(n) && Players.getLocal().distanceTo(n) < 15);
        if (rat != null) {
            log.debug("Attacking Giant rat");
            lastAnimation = System.currentTimeMillis();
            rat.interact("Attack");
            Time.sleepUntil(() -> TileItems.getNearest(ItemID.RAW_RAT_MEAT) != null || stoppedAnimating(), 100_000);
            return;
        }
        if (!RAT_AREA.contains(Players.getLocal())) {
            log.debug("Walking to giant rats");
            questUtils.walkTo(RAT_POSITION);
        }
    }

    private boolean stoppedAnimating() {
        if (Players.getLocal().isAnimating()) {
            lastAnimation = System.currentTimeMillis();
        }
        return System.currentTimeMillis() - lastAnimation > 5000;
    }

    private void getRatsTail() {
        TileItem ratTail = TileItems.getNearest(ItemID.RATS_TAIL);
        if (ratTail != null) {
            log.debug("Taking rats tail");
            ratTail.interact("Take");
            Time.sleepUntil(() -> Inventory.contains(ItemID.RATS_TAIL), 5000);
            return;
        }
        NPC rat = Combat.getAttackableNPC(n -> n.getName() != null && n.getName().equals("Rat") && !n.isDead() && RAT_TAIL_AREA.contains(n) && Reachable.isInteractable(n) && Players.getLocal().distanceTo(n) < 15);
        if (rat != null) {
            log.debug("Attacking rat");
            rat.interact("Attack");
            Time.sleepUntil(() -> Players.getLocal().isAnimating(), 10_000);
            Time.sleepUntil(() -> TileItems.getNearest(ItemID.RATS_TAIL) != null || stoppedAnimating(), 50_000);
            return;
        }
        if (!RAT_TAIL_AREA.contains(Players.getLocal())) {
            log.debug("Walking to rat");
            questUtils.walkTo(RAT_TAIL_POSITION);
        }
    }

    private void getEyeOfNewt() {
        if (!Inventory.contains(ItemID.COINS_995) || Inventory.getFirst(ItemID.COINS_995).getQuantity() < 3) {
            getCoins();
            return;
        }
        buyNewt();
    }

    private void buyNewt() {
        if (Shop.isOpen()) {
            log.debug("Buying newt");
            Shop.buyOne(ItemID.EYE_OF_NEWT);
            return;
        }
        NPC betty = NPCs.getNearest(n -> n.getName().equals("Betty") && Reachable.isInteractable(n) && n.distanceTo(Players.getLocal()) < 15);
        if (betty != null) {
            log.debug("Tradaing betty");
            betty.interact("Trade");
            return;
        }

        log.debug("Walking to betty");
        questUtils.walkTo(BETTY);
    }

    private void getCoins() {
        if (Bank.isOpen()) {
            if (Bank.contains(ItemID.COINS_995) && Bank.getFirst(ItemID.COINS_995).getQuantity() >= 3) {
                log.debug("Withdrawing coins");
                Bank.withdraw(ItemID.COINS_995, 1, Bank.WithdrawMode.ITEM);
                Bank.withdraw(ItemID.COINS_995, 1, Bank.WithdrawMode.ITEM);
                Bank.withdraw(ItemID.COINS_995, 1, Bank.WithdrawMode.ITEM);
                Time.sleepUntil(() -> Inventory.contains(ItemID.COINS_995), 5000);
                return;
            }
        }
        if (BankLocation.getNearest().getArea().contains(Players.getLocal())) {
            log.debug("Opening bank");
            questUtils.openBank();
            return;
        }
        if (nearestBank == null) {
            nearestBank = questUtils.getNearestBank(Players.getLocal().getWorldLocation()).getArea().getCenter();
            return;
        }
        log.debug("Walking to bank");
        questUtils.walkTo(nearestBank);
    }

    public static final class Dialogs {
        public static final String[] HETTY = {"I am in search of a quest.", "Yes."};
    }
}
