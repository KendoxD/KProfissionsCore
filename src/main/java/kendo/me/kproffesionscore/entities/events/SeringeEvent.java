package kendo.me.kproffesionscore.entities.events;

import kendo.me.kproffesionscore.builder.entities.CustomEntity;
import kendo.me.kproffesionscore.builder.entities.EntityBuilder;
import kendo.me.kproffesionscore.builder.entities.ProjectileBuilder;
import kendo.me.kproffesionscore.builder.item.ItemBuilder;
import kendo.me.kproffesionscore.entities.SeringaProjectil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Random;
//TODO: Clean this bitch
public class SeringeEvent implements Listener {
    public SeringeEvent(JavaPlugin plugin){
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        ItemBuilder itemBuilder = new ItemBuilder(Material.PAPER)
                .setModelData(2002);
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (item != null && item.isSimilar(itemBuilder.build())) {

                disparar(player, item);

                item.setAmount(item.getAmount() - 1);
            }
        }
    }
    private void disparar(Player player, ItemStack item) {
        Vector direction = player.getEyeLocation().getDirection();
        Location spawnLoc = player.getEyeLocation().clone();

        spawnLoc.add(direction.clone().multiply(0.5));

        CustomEntity entity = new EntityBuilder(new Random().nextInt(1000000))
                .setLocation(spawnLoc)
                .setModel(item.clone())
                .setHeadPose(0F, 0F, 0F)
                .setModelOffset(0.9)
                .setSmall(true)
                .setInvisible(true)
                .setHitBox(1, 1.7)
                .build();

        new ProjectileBuilder(entity)
                .setVelocity(direction.multiply(1.3))
                .setGravity(0.010)
                .launch(SeringaProjectil.class);
    }
}
