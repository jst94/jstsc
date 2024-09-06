package net.runelite.client.plugins.jstaccbuilder.tasks.skills.mining;

import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Rand;

public class MiningData {

    // 3 ore spots
    private static final WorldPoint LUMBRIDGE_TIN = new WorldPoint(3223, 3147, 0);
    // 2 ore spots
    private static final WorldPoint LUMBRIDGE_COPPER_SOUTH = new WorldPoint(3228, 3145, 0);
    private static final WorldPoint LUMBRIDGE_COPPER_EAST = new WorldPoint(3230, 3146, 0);
    private static final WorldPoint LUMBRIDGE_COPPER_NORTH = new WorldPoint(3229, 3147, 0);
    private static final WorldPoint RIMMINGTON_COPPER_EAST = new WorldPoint(2978, 3247, 0);
    private static final WorldPoint RIMMINGTON_COPPER_WEST = new WorldPoint(2977, 3246, 0);
    private static final WorldPoint VARROCK_TIN_WEST = new WorldPoint(3282, 3363, 0);
    private static final WorldPoint VARROCK_COPPER_SOUTH = new WorldPoint(3286, 3361, 0);
    private static final WorldPoint VARROCK_COPPER_EAST = new WorldPoint(3289, 3362, 0);
    private static final WorldPoint VARROCK_COPPER_NORTH = new WorldPoint(3287, 3364, 0);
    private static final WorldPoint[] MINING_LOCATIONS = {LUMBRIDGE_TIN, LUMBRIDGE_COPPER_SOUTH, LUMBRIDGE_COPPER_EAST,
            LUMBRIDGE_COPPER_NORTH, RIMMINGTON_COPPER_EAST, RIMMINGTON_COPPER_WEST, VARROCK_TIN_WEST, VARROCK_COPPER_SOUTH,
            VARROCK_COPPER_EAST, VARROCK_COPPER_NORTH};
    private static final int[] DROPPABLES = {ItemID.COPPER_ORE, ItemID.IRON_ORE, ItemID.CLAY, ItemID.TIN_ORE,
            ItemID.SILVER_ORE, ItemID.COAL, ItemID.GOLD_ORE, ItemID.MITHRIL_ORE, ItemID.ADAMANTITE_ORE,
            ItemID.RUNITE_ORE, ItemID.UNCUT_SAPPHIRE, ItemID.UNCUT_EMERALD, ItemID.UNCUT_RUBY, ItemID.UNCUT_DIAMOND, ItemID.CLUE_GEODE_BEGINNER,
            ItemID.CLUE_GEODE_EASY, ItemID.CLUE_GEODE_MEDIUM, ItemID.CLUE_GEODE_HARD, ItemID.CLUE_GEODE_ELITE};

    public WorldPoint getRandomMiningPosition() {
        int randomIndex = Rand.nextInt(0, MINING_LOCATIONS.length - 1);
        return MINING_LOCATIONS[randomIndex];
    }

    public int[] getDroppables() {
        return DROPPABLES;
    }


}
