package kendo.me.kproffesionscore.entities.events.combatente;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.entities.CustomEntity;
import kendo.me.kproffesionscore.builder.entities.EntityBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SwordAttackTest implements Listener {

    public SwordAttackTest(KProfessionsCore plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAttack(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        spawnSlashAnimation(player);
    }

    private void spawnSlashAnimation(Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Location spawnLoc = eye.clone().add(dir.multiply(1.4));
        spawnLoc.setYaw(0F);
        spawnLoc.setPitch(0F);

        ItemStack slashItem = new ItemStack(Material.COAL);
        ItemMeta meta = slashItem.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(17);
            slashItem.setItemMeta(meta);
        }

        float initialYaw = (float) Math.toRadians(player.getLocation().getYaw());
        CustomEntity slash = new EntityBuilder((int)(Math.random() * 999999))
                .setLocation(spawnLoc)
                .setModel(slashItem)
                .setInvisible(true)
                .setSmall(false)
                .setMarker(true)
                .setModelOffset(1.6)
                .setHeadPose(0F, initialYaw, 0F)
                .build();

        slash.getViewers().add(player);
        KProfessionsCore.getEntityManager().addEntity(slash);
        slash.spawn(slash.getViewers());

        new BukkitRunnable() {
            int frame = 17;

            @Override
            public void run() {
                if (frame > 32 || !player.isOnline() || player.isDead()) {
                    slash.remove();
                    KProfessionsCore.getEntityManager().removeEntity(slash);
                    this.cancel();
                    return;
                }

                Location cEye = player.getEyeLocation();
                Vector cDir = cEye.getDirection().normalize();
                Location cLoc = cEye.clone().add(cDir.multiply(1.4)).subtract(0, 1.6, 0);
                cLoc.setYaw(0F);
                cLoc.setPitch(0F);
                slash.teleport(cLoc);
                float yawRadian = (float) Math.toRadians(player.getLocation().getYaw());
                slash.updateRotation(15, 0F, yawRadian, 0F);
                ItemStack frameItem = new ItemStack(Material.COAL);
                ItemMeta fMeta = frameItem.getItemMeta();
                if (fMeta != null) {
                    fMeta.setCustomModelData(frame);
                    frameItem.setItemMeta(fMeta);
                }
                slash.updateModel(frameItem);

                frame++;
            }
        }.runTaskTimer(KProfessionsCore.getInstance(), 0L, 1L);
    }
}