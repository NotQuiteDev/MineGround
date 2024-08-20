package org.battle.mineground;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ExplosionDamageListener implements Listener {

    private final JavaPlugin plugin;

    public ExplosionDamageListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // 이벤트가 플레이어에게 발생했고, 데미지 원인이 폭발일 경우
        if (event.getEntity() instanceof Player &&
                (event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION)) {

            // config.yml에서 데미지 감소 비율을 가져옴
            double reductionFactor = plugin.getConfig().getDouble("explosion-damage-reduction", 1.0); // 기본값 1.0 (즉, 데미지 감소 없음)

            // 데미지를 감소 비율에 맞춰 줄임
            double originalDamage = event.getDamage();
            double reducedDamage = originalDamage * reductionFactor;

            event.setDamage(reducedDamage);
        }
    }
}