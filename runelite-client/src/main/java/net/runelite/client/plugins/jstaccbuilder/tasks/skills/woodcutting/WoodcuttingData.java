package net.runelite.client.plugins.jstaccbuilder.tasks.skills.woodcutting;

import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.entities.Players;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class WoodcuttingData {

    private final HashMap<WorldArea, WorldArea> areaToWalkAreaMap = new HashMap<>();
    // Array of all Woodcutting Areas
    private final WorldArea[] WOODCUTTING_AREAS = {CowsNorth.AREA, PortSarim.AREA, FaladorSouth.AREA, Lumbridge.AREA, GE.AREA, Varrock.AREA,
            FaladorWest.AREA, FaladorEast.AREA, CooksGuild.AREA, VarrockNorth.AREA};
    // Array of all Walk Areas associated with Woodcutting Areas
    private final WorldArea[] WOODCUTTING_WALK_AREAS = {CowsNorth.WALK_AREA, PortSarim.WALK_AREA, FaladorSouth.WALK_AREA, Lumbridge.WALK_AREA, GE.WALK_AREA, Varrock.WALK_AREA,
            FaladorWest.WALK_AREA, FaladorEast.WALK_AREA, CooksGuild.WALK_AREA, VarrockNorth.WALK_AREA};

    /**
     * Initializes the mapping between woodcutting areas and walk areas.
     */
    public WoodcuttingData() {
        initializeAreaMap();
    }

    /**
     * Creates a new WorldArea given the coordinates of two diagonal points and a plane.
     *
     * @param x1    The x-coordinate of the first point.
     * @param y1    The y-coordinate of the first point.
     * @param x2    The x-coordinate of the second point.
     * @param y2    The y-coordinate of the second point.
     * @param plane The plane or z-level of the area.
     * @return A new WorldArea object.
     */
    private static WorldArea newWorldArea(int x1, int y1, int x2, int y2, int plane) {
        final WorldPoint point1 = new WorldPoint(x1, y1, plane);
        final WorldPoint point2 = new WorldPoint(x2, y2, plane);
        return new WorldArea(point1, point2);
    }

    /**
     * Returns the nearest woodcutting walk area to the player.
     *
     * @return Nearest WorldArea object.
     */
    public WorldArea getNearest() {
        return Arrays.stream(WOODCUTTING_WALK_AREAS)
                .min(Comparator.comparingInt(x -> x.distanceTo2D(Players.getLocal().getWorldLocation())))
                .orElse(null);
    }

    /**
     * Checks if the player is in a woodcutting area.
     *
     * @return True if the player is in a woodcutting area, otherwise false.
     */
    public boolean inArea() {
        for (WorldArea area : WOODCUTTING_AREAS) {
            if (area.contains(Players.getLocal())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a random woodcutting area.
     *
     * @return Random WorldArea object.
     */
    public WorldArea getRandomWoodcuttingArea() {
        int randomIndex = Rand.nextInt(0, WOODCUTTING_AREAS.length - 1);
        return WOODCUTTING_AREAS[randomIndex];
    }

    // ==================== DATA DEFINITIONS ====================

    /**
     * Retrieves the associated walk area for a given woodcutting area.
     *
     * @param woodcuttingArea The woodcutting area to get the walk area for.
     * @return The associated WorldArea object for walking.
     */
    public WorldArea getWalkArea(WorldArea woodcuttingArea) {
        return areaToWalkAreaMap.getOrDefault(woodcuttingArea, null);
    }

    /**
     * Initializes the mapping between woodcutting areas and walk areas.
     */
    private void initializeAreaMap() {
        for (int i = 0; i < WOODCUTTING_AREAS.length; i++) {
            areaToWalkAreaMap.put(WOODCUTTING_AREAS[i],
                    WOODCUTTING_WALK_AREAS[i]);
        }
    }

    // Cows North Area Data
    public static class CowsNorth {
        public static final WorldPoint[] POINTS = {
                new WorldPoint(3155, 3315, 0), new WorldPoint(3161, 3315, 0), new WorldPoint(3164, 3318, 0),
                new WorldPoint(3169, 3318, 0), new WorldPoint(3171, 3316, 0), new WorldPoint(3178, 3316, 0),
                new WorldPoint(3180, 3314, 0), new WorldPoint(3185, 3314, 0), new WorldPoint(3189, 3310, 0),
                new WorldPoint(3190, 3310, 0), new WorldPoint(3192, 3308, 0), new WorldPoint(3195, 3308, 0),
                new WorldPoint(3196, 3307, 0), new WorldPoint(3198, 3307, 0), new WorldPoint(3199, 3308, 0),
                new WorldPoint(3201, 3308, 0), new WorldPoint(3201, 3333, 0), new WorldPoint(3198, 3336, 0),
                new WorldPoint(3195, 3336, 0), new WorldPoint(3193, 3338, 0), new WorldPoint(3190, 3338, 0),
                new WorldPoint(3186, 3342, 0), new WorldPoint(3185, 3342, 0), new WorldPoint(3184, 3343, 0),
                new WorldPoint(3183, 3343, 0), new WorldPoint(3181, 3345, 0), new WorldPoint(3177, 3345, 0),
                new WorldPoint(3175, 3343, 0), new WorldPoint(3166, 3343, 0), new WorldPoint(3165, 3344, 0),
                new WorldPoint(3163, 3344, 0), new WorldPoint(3160, 3347, 0), new WorldPoint(3155, 3347, 0),
                new WorldPoint(3153, 3345, 0), new WorldPoint(3153, 3335, 0), new WorldPoint(3154, 3334, 0),
                new WorldPoint(3154, 3316, 0)
        };
        public static final WorldArea WALK_AREA = new WorldArea(3169, 3324, 17, 8, 0);
        // Calculate the boundaries for COWS_NORTH
        private static final int minX = Arrays.stream(POINTS).mapToInt(WorldPoint::getX).min().orElse(0);
        private static final int minY = Arrays.stream(POINTS).mapToInt(WorldPoint::getY).min().orElse(0);
        private static final int maxX = Arrays.stream(POINTS).mapToInt(WorldPoint::getX).max().orElse(0);
        private static final int maxY = Arrays.stream(POINTS).mapToInt(WorldPoint::getY).max().orElse(0);
        public static final WorldArea AREA = newWorldArea(minX, minY, maxX, maxY, 0);
    }

    // Port Sarim Area Data
    public static class PortSarim {
        public static final WorldArea AREA = new WorldArea(3031, 3260, 33, 20, 0);
        public static final WorldArea WALK_AREA = new WorldArea(3041, 3264, 8, 6, 0);
    }

    // South of Falador Area Data
    public static class FaladorSouth {
        public static final WorldArea AREA = new WorldArea(2954, 3277, 54, 42, 0);
        public static final WorldArea WALK_AREA = new WorldArea(2983, 3301, 8, 6, 0);
    }

    // Lumbridge Area Data
    public static class Lumbridge {
        public static final WorldArea AREA = newWorldArea(3159, 3208, 3201, 3256, 0);
        public static final WorldArea WALK_AREA = newWorldArea(3188, 3221, 3194, 3227, 0);
    }

    // GE Area Data
    public static class GE {
        public static final WorldArea AREA = newWorldArea(3147, 3447, 3184, 3464, 0);
        public static final WorldArea WALK_AREA = newWorldArea(3152, 3454, 3159, 3460, 0);
    }

    // Varrock Area Data
    public static class Varrock {
        public static final WorldArea AREA = newWorldArea(3153, 3364, 3173, 3427, 0);
        public static final WorldArea WALK_AREA = newWorldArea(3158, 3400, 3169, 3385, 0);
    }

    // Falador West Area Data
    public static class FaladorWest {
        public static final WorldArea AREA = new WorldArea(2956, 3428, 37, 33, 0);
        public static final WorldArea WALK_AREA = new WorldArea(2966, 3441, 8, 6, 0);
    }

    // Falador East Area Data
    public static class FaladorEast {
        public static final WorldArea AREA = new WorldArea(3037, 3417, 29, 52, 0);
        public static final WorldArea WALK_AREA = new WorldArea(3040, 3437, 7, 10, 0);
    }

    // Cooks Guild Area Data
    public static class CooksGuild {
        public static final WorldArea AREA = new WorldArea(3118, 3417, 31, 27, 0);
        public static final WorldArea WALK_AREA = new WorldArea(3139, 3428, 8, 4, 0);
    }

    // Varrock North Area Data
    public static class VarrockNorth {
        public static final WorldArea AREA = new WorldArea(3199, 3508, 45, 12, 0);
        public static final WorldArea WALK_AREA = new WorldArea(3218, 3511, 6, 4, 0);
    }


}
