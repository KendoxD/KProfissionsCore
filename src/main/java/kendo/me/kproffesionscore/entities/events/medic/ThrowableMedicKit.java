package kendo.me.kproffesionscore.entities.events.medic;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.entities.CustomEntity;
import kendo.me.kproffesionscore.builder.entities.EntityBuilder;
import kendo.me.kproffesionscore.manager.config.CooldownsManager;
import kendo.me.kproffesionscore.professions.Medico;
import kendo.me.kproffesionscore.professions.database.connection.dao.MedicoDao;
import kendo.me.kproffesionscore.utils.ChatUtils;
import kendo.me.kproffesionscore.utils.ConfigUtils;
import kendo.me.kproffesionscore.utils.skript.SkriptUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ThrowableMedicKit implements Listener {

    private final ConfigUtils configUtils = new ConfigUtils();
    private final String skillKey = "medickit-2";
    private final CooldownsManager cooldownsManager = KProfessionsCore.getCooldownsManager();
    private final MedicoDao medicoDao = new MedicoDao(KProfessionsCore.getDatabase().getConnection());

    public ThrowableMedicKit(JavaPlugin plugin) {
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

        String materialName = config.getString("skills." + skillKey + ".item", "COAL").replace("minecraft:", "").toUpperCase();
        Material targetMaterial = Material.getMaterial(materialName);
        int targetModel = config.getInt("skills." + skillKey + ".model-data");
        String targetName = ChatUtils.color(config.getString("skills." + skillKey + ".item-name"));

        ItemMeta meta = item.getItemMeta();
        if (item.getType() != targetMaterial || !meta.hasCustomModelData() ||
                meta.getCustomModelData() != targetModel || !meta.getDisplayName().equals(targetName)) return;

        if (player.isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

            int requiredLevel = config.getInt("skills." + skillKey + ".level-required", 15);
            Medico medicoData = medicoDao.load(player.getName());
            int playerLevel = (medicoData != null) ? medicoData.getProfissionLevel() : 0;

            if (playerLevel < requiredLevel) {
                player.sendMessage(ChatUtils.color("&c&l(!) &7Seu nível de Médico (&e" + playerLevel + "&7) é insuficiente. Requer nível &6" + requiredLevel));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                event.setCancelled(true);
                return;
            }

            int cooldownTime = config.getInt("skills." + skillKey + ".cooldown", 40);

            if (cooldownsManager.isCooldown(player.getUniqueId(), skillKey)) {
                double remaining = cooldownsManager.getRemaining(player.getUniqueId(), skillKey);
                String progressBar = cooldownsManager.getProgressBar(remaining, cooldownTime, 20, "|", "&a", "&7");
                String message = ChatUtils.color("&2&lCooldown &7[" + progressBar + "&7] &e" + String.format("%.1f", remaining) + "s");

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                event.setCancelled(true);
                return;
            }

            deployHealingStation(player, item, config);

            if (medicoData != null) {
                double baseExp = config.getDouble("skills." + skillKey + ".exp-gain", 25.0);
                double finalExp = configUtils.calculateDynamicExp(baseExp, playerLevel, requiredLevel);

                medicoData.addExp(finalExp);
                medicoDao.save(medicoData);
                player.sendMessage(ChatUtils.color("&a+ " + String.format("%.1f", finalExp) + " XP de Médico (Estação de Cura)!"));
            }

            cooldownsManager.setCooldown(player.getUniqueId(), skillKey, cooldownTime);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.color("&a&l✔ ESTAÇÃO LANÇADA!")));

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            event.setCancelled(true);
        }
    }

    private void deployHealingStation(Player player, ItemStack item, YamlConfiguration config) {
        Location dropLoc = player.getLocation().add(player.getLocation().getDirection().multiply(1.5));
        dropLoc.setY(dropLoc.getY() + 0.2);

        ItemStack entityItem = item.clone();
        ItemMeta meta = entityItem.getItemMeta();
        int entityModel = config.getInt("skills." + skillKey + ".entity-model-data");
        if (meta != null) {
            meta.setCustomModelData(entityModel);
            entityItem.setItemMeta(meta);
        }

        EntityBuilder builder = new EntityBuilder(new Random().nextInt(1000000))
                .setLocation(dropLoc)
                .setModel(entityItem)
                .setSmall(false)
                .setInvisible(true);

        CustomEntity stationEntity = new CustomEntity(builder, false);
        List<Player> nearby = player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(dropLoc) < 2500)
                .collect(Collectors.toList());

        stationEntity.spawn(nearby);
        KProfessionsCore.getEntityManager().addEntity(stationEntity);

        int durationSeconds = config.getInt("skills." + skillKey + ".effect-time", 10);
        double range = config.getDouble("skills." + skillKey + ".range", 5.0);
        double healPercent = config.getDouble("skills." + skillKey + ".heal-percent", 0.1);

        new BukkitRunnable() {
            int ticks = 0;
            final int totalTicks = durationSeconds * 20;
            float yaw = 0;

            @Override
            public void run() {
                if (ticks >= totalTicks) {
                    stationEntity.remove();
                    KProfessionsCore.getEntityManager().removeEntity(stationEntity);
                    this.cancel();
                    return;
                }

                yaw += 15f;
                Location rotLoc = dropLoc.clone();
                rotLoc.setYaw(yaw);
                stationEntity.teleport(rotLoc);

                if (ticks % 4 == 0) {
                    for (double i = 0; i < 360; i += 30) {
                        double rad = Math.toRadians(i);
                        double x = Math.cos(rad) * range;
                        double z = Math.sin(rad) * range;
                        dropLoc.getWorld().spawnParticle(Particle.COMPOSTER, dropLoc.clone().add(x, 0.1, z), 1, 0, 0, 0, 0);
                    }
                }

                if (ticks % 10 == 0) {
                    stationEntity.spawnParticle(Particle.HEART, 1, 0.3);
                }

                if (ticks % 20 == 0) {
                    dropLoc.getWorld().playSound(dropLoc, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 1.2f);
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (target.getWorld().equals(dropLoc.getWorld()) && target.getLocation().distanceSquared(dropLoc) <= (range * range)) {
                            double maxHp = SkriptUtils.getSkriptVariable(target, "hpmax");
                            double currentHp = SkriptUtils.getSkriptVariable(target, "hp");

                            if (currentHp < maxHp) {
                                double amountToHeal = maxHp * healPercent;
                                SkriptUtils.setVariable(target, "hp", Math.min(maxHp, currentHp + amountToHeal));
                                target.spawnParticle(Particle.VILLAGER_HAPPY, target.getLocation().add(0, 1.5, 0), 3, 0.2, 0.2, 0.2, 0);
                            }
                        }
                    }
                }
                ticks += 2;
            }
        }.runTaskTimer(KProfessionsCore.getInstance(), 0L, 2L);
    }
}