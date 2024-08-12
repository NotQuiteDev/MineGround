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
import org.bukkit.util.Vector;

public class WaterBreathing4ArrowListener implements Listener {

    private final MineGround plugin;

    public WaterBreathing4ArrowListener(MineGround plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();

            // 화살에 적용된 Potion NBT 확인
            if (arrow.hasCustomEffects()) {
                arrow.getCustomEffects().forEach(effect -> {
                    if (effect.getType().equals(PotionEffectType.WATER_BREATHING) && effect.getAmplifier() == 3) {
                        // Water Breathing IV 포션이 적용된 화살에 맞았을 때
                        Entity hitEntity = event.getHitEntity();
                        if (hitEntity != null && hitEntity instanceof LivingEntity) {
                            // 엔티티에 맞았을 때, 엔티티를 얼음 속에 가둠
                            freezeEntity((LivingEntity) hitEntity, plugin.getConfig().getInt("ice-arrow-radius", 5));
                            // 엔티티가 얼음 속에 갇히는 소리 재생
                            playFreezeSound(hitEntity.getLocation());
                        } else {
                            // 땅에 맞았을 때, 주변 구체를 얼음으로 변환
                            freezeArea(arrow.getLocation(), plugin.getConfig().getInt("ice-arrow-radius", 5));
                            // 땅에 맞아 주변을 얼음으로 변환하는 소리 재생
                            playFreezeSound(arrow.getLocation());
                        }

                        // 화살 제거
                        arrow.remove();
                    }
                });
            }
        }
    }

    private void freezeArea(Location center, int radius) {
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
                    if (center.distance(loc) <= radius) {
                        Block block = loc.getBlock();
                        if (block.getType() != Material.AIR) {
                            block.setType(Material.PACKED_ICE); // 녹지 않는 얼음 사용
                        }
                    }
                }
            }
        }
    }

    private void freezeEntity(LivingEntity entity, int radius) {
        Location center = entity.getLocation();

        // 엔티티 주변에 더 큰 구체를 그려서 얼음으로 가둠
        freezeArea(center, radius);

        // 엔티티를 확실히 가두기 위해 더 큰 구체를 생성
        int expandedRadius = 3; //
        int startX = center.getBlockX() - expandedRadius;
        int endX = center.getBlockX() + expandedRadius;
        int startY = center.getBlockY() - expandedRadius;
        int endY = center.getBlockY() + expandedRadius;
        int startZ = center.getBlockZ() - expandedRadius;
        int endZ = center.getBlockZ() + expandedRadius;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Location loc = new Location(center.getWorld(), x, y, z);
                    if (center.distance(loc) <= expandedRadius) {
                        Block block = loc.getBlock();
                        if (block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR) {
                            block.setType(Material.PACKED_ICE); // 확실히 가두기 위해 녹지 않는 얼음 사용
                        }
                    }
                }
            }
        }

        // 엔티티가 움직이지 못하도록 속도 설정
        entity.setVelocity(new Vector(0, 0, 0));
    }

    private void playFreezeSound(Location location) {
        // 얼음이 생성되는 효과음을 재생 (여기서는 기본적인 얼음 깨지는 소리)
        location.getWorld().playSound(location, Sound.BLOCK_GLASS_PLACE, 1.0f, 1.0f);
        location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 0.5f, 1.2f);
    }
}