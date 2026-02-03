package kendo.me.kproffesionscore.entities;

import kendo.me.kproffesionscore.KProfessionsCore;
import kendo.me.kproffesionscore.builder.entities.ProjectileBuilder;
import kendo.me.kproffesionscore.entities.projectil.CustomProjectil;
import kendo.me.kproffesionscore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
public class GreenSeringeProjectil extends CustomProjectil implements Listener {
    //TODO: Javadocs this shit
    public GreenSeringeProjectil(ProjectileBuilder builder) {
        super(builder);
        Bukkit.getPluginManager().registerEvents(this, KProfessionsCore.getInstance());
    }

    @Override
    public void onHitPlayer(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.5f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 80, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 200, 2));

        player.sendTitle("", ChatUtils.color("&cVoce foi atingido por uma seringa!!"), 5,10,10);
    }

    @Override
    public void onHitBlock(org.bukkit.Location loc) {
        loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.5f, 2.0f);
    }
    @Override
    public void onTick() {
        entity.spawnParticle(Particle.VILLAGER_HAPPY, 1, 0.05);
    }


}