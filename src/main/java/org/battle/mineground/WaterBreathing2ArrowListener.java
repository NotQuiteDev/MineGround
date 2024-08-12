package org.battle.mineground;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WaterBreathing2ArrowListener implements Listener {

    private final MineGround plugin;

    public WaterBreathing2ArrowListener(MineGround plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();

            // 화살에 적용된 Potion NBT 확인
            if (arrow.hasCustomEffects()) {
                arrow.getCustomEffects().forEach(effect -> {
                    if (effect.getType().equals(PotionEffectType.WATER_BREATHING) && effect.getAmplifier() == 1) {
                        // Water Breathing II 포션이 적용된 화살에 번개를 소환
                        summonLightning(arrow.getLocation());
                        // 번개 소환 후 화살 제거
                        arrow.remove();
                    }
                });
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        for (LivingEntity entity : event.getAffectedEntities()) {
            PotionEffect waterBreathingEffect = entity.getPotionEffect(PotionEffectType.WATER_BREATHING);

            if (waterBreathingEffect != null && waterBreathingEffect.getAmplifier() == 1) {
                // Water Breathing II 포션 효과가 있는 엔티티 근처에 번개 소환
                summonLightning(entity.getLocation());
            }
        }
    }

    private void summonLightning(Location location) {
        // config에서 번개 소환 범위를 불러옴
        double lightningRange = plugin.getConfig().getDouble("lightning-range", 5.0);

        // 번개를 소환할 위치를 결정하고 소환
        location.getWorld().strikeLightning(location);

        // 번개 범위 내의 위치에도 번개를 소환할 수 있음
        // 예를 들어, 범위 내의 네 개의 방향에 번개를 소환하는 코드
        location.getWorld().strikeLightning(location.add(lightningRange, 0, 0));
        location.getWorld().strikeLightning(location.add(-lightningRange, 0, 0));
        location.getWorld().strikeLightning(location.add(0, 0, lightningRange));
        location.getWorld().strikeLightning(location.add(0, 0, -lightningRange));
    }
}