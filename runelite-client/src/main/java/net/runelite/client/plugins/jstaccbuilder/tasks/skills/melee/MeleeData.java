package net.runelite.client.plugins.jstaccbuilder.tasks.skills.melee;

import net.runelite.api.coords.WorldPoint;

import java.security.SecureRandom;
import java.util.List;

public class MeleeData {

    public static final WorldPoint[] CHICKENS_EAST = {
            new WorldPoint(3231, 3287, 0),
            new WorldPoint(3236, 3287, 0),
            new WorldPoint(3236, 3290, 0),
            new WorldPoint(3237, 3291, 0),
            new WorldPoint(3237, 3292, 0),
            new WorldPoint(3236, 3293, 0),
            new WorldPoint(3236, 3298, 0),
            new WorldPoint(3237, 3298, 0),
            new WorldPoint(3237, 3299, 0),
            new WorldPoint(3235, 3301, 0),
            new WorldPoint(3226, 3301, 0),
            new WorldPoint(3225, 3300, 0),
            new WorldPoint(3225, 3295, 0),
            new WorldPoint(3231, 3295, 0)
    };
    public static final WorldPoint[] CHICKENS_NORTH = {
            new WorldPoint(3171, 3289, 0),
            new WorldPoint(3174, 3289, 0),
            new WorldPoint(3175, 3288, 0),
            new WorldPoint(3176, 3288, 0),
            new WorldPoint(3177, 3289, 0),
            new WorldPoint(3183, 3289, 0),
            new WorldPoint(3185, 3291, 0),
            new WorldPoint(3185, 3295, 0),
            new WorldPoint(3186, 3296, 0),
            new WorldPoint(3186, 3297, 0),
            new WorldPoint(3185, 3298, 0),
            new WorldPoint(3185, 3300, 0),
            new WorldPoint(3183, 3302, 0),
            new WorldPoint(3182, 3302, 0),
            new WorldPoint(3181, 3303, 0),
            new WorldPoint(3179, 3303, 0),
            new WorldPoint(3179, 3307, 0),
            new WorldPoint(3173, 3307, 0),
            new WorldPoint(3173, 3303, 0),
            new WorldPoint(3169, 3299, 0),
            new WorldPoint(3170, 3298, 0),
            new WorldPoint(3170, 3295, 0),
            new WorldPoint(3169, 3294, 0),
            new WorldPoint(3169, 3291, 0)
    };
    private final SecureRandom secureRandom = new SecureRandom();
    private final List<WorldPoint[]> chickenAreas = List.of(
            CHICKENS_EAST,
            CHICKENS_NORTH
            // Add more chicken areas here if needed
    );

    public WorldPoint[] getRandomChickenArea() {
        int randomIndex = secureRandom.nextInt(chickenAreas.size());
        return chickenAreas.get(randomIndex);
    }
}
