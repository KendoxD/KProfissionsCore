package kendo.me.kproffesionscore.builder.entities;

import com.comphenix.protocol.wrappers.Vector3F;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.List;
import java.util.UUID;

public class EntityBuilder {
    private final int entityId;
    private List<Player> playersToShow;
    private UUID uuid = UUID.randomUUID();
    private ItemStack model;
    private Location location;

    private Vector3F headPose = new Vector3F(0, 0, 0);
    private double yOffset = 0.0;

    private double hitBoxWidth;
    private double hitBoxHeight;
    private boolean invisible = true;
    private boolean small = false;
    private boolean marker = true;
    private boolean noBasePlate = true; // Adicionado
    private boolean debugMode;

    public EntityBuilder(int entityId){
        this.entityId = entityId;
    }

    public EntityBuilder setHeadPose(float x, float y, float z) {
        this.headPose = new Vector3F(x, y, z);
        return this;
    }

    // Define o quanto a entidade deve descer em relação ao spawn real
    public EntityBuilder setModelOffset(double yOffset) {
        this.yOffset = yOffset;
        return this;
    }

    public EntityBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    public EntityBuilder setModel(ItemStack model) {
        this.model = model;
        return this;
    }

    public EntityBuilder setSmall(boolean small) {
        this.small = small;
        return this;
    }

    public EntityBuilder setMarker(boolean marker) {
        this.marker = marker;
        return this;
    }

    public EntityBuilder setNoBasePlate(boolean noBasePlate) { // Setter adicionado
        this.noBasePlate = noBasePlate;
        return this;
    }

    public EntityBuilder setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    public EntityBuilder setInvisible(boolean invisible) {
        this.invisible = invisible;
        return this;
    }

    public EntityBuilder setHitBox(double width, double height) {
        this.hitBoxWidth = width;
        this.hitBoxHeight = height;
        return this;
    }

    // Getters
    public Vector3F getHeadPose() { return headPose; }
    public double getYOffset() { return yOffset; }
    public int getEntityId() { return entityId; }
    public List<Player> getPlayersToShow() { return playersToShow; }
    public UUID getUuid() { return uuid; }
    public ItemStack getModel() { return model; }
    public Location getLocation() { return location; }
    public double getHitBoxWidth() { return hitBoxWidth; }
    public double getHitBoxHeight() { return hitBoxHeight; }
    public boolean isInvisible() { return invisible; }
    public boolean isSmall() { return small; }
    public boolean isMarker() { return marker; }
    public boolean isNoBasePlate() { return noBasePlate; } // Getter adicionado

    public CustomEntity build() {
        if (location == null) throw new IllegalStateException("Location não definida!");
        if (model == null) throw new IllegalStateException("Model não definido!");

        // Aplica o offset customizado antes de criar a entidade
        this.location.subtract(0, yOffset, 0);

        CustomEntity customEntity = new CustomEntity(this, this.debugMode);
        if (playersToShow != null && !playersToShow.isEmpty()) {
            customEntity.spawn(playersToShow);
        }
        return customEntity;
    }
}