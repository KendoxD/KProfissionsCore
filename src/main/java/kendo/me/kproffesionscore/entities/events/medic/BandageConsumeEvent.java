package kendo.me.kproffesionscore.entities.events.medic;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.manager.config.CooldownsManager;
import kendo.me.kproffesionscore.professions.Medico;
import kendo.me.kproffesionscore.professions.database.connection.dao.MedicoDao;
import kendo.me.kproffesionscore.utils.ChatUtils;
import kendo.me.kproffesionscore.utils.ConfigUtils;
import kendo.me.kproffesionscore.utils.skript.SkriptUtils;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BandageConsumeEvent implements Listener {

    private final ConfigUtils configUtils = new ConfigUtils();
    private final String skillKey = "bandagem";
    private final CooldownsManager cooldownsManager = KProfessionsCore.getCooldownsManager();
    private final Set<UUID> usingBandage = new HashSet<>();
    private final MedicoDao medicoDao = new MedicoDao(KProfessionsCore.getDatabase().getConnection());

    public BandageConsumeEvent(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;

        YamlConfiguration config = KProfessionsCore.getConfigManager().getProfessionConfig("medico.yml");
        if (config == null) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        Player player = event.getPlayer();
        String materialName = config.getString("skills." + skillKey + ".item", "PAPER").replace("minecraft:", "").toUpperCase();
        Material targetMaterial = Material.getMaterial(materialName);
        int targetModel = config.getInt("skills." + skillKey + ".model-data");
        String targetName = ChatUtils.color(config.getString("skills." + skillKey + ".item-name", "Bandagem"));

        ItemMeta meta = item.getItemMeta();
        if (item.getType() != targetMaterial || !meta.hasCustomModelData() ||
                meta.getCustomModelData() != targetModel || !meta.getDisplayName().equals(targetName)) return;

        if (!player.isSneaking() || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) return;

        int requiredLevel = config.getInt("skills." + skillKey + ".level-required", 1);
        Medico medicoData = medicoDao.load(player.getName());
        int playerLevel = (medicoData != null) ? medicoData.getProfissionLevel() : 0;

        if (playerLevel < requiredLevel) {
            player.sendMessage(ChatUtils.color("&c&l(!) &7Seu nível de Médico (&e" + playerLevel + "&7) é insuficiente. Requer nível &6" + requiredLevel));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (usingBandage.contains(player.getUniqueId())) return;

        double currentHp = SkriptUtils.getSkriptVariable(player, "hp");
        double maxHp = SkriptUtils.getSkriptVariable(player, "hpmax");

        if (currentHp >= (maxHp * 0.75)) {
            player.sendMessage(ChatUtils.color("&cVocê só pode usar bandagens se tiver menos de 75% da vida!"));
            return;
        }

        if (cooldownsManager.isCooldown(player.getUniqueId(), skillKey)) {
            double remaining = cooldownsManager.getRemaining(player.getUniqueId(), skillKey);
            player.sendMessage(ChatUtils.color("&cBandagem em recarga: &f" + String.format("%.1f", remaining) + "s"));
            return;
        }

        useBandage(player, item, config, playerLevel, requiredLevel);
        event.setCancelled(true);
    }

    private void useBandage(Player player, ItemStack item, YamlConfiguration config, int playerLevel, int requiredLevel) {
        int usageTime = config.getInt("skills." + skillKey + ".usage-time", 3);
        int cooldownTime = config.getInt("skills." + skillKey + ".cooldown", 0);
        double percentHeal = config.getDouble("skills." + skillKey + ".percent-heal", 0.15);
        double baseExp = config.getDouble("skills." + skillKey + ".exp-gain", 2.0);

        final UUID uuid = player.getUniqueId();
        final ItemStack itemUsed = item.clone();

        usingBandage.add(uuid);

        new BukkitRunnable() {
            int ticks = 0;
            final int totalTicks = usageTime * 20;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    usingBandage.remove(uuid);
                    this.cancel();
                    return;
                }

                if (!player.isSneaking()) {
                    player.sendTitle(ChatUtils.color("&c&lCancelando.."), ChatUtils.color("&7Mantenha o SHIFT pressionado!"), 0, 20, 10);
                    usingBandage.remove(uuid);
                    this.cancel();
                    return;
                }

                if (!player.getInventory().getItemInMainHand().isSimilar(itemUsed)) {
                    player.sendTitle(ChatUtils.color("&c&lCancelando.."), ChatUtils.color("&7Você trocou de item!"), 0, 20, 10);
                    usingBandage.remove(uuid);
                    this.cancel();
                    return;
                }

                double angle = ticks * 0.15;
                double x = Math.cos(angle) * 0.7;
                double z = Math.sin(angle) * 0.7;
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(x, 0.5 + (ticks * 0.01), z), 1, 0, 0, 0, 0);

                if (ticks % 10 == 0) {
                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.8f, 1.2f);
                }

                String progressBar = cooldownsManager.getProgressBar(totalTicks - ticks, totalTicks, 15, "|", "&a", "&7");
                player.sendTitle(ChatUtils.color("&f&lEnfaixando..."), ChatUtils.color("&7Segure SHIFT [" + progressBar + "&7]"), 0, 7, 0);

                if (ticks >= totalTicks) {
                    double currentHp = SkriptUtils.getSkriptVariable(player, "hp");
                    double maxHp = SkriptUtils.getSkriptVariable(player, "hpmax");

                    double finalHp = Math.min(currentHp + (maxHp * percentHeal), maxHp * 0.75);
                    SkriptUtils.setVariable(player, "hp", finalHp);

                    double finalExp = configUtils.calculateDynamicExp(baseExp, playerLevel, requiredLevel);

                    Medico medico = medicoDao.load(player.getName());
                    if (medico != null) {
                        medico.addExp(finalExp);
                        medicoDao.save(medico);
                        player.sendMessage(ChatUtils.color("&a+ " + String.format("%.1f", finalExp) + " XP de Médico!"));
                    }

                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }

                    player.sendTitle(ChatUtils.color("&a&l✚ Bandagem Aplicada!"), "", 5, 40, 10);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                    cooldownsManager.setCooldown(uuid, skillKey, cooldownTime);
                    usingBandage.remove(uuid);
                    this.cancel();
                }
                ticks += 2;
            }
        }.runTaskTimer(KProfessionsCore.getInstance(), 0L, 2L);
    }
}