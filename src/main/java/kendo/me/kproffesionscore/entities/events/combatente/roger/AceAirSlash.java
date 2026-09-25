package kendo.me.kproffesionscore.entities.events.combatente.roger;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.entities.CustomEntity;
import kendo.me.kproffesionscore.builder.entities.EntityBuilder;
import kendo.me.kproffesionscore.utils.ChatUtils;
import kendo.me.kproffesionscore.utils.ConfigUtils;
import kendo.me.kproffesionscore.utils.skript.SkriptUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Air Slash: slash tacável (projétil) da Ace. Ativado com Botão Direito
 * (sem sneak — sneak+direito é reservado pro Kamusari, ver AceKamusari).
 * Usa o flipbook "roger ace", custom model data 33-48, na ordem de ativação.
 */
public class AceAirSlash implements Listener {

    private static final int FRAME_START = 33;
    private static final int FRAME_END = 48;
    private static final double PROJECTILE_SPEED = 0.9; // blocos por tick
    private static final double HIT_CHECK_RADIUS = 3.0;

    private final ConfigUtils configUtils = new ConfigUtils();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public AceAirSlash(KProfessionsCore plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAirSlash(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        if (event.getPlayer().isSneaking()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        YamlConfiguration config = configUtils.getConfigFile("combatente");
        int configModelData = config.getInt("espadas.ace-roger.model-data");
        if (!meta.hasCustomModelData() || meta.getCustomModelData() != configModelData) return;

        event.setCancelled(true);

        castAirSlash(player, config);
    }

    private void castAirSlash(Player player, YamlConfiguration config) {
        String path = "espadas.ace-roger.skills.air-slash.";
        double cdSeconds = config.getDouble(path + "cooldown");
        String cdMsg = config.getString(path + "cooldown-message");
        double damagePct = config.getDouble(path + "damage-percentage");

        if (!checkCooldown(player, cdSeconds, cdMsg)) return;

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.8f);
        launchProjectile(player, damagePct);
    }

    private void launchProjectile(Player player, double damagePct) {
        List<ItemStack> frames = new ArrayList<>();
        for (int i = FRAME_START; i <= FRAME_END; i++) {
            ItemStack frameItem = new ItemStack(Material.COAL);
            ItemMeta meta = frameItem.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(i);
                frameItem.setItemMeta(meta);
            }
            frames.add(frameItem);
        }


        Location eyeLoc = player.getEyeLocation().clone();
        Vector dir = eyeLoc.getDirection().clone().normalize();

        Location spawnLoc = eyeLoc.clone().add(dir.clone().multiply(1.0));

        float yawRad = (float) Math.toRadians(player.getLocation().getYaw() + 180f);
        float pitchRad = (float) Math.toRadians(-player.getLocation().getPitch());

        CustomEntity projectile = new EntityBuilder((int) (Math.random() * 999999))
                .setLocation(spawnLoc)
                .setModel(frames.get(0))
                .setInvisible(true)
                .setMarker(true)
                .setHitBox(1.4, 1.4)
                .setDebugMode(false)
                .build();

        List<Player> viewers = player.getWorld().getPlayers().stream()
                .filter(p -> p.getLocation().distanceSquared(spawnLoc) < 2500)
                .collect(Collectors.toList());

        projectile.spawn(viewers);
        KProfessionsCore.getEntityManager().addEntity(projectile);
        projectile.updateRotation(15, pitchRad, yawRad, 0F);

        final double baseStrength = SkriptUtils.getFinalStrength(player);
        final double damageToApply = baseStrength * (damagePct / 100.0);
        final Set<UUID> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            int frameIndex = 0;

            @Override
            public void run() {
                if (frameIndex >= frames.size() || !player.isOnline()) {
                    projectile.remove();
                    KProfessionsCore.getEntityManager().removeEntity(projectile);
                    this.cancel();
                    return;
                }

                projectile.teleport(projectile.getEntityLocation().add(dir.clone().multiply(PROJECTILE_SPEED)));
                Location currentLoc = projectile.getEntityLocation();

                for (org.bukkit.entity.Entity entity : currentLoc.getWorld().getNearbyEntities(currentLoc, HIT_CHECK_RADIUS, HIT_CHECK_RADIUS, HIT_CHECK_RADIUS)) {
                    if (!(entity instanceof LivingEntity target) || entity.getUniqueId().equals(player.getUniqueId())) {
                        continue;
                    }
                    if (hitEntities.contains(target.getUniqueId())) {
                        continue;
                    }
                    if (!projectile.getBoundingBox().overlaps(target.getBoundingBox())) {
                        continue;
                    }

                    // Player usa o "hp" do Skript; qualquer outro LivingEntity (mob) leva dano vanilla.
                    if (target instanceof Player targetPlayer) {
                        double targetHp = SkriptUtils.getSkriptVariable(targetPlayer, "hp");
                        double newHp = Math.max(0, targetHp - damageToApply);
                        SkriptUtils.setVariable(targetPlayer, "hp", newHp);
                    } else {
                        target.damage(damageToApply, player);
                    }

                    target.getWorld().spawnParticle(Particle.BLOCK_CRACK, target.getLocation().add(0, 1, 0), 15, 0.2, 0.2, 0.2, Material.REDSTONE_BLOCK.createBlockData());
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.6f, 1.4f);

                    Vector kb = dir.clone().multiply(0.5).setY(0.2);
                    target.setVelocity(target.getVelocity().add(kb));

                    hitEntities.add(target.getUniqueId());
                }

                projectile.drawHitbox();
                projectile.updateModel(frames.get(frameIndex));
                frameIndex++;
            }
        }.runTaskTimer(KProfessionsCore.getInstance(), 0L, 1L);
    }

    private boolean checkCooldown(Player p, double seconds, String message) {
        long now = System.currentTimeMillis();
        Long until = cooldowns.get(p.getUniqueId());

        if (until != null && until > now) {
            if (message != null && !message.isEmpty()) {
                double remaining = (until - now) / 1000.0;
                p.sendMessage(ChatUtils.color(message.replace("%time%", String.format("%.1f", remaining))));
            }
            return false;
        }

        cooldowns.put(p.getUniqueId(), now + (long) (seconds * 1000));
        return true;
    }
}