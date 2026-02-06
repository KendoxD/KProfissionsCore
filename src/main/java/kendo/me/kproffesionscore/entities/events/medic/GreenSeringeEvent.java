package kendo.me.kproffesionscore.entities.events.medic;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.entities.CustomEntity;
import kendo.me.kproffesionscore.builder.entities.EntityBuilder;
import kendo.me.kproffesionscore.builder.entities.ProjectileBuilder;
import kendo.me.kproffesionscore.entities.GreenSeringeProjectil;
import kendo.me.kproffesionscore.utils.ChatUtils;
import kendo.me.kproffesionscore.utils.ConfigUtils;
import kendo.me.kproffesionscore.manager.config.CooldownsManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Random;

public class GreenSeringeEvent implements Listener {
    //TODO: Javadocs this shit
    private final ConfigUtils configUtils = new ConfigUtils();
    private final String skillKey = "seringa-verde";
    private final CooldownsManager cooldownsManager = KProfessionsCore.getCooldownsManager();
    private final YamlConfiguration config = configUtils.getConfigFile("medico");

    public GreenSeringeEvent(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        Player player = event.getPlayer();

        String materialName = config.getString("skills." + skillKey + ".item", "PAPER").replace("minecraft:", "").toUpperCase();
        Material targetMaterial = Material.getMaterial(materialName);
        int modelData = config.getInt("skills." + skillKey + ".model-data");

        String targetName = ChatUtils.color(config.getString("skills." + skillKey + ".item-name"));

        ItemMeta meta = item.getItemMeta();

        if (item.getType() != targetMaterial) return;
        if (!meta.hasCustomModelData() || meta.getCustomModelData() != modelData) return;

        if (!meta.getDisplayName().equals(targetName)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            int cooldownTime = config.getInt("skills." + skillKey + ".cooldown", 10);

            if (cooldownsManager.isCooldown(player.getUniqueId(), skillKey)) {
                double remaining = cooldownsManager.getRemaining(player.getUniqueId(), skillKey);
                String progressBar = cooldownsManager.getProgressBar(remaining, cooldownTime, 20, "|", "&a", "&7");
                String message = ChatUtils.color("&6&lSERINGA &7[" + progressBar + "&7] &e" + String.format("%.1f", remaining) + "s");

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                event.setCancelled(true);
                return;
            }

            disparar(player, item);
            cooldownsManager.setCooldown(player.getUniqueId(), skillKey, cooldownTime);

            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.color("&a&l✔ SERINGA LANÇADA!")));

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            event.setCancelled(true);
        }
    }

    private void disparar(Player player, ItemStack item) {
        Vector direction = player.getEyeLocation().getDirection();
        Location spawnLoc = player.getEyeLocation().clone().add(direction.clone().multiply(0.5));

        ItemStack visualItem = item.clone();
        visualItem.setAmount(1);

        CustomEntity entity = new EntityBuilder(new Random().nextInt(1000000))
                .setLocation(spawnLoc)
                .setModel(visualItem)
                .setHeadPose(0F, 0F, 0F)
                .setModelOffset(0.9)
                .setSmall(true)
                .setInvisible(true)
                .setHitBox(1, 1.7)
                .build();

        new ProjectileBuilder(entity)
                .setVelocity(direction.multiply(1.3))
                .setGravity(0.010)
                .launch(GreenSeringeProjectil.class);
    }
}