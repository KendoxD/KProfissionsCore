package kendo.me.kproffesionscore.entities.events.combatente.roger;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.entities.CustomEntity;
import kendo.me.kproffesionscore.builder.entities.EntityBuilder;
import kendo.me.kproffesionscore.utils.ChatUtils;
import kendo.me.kproffesionscore.utils.ConfigUtils;
import kendo.me.kproffesionscore.utils.skript.SkriptUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class AceSlash implements Listener {

    private static final double SPAWN_HEIGHT_OFFSET = -0.6;
    private static final double SPAWN_FORWARD_DISTANCE = 1.3;

    private final ConfigUtils configUtils = new ConfigUtils();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public AceSlash(KProfessionsCore plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAttack(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        YamlConfiguration config = configUtils.getConfigFile("combatente");
        int configModelData = config.getInt("espadas.ace-roger.model-data");

        if (!meta.hasCustomModelData() || meta.getCustomModelData() != configModelData) return;

        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            handleSkill(player, config, "slash-normal", 17, 32, false, 0.0);
        }
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        YamlConfiguration config = configUtils.getConfigFile("combatente");
        int configModelData = config.getInt("espadas.ace-roger.model-data");

        if (item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == configModelData) {
            handleSkill(player, config, "slash-normal", 17, 32, false, 0.0);
        }
    }

    private void handleSkill(Player player, YamlConfiguration config, String skillKey, int startFrame, int endFrame, boolean isProjectile, double speed) {
        String path = "espadas.ace-roger.skills." + skillKey + ".";
        double cdSeconds = config.getDouble(path + "cooldown");
        String cdMsg = config.getString(path + "cooldown-message");

        double damagePct = config.getDouble(path + "damage-percentage");

        if (!checkCooldown(player, skillKey, cdSeconds, cdMsg)) return;
        if (skillKey.equals("slash-normal")) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.3f);
        }
        spawnSlashAnimation(player, startFrame, endFrame, isProjectile, speed, damagePct);
    }

    private void spawnSlashAnimation(Player player, int start, int end, boolean isProjectile, double speed, double damagePct) {
        List<ItemStack> animationFrames = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            ItemStack frameItem = new ItemStack(Material.COAL);
            ItemMeta meta = frameItem.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(i);
                frameItem.setItemMeta(meta);
            }
            animationFrames.add(frameItem);
        }

        Location base = player.getLocation().clone();
        Vector dir = base.getDirection().setY(0);
        if (dir.lengthSquared() < 1.0E-4) {
            double yawRad = Math.toRadians(base.getYaw());
            dir = new Vector(-Math.sin(yawRad), 0, Math.cos(yawRad));
        }
        dir.normalize();

        Location spawnLoc = base.clone();
        spawnLoc.add(dir.clone().multiply(SPAWN_FORWARD_DISTANCE));
        spawnLoc.add(0, SPAWN_HEIGHT_OFFSET, 0);
        float yawDeg = player.getLocation().getYaw() + 180f;
        float pitchDeg = 0F;

        CustomEntity slash = new EntityBuilder((int) (Math.random() * 999999))
                .setLocation(spawnLoc)
                .setModel(animationFrames.get(0))
                .setInvisible(true)
                .setMarker(true)
                .setHitBox(2.0, 1.5)
                .setDebugMode(false)
                .build();

        List<Player> nearby = player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(spawnLoc) < 2500)
                .collect(Collectors.toList());

        slash.spawn(nearby);
        KProfessionsCore.getEntityManager().addEntity(slash);

        slash.updateRotation(15, pitchDeg, yawDeg, 0F);

        final double baseStrength = SkriptUtils.getFinalStrength(player);
        final double damageToApply = baseStrength * (damagePct / 100.0);
        final Set<UUID> hitEntities = new HashSet<>();
        final Vector finalDir = dir;

        final List<org.bukkit.entity.Entity> staticNearby = isProjectile
                ? null
                : new ArrayList<>(spawnLoc.getWorld().getNearbyEntities(spawnLoc, 3.5, 3.5, 3.5));

        new BukkitRunnable() {
            int frameIndex = 0;

            @Override
            public void run() {
                if (frameIndex >= animationFrames.size() || !player.isOnline()) {
                    slash.remove();
                    KProfessionsCore.getEntityManager().removeEntity(slash);
                    this.cancel();
                    return;
                }

                if (isProjectile) {
                    slash.teleport(slash.getEntityLocation().add(finalDir.clone().multiply(speed)));
                }

                Location currentLoc = slash.getEntityLocation();

                Collection<org.bukkit.entity.Entity> candidates = isProjectile
                        ? currentLoc.getWorld().getNearbyEntities(currentLoc, 3.5, 3.5, 3.5)
                        : staticNearby;

                for (org.bukkit.entity.Entity entity : candidates) {
                    if (!(entity instanceof org.bukkit.entity.LivingEntity target) || entity.getUniqueId().equals(player.getUniqueId())) {
                        continue;
                    }
                    if (hitEntities.contains(target.getUniqueId())) {
                        continue;
                    }

                    if (!slash.getBoundingBox().overlaps(target.getBoundingBox())) {
                        continue;
                    }

                    if (target instanceof Player targetPlayer) {
                        double targetHp = SkriptUtils.getSkriptVariable(targetPlayer, "hp");
                        double newHp = Math.max(0, targetHp - damageToApply);
                        SkriptUtils.setVariable(targetPlayer, "hp", newHp);
                    } else {
                        target.damage(damageToApply, player);
                    }

                    target.getWorld().spawnParticle(Particle.BLOCK_CRACK, target.getLocation().add(0, 1, 0), 15, 0.2, 0.2, 0.2, Material.REDSTONE_BLOCK.createBlockData());
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.5f, 1.6f);
                    Vector kb = finalDir.clone().multiply(0.5).setY(0.2);
                    target.setVelocity(target.getVelocity().add(kb));

                    hitEntities.add(target.getUniqueId());
                }

                slash.drawHitbox();

                slash.updateModel(animationFrames.get(frameIndex));
                frameIndex++;
            }
        }.runTaskTimer(KProfessionsCore.getInstance(), 0L, 1L);
    }

    private boolean checkCooldown(Player p, String skill, double seconds, String message) {
        long now = System.currentTimeMillis();
        Map<String, Long> pCD = cooldowns.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>());

        if (pCD.containsKey(skill) && pCD.get(skill) > now) {
            if (message != null && !message.isEmpty()) {
                double remaining = (pCD.get(skill) - now) / 1000.0;
                p.sendMessage(ChatUtils.color(message.replace("%time%", String.format("%.1f", remaining))));
            }
            return false;
        }

        pCD.put(skill, now + (long) (seconds * 1000));
        return true;
    }
}