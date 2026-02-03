package kendo.me.kproffesionscore.entities.events;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.manager.config.CooldownsManager;
import kendo.me.kproffesionscore.utils.ChatUtils;
import kendo.me.kproffesionscore.utils.ConfigUtils;
import kendo.me.kproffesionscore.utils.skript.SkriptUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RedSeringeEvent implements Listener {
    //TODO: Javadocs this shit
    private final ConfigUtils configUtils = new ConfigUtils();
    private final String skillKey = "seringa-cura";
    private final CooldownsManager cooldownsManager = KProfessionsCore.getCooldownsManager();
    private final YamlConfiguration config = configUtils.getConfigFile("medico");

    public RedSeringeEvent(JavaPlugin plugin) {
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
        String targetName = ChatUtils.color(config.getString("skills." + skillKey + ".item-name", "Seringa vermelha"));

        ItemMeta meta = item.getItemMeta();
        if (item.getType() != targetMaterial || !meta.hasCustomModelData() ||
                meta.getCustomModelData() != modelData || !meta.getDisplayName().equals(targetName)) return;

        if (player.isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            double currentHp = SkriptUtils.getSkriptVariable(player, "hp");
            double maxHp = SkriptUtils.getSkriptVariable(player, "hpmax");

            if (currentHp >= maxHp) {
                player.sendMessage(ChatUtils.color("&cVocê já está com a vida cheia!"));
                event.setCancelled(true);
                return;
            }

            int cooldownTime = config.getInt("skills." + skillKey + ".cooldown");

            if (cooldownsManager.isCooldown(player.getUniqueId(), skillKey)) {
                double remaining = cooldownsManager.getRemaining(player.getUniqueId(), skillKey);
                String progressBar = cooldownsManager.getProgressBar(remaining, cooldownTime, 20, "|", "&c", "&7");
                String message = ChatUtils.color("&4&lCURA &7[" + progressBar + "&7] &e" + String.format("%.1f", remaining) + "s");

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                return;
            }

            double percentHeal = config.getDouble("skills." + skillKey + ".percent-heal", 0.2);
            int effectTime = config.getInt("skills." + skillKey + ".effect-time", 5);

            startSkriptRegenTask(player, percentHeal, effectTime);

            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 0.5f);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.color("&a&l✚ REGENERAÇÃO ATIVA!")));

            cooldownsManager.setCooldown(player.getUniqueId(), skillKey, cooldownTime);

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            event.setCancelled(true);
        }
    }

    private void startSkriptRegenTask(Player player, double totalPercent, int seconds) {
        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (!player.isOnline() || elapsed >= seconds) {
                    this.cancel();
                    return;
                }

                double maxHp = SkriptUtils.getSkriptVariable(player, "hpmax");
                double currentHp = SkriptUtils.getSkriptVariable(player, "hp");

                double totalHeal = maxHp * totalPercent;
                double healPerTick = totalHeal / seconds;

                double finalHp = Math.min(maxHp, Math.ceil(currentHp + healPerTick));

                SkriptUtils.setVariable(player, "hp", finalHp);

                player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0, 1.5, 0), 1);
                elapsed++;
            }
        }.runTaskTimer(KProfessionsCore.getInstance(), 0L, 20L);
    }
}