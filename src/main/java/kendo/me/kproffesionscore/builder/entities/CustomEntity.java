package kendo.me.kproffesionscore.builder.entities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.utils.CustomHitBox;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomEntity {
    private final EntityBuilder settings;
    private Location currentLocation;
    private final int entityId;
    private final UUID uuid;
    private boolean debugMode;
    private final List<Player> viewers = new ArrayList<>();
    private final CustomHitBox hitBox;

    public CustomEntity(@NotNull EntityBuilder builder, boolean debugMode) {
        this.settings = builder;
        this.currentLocation = builder.getLocation();
        this.entityId = builder.getEntityId();
        this.uuid = builder.getUuid();
        this.debugMode = debugMode;
        this.hitBox = new CustomHitBox(builder.getHitBoxWidth(), builder.getHitBoxHeight(), builder.isSmall());
    }

    public List<Player> getViewers() {
        return viewers;
    }

    public void spawn(List<Player> players) {
        this.viewers.addAll(players);
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();

        PacketContainer spawn = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        spawn.getIntegers().write(0, entityId);
        spawn.getUUIDs().write(0, uuid);
        spawn.getIntegers().write(1, 1);
        spawn.getDoubles().write(0, currentLocation.getX()).write(1, currentLocation.getY()).write(2, currentLocation.getZ());

        PacketContainer meta = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        meta.getIntegers().write(0, entityId);
        WrappedDataWatcher watcher = new WrappedDataWatcher();

        byte status = (byte) (settings.isInvisible() ? 0x20 : 0);
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), status);

        byte flags = 0;
        if (settings.isSmall()) flags |= 0x01;
        if (settings.isNoBasePlate()) flags |= (byte) 0x08;
        if (settings.isMarker()) flags |= (byte) 0x10;
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(14, WrappedDataWatcher.Registry.get(Byte.class)), flags);
        WrappedDataWatcher.WrappedDataWatcherObject headPoseObject = new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.getVectorSerializer());
        watcher.setObject(headPoseObject, settings.getHeadPose());

        meta.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        sendPacket(players, spawn, meta);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (viewers.isEmpty()) return;
                PacketContainer equip = pm.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
                equip.getIntegers().write(0, entityId);
                List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairs = new ArrayList<>();
                pairs.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, settings.getModel()));
                equip.getSlotStackPairLists().write(0, pairs);
                sendPacket(viewers, equip);
            }
        }.runTaskLater(KProfessionsCore.getInstance(), 2L);
    }

    public void teleport(@NotNull Location loc) {
        this.currentLocation = loc;
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer tele = pm.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        tele.getIntegers().write(0, entityId);
        tele.getDoubles().write(0, loc.getX()).write(1, loc.getY()).write(2, loc.getZ());
        tele.getBytes().write(0, (byte) (loc.getYaw() * 256.0F / 360.0F));
        tele.getBytes().write(1, (byte) (loc.getPitch() * 256.0F / 360.0F));
        tele.getBooleans().write(0, false);
        PacketContainer head = pm.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        head.getIntegers().write(0, entityId);
        head.getBytes().write(0, (byte) (loc.getYaw() * 256.0F / 360.0F));
        sendPacket(this.viewers, tele, head);
    }

    public void remove() {
        PacketContainer destroy = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroy.getIntegerArrays().write(0, new int[]{entityId});
        sendPacket(this.viewers, destroy);
        this.viewers.clear();
    }

    private void sendPacket(@NotNull List<Player> players, PacketContainer... packets) {
        for (Player p : players) {
            if (!p.isOnline()) continue;
            try {
                for (PacketContainer packet : packets) ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
            } catch (Exception ignored) {}
        }
    }
    public void spawnParticle(org.bukkit.Particle particle, int count, double offset) {
        if (viewers.isEmpty()) return;

        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getNewParticles().write(0, WrappedParticle.create(particle, null));
        packet.getDoubles()
                .write(0, currentLocation.getX())
                .write(1, currentLocation.getY() + 0.7)
                .write(2, currentLocation.getZ());
        packet.getFloat().write(0, (float) offset); // X
        packet.getFloat().write(1, (float) offset); // Y
        packet.getFloat().write(2, (float) offset); // Z o
        packet.getFloat().write(3, 0F);
        packet.getIntegers().write(0, count);
        packet.getBooleans().write(0, false);

        sendPacket(this.viewers, packet);
    }

    public void updateRotation(int index, float x, float y, float z) {
        if (viewers.isEmpty()) return;

        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        PacketContainer metaPacket = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, entityId);

        Vector3F angle = new Vector3F(x, y, z);

        WrappedDataWatcher watcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.getVectorSerializer();

        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(index, serializer), angle);

        metaPacket.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        sendPacket(this.viewers, metaPacket);
    }

    public void updateModel(ItemStack item) {
        this.settings.setModel(item);
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, this.entityId);
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairList = new ArrayList<>();
        pairList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, item));
        packet.getSlotStackPairLists().write(0, pairList);
        for (org.bukkit.entity.Player viewer : this.viewers) {
            if (viewer.isOnline()) {
                try {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(viewer, packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void drawHitbox() {
        if (debugMode) {
        hitBox.draw(currentLocation, getViewers(), false);
    } }
    public boolean isInside(Location pt) { return hitBox.isInside(currentLocation, pt, false); }
    public Location getEntityLocation() { return currentLocation; }
}