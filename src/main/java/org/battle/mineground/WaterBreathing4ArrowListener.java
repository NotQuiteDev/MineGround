package org.battle.mineground;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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

import java.util.Random;

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
        Random random = new Random();

        // config에서 얼음 변환 확률을 가져옴 (기본값 0.3)
        double freezeChance = plugin.getConfig().getDouble("ice-arrow-freeze-chance", 0.3);

        // 먼저 반경 내의 블록을 얼음으로 변환
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Location loc = new Location(center.getWorld(), x, y, z);
                    if (center.distance(loc) <= radius) {
                        Block block = loc.getBlock();
                        // config에 설정된 확률로 블록을 얼음으로 변환
                        if (block.getType() != Material.AIR && block.getType() != Material.CHEST && block.getType() != Material.BARRIER && block.getType() != Material.BEDROCK) {
                            if (random.nextDouble() <= freezeChance) {
                                block.setType(Material.PACKED_ICE); // 녹지 않는 얼음 사용
                                playIceEffect(loc); // 냉기 파티클 효과 추가
                            }
                        }
                    }
                }
            }
        }

        // 엔티티 위치에 얼음 기둥 생성
        for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity instanceof LivingEntity) {
                Location entityLocation = entity.getLocation();
                createIcePillar(entityLocation);
            }
        }
    }

    private void createIcePillar(Location loc) {
        // 위치에 높이 2짜리 얼음 기둥 생성
        Block block1 = loc.getBlock();
        Block block2 = loc.clone().add(0, 1, 0).getBlock();

        if (block1.getType() == Material.AIR || block1.getType() == Material.CAVE_AIR) {
            block1.setType(Material.PACKED_ICE);
        }
        if (block2.getType() == Material.AIR || block2.getType() == Material.CAVE_AIR) {
            block2.setType(Material.PACKED_ICE);
        }
    }

    private void freezeEntity(LivingEntity entity, int radius) {
        Location center = entity.getLocation();
        Random random = new Random();

        // 엔티티 주변에 더 큰 구체를 그려서 얼음으로 가둠
        freezeArea(center, radius);

        // config에서 엔티티 주변 얼음 변환 확률을 가져옴 (기본값 0.2)
        double entityFreezeChance = plugin.getConfig().getDouble("ice-entity-freeze-chance", 0.2);

        // 엔티티를 확실히 가두기 위해 더 큰 구체를 생성
        int expandedRadius = 3;
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
                        // config에 설정된 확률로 공기 블록을 얼음으로 변환
                        if ((block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR)
                                && block.getType() != Material.CHEST
                                && block.getType() != Material.BARRIER
                                && block.getType() != Material.BEDROCK) {

                            if (random.nextDouble() <= entityFreezeChance) {
                                block.setType(Material.PACKED_ICE); // 확실히 가두기 위해 녹지 않는 얼음 사용
                                playIceEffect(loc); // 냉기 파티클 효과 추가
                            }
                        }
                    }
                }
            }
        }

        // 엔티티가 움직이지 못하도록 속도 설정
        entity.setVelocity(new Vector(0, 0, 0));
    }
    private void playIceEffect(Location location) {
        // 냉기스러운 파티클 효과 추가 (개수 줄여서 성능 최적화)
        location.getWorld().spawnParticle(Particle.CLOUD, location, 5, 0.5, 0.5, 0.5, 0.01);
        location.getWorld().spawnParticle(Particle.SNOWFLAKE, location, 8, 0.5, 0.5, 0.5, 0.01); // 1.17+ 버전
        location.getWorld().spawnParticle(Particle.SNOW_SHOVEL, location, 5, 0.5, 0.5, 0.5, 0.01);
    }

    private void playFreezeSound(Location location) {
        // 소리의 볼륨과 피치 조절, 더 멀리서 들리도록 조정
        location.getWorld().playSound(location, Sound.BLOCK_GLASS_PLACE, 2.0f, 1.0f);  // 볼륨을 2.0f로 증가
        location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.2f);  // 볼륨을 1.0f로 증가
    }
}