package kendo.me.kproffesionscore.entities.events.medic;
import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.manager.config.CooldownsManager;
import kendo.me.kproffesionscore.utils.ChatUtils;
import kendo.me.kproffesionscore.utils.ConfigUtils;
import kendo.me.kproffesionscore.utils.skript.SkriptUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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

public class MedicKitEvent implements Listener {
    //TODO: Javadocs this shit
    private final ConfigUtils configUtils = new ConfigUtils();
    private final String skillKey = "medickit";
    private final CooldownsManager cooldownsManager = KProfessionsCore.getCooldownsManager();
    private final YamlConfiguration config = configUtils.getConfigFile("medico");

    public MedicKitEvent(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        Player player = event.getPlayer();

        String materialName = config.getString("skills." + skillKey + ".item", "COAL").replace("minecraft:", "").toUpperCase();
        Material targetMaterial = Material.getMaterial(materialName);
        int targetModel = config.getInt("skills." + skillKey + ".model-data");
        String targetName = ChatUtils.color(config.getString("skills." + skillKey + ".item-name", "Medickit"));

        ItemMeta meta = item.getItemMeta();
        if (item.getType() != targetMaterial || !meta.hasCustomModelData() ||
                meta.getCustomModelData() != targetModel || !meta.getDisplayName().equals(targetName)) return;

        if (!player.isSneaking() || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) return;

        double currentHp = SkriptUtils.getSkriptVariable(player, "hp");
        double maxHp = SkriptUtils.getSkriptVariable(player, "hpmax");

        if (currentHp >= maxHp) {
            player.sendMessage(ChatUtils.color("&cVocê já está com a vida cheia!"));
            return;
        }

        if (cooldownsManager.isCooldown(player.getUniqueId(), skillKey)) {
            double remaining = cooldownsManager.getRemaining(player.getUniqueId(), skillKey);
            player.sendMessage(ChatUtils.color("&cMedicKit em recarga: &f" + String.format("%.1f", remaining) + "s"));
            return;
        }

        useMedicKit(player, item);
        event.setCancelled(true);
    }

    private void useMedicKit(Player player, ItemStack item) {
        int usageTime = config.getInt("skills." + skillKey + ".usage-time", 15);
        int cooldownTime = config.getInt("skills." + skillKey + ".cooldown", 10);

        final Location startLoc = player.getLocation();
        final ItemStack itemUsed = item.clone();

        new BukkitRunnable() {
            int ticks = 0;
            final int totalTicks = usageTime * 20;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    this.cancel();
                    return;
                }
                if (!player.isSneaking()) {
                    player.sendTitle(ChatUtils.color("&c&lCancelando.."), ChatUtils.color("&7Mantenha o SHIFT pressionado!"), 0, 20, 10);
                    this.cancel();
                    return;
                }

                if (!player.getInventory().getItemInMainHand().isSimilar(itemUsed)) {
                    player.sendTitle(ChatUtils.color("&c&lCancelando.."), ChatUtils.color("&7Você trocou de item!"), 0, 20, 10);
                    this.cancel();
                    return;
                }

//                if (player.getLocation().distanceSquared(startLoc) > 0.25) {
//                    player.sendTitle(ChatUtils.color("&c&lCancelando,,"), ChatUtils.color("&7Não se mova!"), 0, 20, 10);
//                    this.cancel();
//                    return;
//                }

                double angle = ticks * 0.15;
                double x = Math.cos(angle) * 0.8;
                double z = Math.sin(angle) * 0.8;
                player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(x, ticks * 0.005, z), 2, 0, 0, 0, 0.05);

                if (ticks % 20 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_WOOL_PLACE, 1.0f, 0.8f);
                }

                String progressBar = cooldownsManager.getProgressBar(totalTicks - ticks, totalTicks, 20, "|", "&a", "&7");
                player.sendTitle(ChatUtils.color("&e&lCurando..."), ChatUtils.color("&7Segure SHIFT [" + progressBar + "&7]"), 0, 7, 0);

                if (ticks >= totalTicks) {
                    double maxHp = SkriptUtils.getSkriptVariable(player, "hpmax");
                    SkriptUtils.setVariable(player, "hp", maxHp);

                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }

                    player.sendTitle(ChatUtils.color("&a&l✚ Curado!"), "", 5, 40, 10);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
                    cooldownsManager.setCooldown(player.getUniqueId(), skillKey, cooldownTime);
                    this.cancel();
                }
                ticks += 2;
            }
        }.runTaskTimer(KProfessionsCore.getInstance(), 0L, 2L);
    }
}