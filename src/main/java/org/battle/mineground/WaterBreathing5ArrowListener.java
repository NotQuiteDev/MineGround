package org.battle.mineground;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffectType;

public class WaterBreathing5ArrowListener implements Listener {

    private final MineGround plugin;

    public WaterBreathing5ArrowListener(MineGround plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();

            // 화살에 적용된 Potion NBT 확인
            if (arrow.hasCustomEffects()) {
                arrow.getCustomEffects().forEach(effect -> {
                    if (effect.getType().equals(PotionEffectType.WATER_BREATHING) && effect.getAmplifier() == 4) {
                        Entity hitEntity = event.getHitEntity();
                        Location impactLocation = (hitEntity != null) ? hitEntity.getLocation() : arrow.getLocation();

                        // 마그마 블록 생성 (동그란 형태)
                        transformAreaToMagma(impactLocation, plugin.getConfig().getInt("fire-arrow-radius", 3));
                        playExtinguishSound(impactLocation);

                        if (hitEntity != null && hitEntity instanceof LivingEntity) {
                            // 엔티티에 불 붙이기
                            igniteEntity((LivingEntity) hitEntity, plugin.getConfig().getInt("fire-arrow-duration", 5));
                            playExtinguishSound(hitEntity.getLocation());
                        }

                        // 화살 제거
                        arrow.remove();
                    }
                });
            }
        }
    }

    private void transformAreaToMagma(Location center, int radius) {
        int startX = center.getBlockX() - radius;
        int endX = center.getBlockX() + radius;
        int startY = center.getBlockY() - radius;
        int endY = center.getBlockY() + radius;
        int startZ = center.getBlockZ() - radius;
        int endZ = center.getBlockZ() + radius;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Location loc = new Location(center.getWorld(), x, y, z);
                    // 동그란 형태를 유지하기 위해 거리 체크
                    if (center.distance(loc) <= radius) {
                        Block block = loc.getBlock();
                        // 특정 블록이 아닌 경우 마그마 블록으로 변환
                        if (block.getType() != Material.AIR && block.getType() != Material.WATER &&
                                block.getType() != Material.CHEST && block.getType() != Material.BARRIER && block.getType() != Material.BEDROCK) {
                            block.setType(Material.MAGMA_BLOCK);
                        }
                    }
                }
            }
        }
    }

    private void igniteEntity(LivingEntity entity, int duration) {
        // 엔티티에게 불을 붙임
        entity.setFireTicks(duration * 20);  // duration은 초 단위이며, 1초 = 20틱
    }

    private void playExtinguishSound(Location location) {
        // 불이 꺼지는 효과음 재생
        location.getWorld().playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
    }
}
