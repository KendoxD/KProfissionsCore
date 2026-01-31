package kendo.me.kproffesionscore.entities;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.entities.ProjectileBuilder;
import kendo.me.kproffesionscore.entities.projectil.CustomProjectil;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
//TODO: clean this bitch
public class SeringaProjectil extends CustomProjectil implements Listener {

    public SeringaProjectil(ProjectileBuilder builder) {
        super(builder);
        Bukkit.getPluginManager().registerEvents(this, KProfessionsCore.getInstance());
    }

    @Override
    public void onHitPlayer(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.5f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 5, 1));

        player.sendTitle("", ChatUtils.color("&cVoce foi atingido!!"), 5,10,5);
    }

    @Override
    public void onHitBlock(org.bukkit.Location loc) {
        loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);
    }
    @Override
    public void onTick() {
        entity.getEntityLocation().getWorld().spawnParticle(
                org.bukkit.Particle.VILLAGER_HAPPY,
                entity.getEntityLocation(),
                1, 0.1, 0.1, 0.1, 0
        );
    }


}