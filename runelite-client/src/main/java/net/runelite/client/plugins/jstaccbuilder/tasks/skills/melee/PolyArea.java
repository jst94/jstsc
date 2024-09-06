package net.runelite.client.plugins.jstaccbuilder.tasks.skills.melee;

import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.SceneEntity;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolyArea {
    private final WorldPoint[] definedPoints;
    private final SecureRandom secureRandom;
    private final WorldPoint[] areaPoints;

    public PolyArea(WorldPoint[] definedPoints) {
        this.definedPoints = definedPoints;
        areaPoints = createArea();
        secureRandom = new SecureRandom();
    }

    private WorldPoint[] createArea() {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        // Calculate bounding box of the polygon
        for (WorldPoint point : definedPoints) {
            if (point.getX() < minX) minX = point.getX();
            if (point.getY() < minY) minY = point.getY();
            if (point.getX() > maxX) maxX = point.getX();
            if (point.getY() > maxY) maxY = point.getY();
        }

        List<WorldPoint> pointsInsidePolygon = new ArrayList<>();
        // Iterate through every point in the bounding box
        for (int x = minX; x <= maxX + 1; x++) {
            for (int y = minY; y <= maxY + 1; y++) {
                // If the point is inside the polygon, add it to the list
                if (contains(x, y, 0)) {
                    pointsInsidePolygon.add(new WorldPoint(x, y, 0));
                }
            }
        }

        return pointsInsidePolygon.toArray(new WorldPoint[0]);
    }

    private boolean contains(int x, int y, int z) {
        boolean result = false;
        for (int i = 0, j = definedPoints.length - 1; i < definedPoints.length; j = i++) {
            if ((definedPoints[i].getY() > y) != (definedPoints[j].getY() > y) &&
                    (x < (definedPoints[j].getX() - definedPoints[i].getX()) *
                            (y - definedPoints[i].getY()) / (definedPoints[j].getY() - definedPoints[i].getY()) + definedPoints[i].getX())) {
                result = !result;
            }
            // Check if point is on the border
            if (onBorder(x, y, definedPoints[i], definedPoints[j])) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean onBorder(int x, int y, WorldPoint p1, WorldPoint p2) {
        // Check if the point is on a border line between two points p1 and p2
        double crossProduct = (y - p1.getY()) * (p2.getX() - p1.getX()) - (x - p1.getX()) * (p2.getY() - p1.getY());
        if (Math.abs(crossProduct) > 0.000001) return false;

        double dotProduct = (x - p1.getX()) * (p2.getX() - p1.getX()) + (y - p1.getY()) * (p2.getY() - p1.getY());
        if (dotProduct < 0) return false;

        double squaredLength = (p2.getX() - p1.getX()) * (p2.getX() - p1.getX()) + (p2.getY() - p1.getY()) * (p2.getY() - p1.getY());
        return dotProduct <= squaredLength;
    }


    public WorldPoint[] getAreaPoints() {
        return areaPoints;
    }

    public boolean contains(SceneEntity entity) {
        return contains(entity.getWorldLocation());
    }

    public boolean contains(WorldPoint point) {
        return Arrays.asList(areaPoints).contains(point);
    }

    public WorldPoint getRandomWorldPoint() {
        return areaPoints[secureRandom.nextInt(areaPoints.length)];
    }
}
