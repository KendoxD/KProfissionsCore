package kendo.me.kproffesionscore.entities.projectil;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.entities.CustomEntity;
import kendo.me.kproffesionscore.builder.entities.ProjectileBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CustomProjectil extends BukkitRunnable {
    //TODO: Javadocs this shit
    protected final CustomEntity entity;
    protected final Vector velocity;
    protected final double gravity;
    protected final double drag;
    protected int ticksLeft;

    public CustomProjectil(ProjectileBuilder builder) {
        this.entity = builder.getEntity();
        this.velocity = builder.getVelocity();
        this.gravity = builder.getGravity();
        this.drag = builder.getDrag();
        this.ticksLeft = builder.getMaxTicks();

        Location start = entity.getEntityLocation();
        List<Player> nearby = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(start.getWorld()))
                .filter(p -> p.getLocation().distanceSquared(start) <= 10000)
                .collect(Collectors.toList());

        this.entity.spawn(nearby);
        KProfessionsCore.getEntityManager().addEntity(entity);
        this.runTaskTimer(KProfessionsCore.getInstance(), 0L, 1L);
    }

    @Override
    public void run() {
        if (ticksLeft-- <= 0) { onExpire(); destroy(); return; }

        Location loc = entity.getEntityLocation().clone();
        velocity.setY(velocity.getY() - gravity);
        velocity.multiply(drag);
        loc.add(velocity);

        if (velocity.lengthSquared() > 0) loc.setDirection(velocity);

        entity.teleport(loc);
        entity.drawHitbox();
        onTick();
            for (Player viewer : entity.getViewers()) {
                loc.setPitch(0);
                float yaw = (float) Math.toDegrees(Math.atan2(-velocity.getX(), velocity.getZ()));

                loc.setYaw(yaw);
                boolean hitBody = entity.isInside(viewer.getLocation());
                boolean hitChest = entity.isInside(viewer.getLocation().add(0,0.9,0));
                boolean hitHead = entity.isInside(viewer.getEyeLocation());
                if (!viewer.getWorld().equals(loc.getWorld())) continue;
                if (viewer.getLocation().distanceSquared(loc) > 16) continue;
                if(viewer.isOnline()) {
                    if (hitBody || hitChest || hitHead) {
                        onHitPlayer(viewer);
                        destroy();
                        return;
                    }
            }
        }

        if (loc.getBlock().getType().isSolid()) { onHitBlock(loc); destroy(); }
    }

    public abstract void onHitPlayer(Player p);
    public void onHitBlock(Location l) {}
    public void onTick() {}
    public void onExpire() {}

    protected void destroy() {
        this.cancel();
        KProfessionsCore.getEntityManager().removeEntity(entity);
        entity.remove();
    }
}