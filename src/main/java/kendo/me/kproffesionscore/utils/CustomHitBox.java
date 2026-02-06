package kendo.me.kproffesionscore.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import java.util.List;

public class CustomHitBox {
    private double width, height;
    private boolean isSmall;

    public CustomHitBox(double width, double height, boolean isSmall) {
        this.width = width;
        this.height = height;
        this.isSmall = isSmall;
    }

    private double getVerticalOffset(boolean followHead) {
        return followHead ? (isSmall ? 0.7 : 1.4) : 0;
    }

    public boolean isInside(Location entityLoc, Location point, boolean followHead) {
        double hw = width / 2;
        double offset = getVerticalOffset(followHead);

        double minX = entityLoc.getX() - hw;
        double maxX = entityLoc.getX() + hw;
        double minY = (entityLoc.getY() + offset) - (followHead ? height / 2 : 0.1);
        double maxY = minY + height;
        double minZ = entityLoc.getZ() - hw;
        double maxZ = entityLoc.getZ() + hw;

        return (point.getX() >= minX && point.getX() <= maxX) &&
                (point.getY() >= minY && point.getY() <= maxY) &&
                (point.getZ() >= minZ && point.getZ() <= maxZ);
    }

    public void draw(Location entityLoc, List<Player> viewers, boolean followHead) {
        if (viewers == null || viewers.isEmpty()) {
            System.out.println(viewers);
            System.out.println("vazio?");
            return;
        }
        double hw = width / 2;
        double offset = getVerticalOffset(followHead);

        double minX = entityLoc.getX() - hw;
        double maxX = entityLoc.getX() + hw;
        double minY = (entityLoc.getY() + offset) - (followHead ? height / 2 : 0);
        double maxY = minY + height;
        double minZ = entityLoc.getZ() - hw;
        double maxZ = entityLoc.getZ() + hw;

        for (double x = minX; x <= maxX; x += 0.2) {
            spawnParticle(viewers, x, minY, minZ); spawnParticle(viewers, x, maxY, minZ);
            spawnParticle(viewers, x, minY, maxZ); spawnParticle(viewers, x, maxY, maxZ);
        }
        for (double y = minY; y <= maxY; y += 0.2) {
            spawnParticle(viewers, minX, y, minZ); spawnParticle(viewers, maxX, y, minZ);
            spawnParticle(viewers, minX, y, maxZ); spawnParticle(viewers, maxX, y, maxZ);
        }
        for (double z = minZ; z <= maxZ; z += 0.2) {
            spawnParticle(viewers, minX, minY, z); spawnParticle(viewers, maxX, minY, z);
            spawnParticle(viewers, minX, maxY, z); spawnParticle(viewers, maxX, maxY, z);
        }
    }

    private void spawnParticle(List<Player> viewers, double x, double y, double z) {
        for (Player p : viewers) {
            if (p.isOnline()) {
                p.spawnParticle(Particle.FLAME, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }
}