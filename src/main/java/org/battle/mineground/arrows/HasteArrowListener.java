package org.battle.mineground.arrows;

import org.battle.mineground.MineGround;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HasteArrowListener implements Listener {

    private final MineGround plugin;

    public HasteArrowListener(MineGround plugin) {
        this.plugin = plugin;
    }

    // 화살이 땅에 박힐 때 이벤트
    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();

            // 화살에 적용된 Potion NBT 확인
            if (arrow.hasCustomEffects()) {
                arrow.getCustomEffects().forEach(effect -> {
                    if (effect.getType().equals(PotionEffectType.FAST_DIGGING) && effect.getAmplifier() == 2) {
                        // Haste III 적용된 화살이 땅에 박히면 폭발
                        arrow.getWorld().createExplosion(arrow.getLocation(), (float) plugin.getExplosionRadius());
                        arrow.remove();  // 화살 제거
                    }
                });
            }
        }
    }

    // 포션 효과가 생기는 이벤트
    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        event.getAffectedEntities().forEach(entity -> {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                PotionEffect hasteEffect = livingEntity.getPotionEffect(PotionEffectType.FAST_DIGGING);

                if (hasteEffect != null && hasteEffect.getAmplifier() == 2) {
                    // Haste III 적용된 엔티티에 폭발
                    livingEntity.getWorld().createExplosion(livingEntity.getLocation(), (float) plugin.getExplosionRadius());
                }
            }
        });
    }
}