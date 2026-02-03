package kendo.me.kproffesionscore.entities.manager;

import kendo.me.kproffesionscore.builder.entities.CustomEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class EntityManager extends BukkitRunnable {
    private final List<CustomEntity> activeEntities = new ArrayList<>();

    public void addEntity(CustomEntity entity) {
        activeEntities.add(entity);
    }

    public void removeEntityFromViewer(CustomEntity entity, List<Player> viewers) {
        entity.remove();
        activeEntities.remove(entity);
    }

    public void removeEntity(CustomEntity entity){
        entity.remove();
        activeEntities.remove(entity);
    }

    public void removeAllEntities(){
        for (CustomEntity activeEntity : activeEntities) {
            activeEntity.remove();
        }
        activeEntities.clear();
    }
    @Override
    public void run() {
//        for (CustomEntity entity : new ArrayList<>(activeEntities)) {
//            for (Player p : Bukkit.getOnlinePlayers()) {
//                if (entity.isInside(p.getLocation()) || entity.isInside(p.getEyeLocation())) {
//                    p.sendTitle("", ChatUtils.color("&c&lDENTRO DA HITBOX"), 0, 5, 0);
//                }
//            }
//        }w
    }
}
