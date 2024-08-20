package org.battle.mineground.arrows;

import org.battle.mineground.MineGround;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffectType;

public class WaterBreathing3ArrowListener implements Listener {

    private final MineGround plugin;

    public WaterBreathing3ArrowListener(MineGround plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();

            // 화살에 적용된 Potion NBT 확인
            if (arrow.hasCustomEffects()) {
                arrow.getCustomEffects().forEach(effect -> {
                    if (effect.getType().equals(PotionEffectType.WATER_BREATHING) && effect.getAmplifier() == 2) {
                        // Water Breathing III 포션이 적용된 화살에 맞았을 때
                        if (arrow.getShooter() instanceof Player) {
                            Player player = (Player) arrow.getShooter();

                            // 플레이어를 화살이 맞은 위치로 순간이동
                            teleportPlayer(player, arrow.getLocation());

                            // 화살에 색상 효과 추가
                            arrow.getWorld().spawnParticle(Particle.REDSTONE, arrow.getLocation(), 10, new Particle.DustOptions(Color.PURPLE, 1.0F));

                            // 화살 제거
                            arrow.remove();
                        }
                    }
                });
            }
        }
    }

    private void teleportPlayer(Player player, Location location) {
        // 플레이어의 기존 pitch와 yaw를 유지
        Location teleportLocation = location.clone();
        teleportLocation.setPitch(player.getLocation().getPitch());
        teleportLocation.setYaw(player.getLocation().getYaw());

        // 엔더 진주와 같은 순간이동 로직을 적용
        player.teleport(teleportLocation);
        player.getWorld().playSound(teleportLocation, "entity.enderman.teleport", 1.0F, 1.0F);
        player.getWorld().spawnParticle(Particle.PORTAL, teleportLocation, 200, 0.5, 0.5, 0.5, 0.2);
    }
}