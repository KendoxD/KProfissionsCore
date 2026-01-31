package kendo.me.kproffesionscore.builder.entities;

import kendo.me.kproffesionscore.entities.projectil.CustomProjectil;
import org.bukkit.util.Vector;
import java.lang.reflect.Constructor;

public class ProjectileBuilder {
    private CustomEntity entity;
    private Vector velocity;
    private double gravity = 0.02;
    private double drag = 0.99;
    private int maxTicks = 100;

    public ProjectileBuilder(CustomEntity entity) {
        this.entity = entity;
    }

    public ProjectileBuilder setVelocity(Vector velocity) {
        this.velocity = velocity;
        return this;
    }

    public ProjectileBuilder setGravity(double gravity) {
        this.gravity = gravity;
        return this;
    }

    public ProjectileBuilder setMaxTicks(int ticks) {
        this.maxTicks = ticks;
        return this;
    }

    public <T extends CustomProjectil> T launch(Class<T> projectileClass) {
        try {
            Constructor<T> constructor = projectileClass.getConstructor(ProjectileBuilder.class);
            return constructor.newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CustomEntity getEntity() { return entity; }
    public Vector getVelocity() { return velocity; }
    public double getGravity() { return gravity; }
    public double getDrag() { return drag; }
    public int getMaxTicks() { return maxTicks; }
}