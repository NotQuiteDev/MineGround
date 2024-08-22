package org.battle.mineground;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HorseTameListener implements Listener {

    private final Plugin plugin;

    public HorseTameListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getRightClicked() instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse) event.getRightClicked();

            // 각 타입에 따라 적절한 길들이기 메서드 호출
            if (horse instanceof Horse) {
                tameHorse(player, (Horse) horse);
            } else if (horse instanceof ZombieHorse) {
                tameZombieHorse(player, (ZombieHorse) horse);
            } else if (horse instanceof SkeletonHorse) {
                tameSkeletonHorse(player, (SkeletonHorse) horse);
            }
        }
    }

    private void tameHorse(Player player, Horse horse) {
        if (!horse.isTamed()) {
            horse.setOwner(player);
            horse.setTamed(true);
            applySpeedMultiplier(horse);
            sendHorseStats(player, horse);
        }
    }

    private void tameZombieHorse(Player player, ZombieHorse horse) {
        if (!horse.isTamed()) {
            horse.setOwner(player);
            horse.setTamed(true);
            applySpeedMultiplier(horse);  // 좀비 말의 속도 수정
            sendZombieHorseStats(player, horse);
        }
    }

    private void tameSkeletonHorse(Player player, SkeletonHorse horse) {
        if (!horse.isTamed()) {
            horse.setOwner(player);
            horse.setTamed(true);
            applySpeedMultiplier(horse);  // 스켈레톤 말의 속도 수정
            sendSkeletonHorseStats(player, horse);
        }
    }

    // 속도를 설정하는 메서드 (AbstractHorse 타입을 받아서 공통 처리)
    private void applySpeedMultiplier(AbstractHorse horse) {
        AttributeInstance speedAttribute = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            double walkSpeedMultiplier = plugin.getConfig().getDouble("player-walk-speed", 1.0); // 기본값은 1.0
            speedAttribute.setBaseValue(speedAttribute.getBaseValue() * walkSpeedMultiplier);
        }
    }

    private void sendHorseStats(Player player, Horse horse) {
        double speed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
        double jumpStrength = horse.getJumpStrength();
        double health = horse.getHealth();

        player.sendMessage(ChatColor.GREEN + "말이 길들여졌습니다! 능력치:");
        player.sendMessage(ChatColor.YELLOW + "속도: " + ChatColor.WHITE + String.format("%.2f", speed));
        player.sendMessage(ChatColor.YELLOW + "점프력: " + ChatColor.WHITE + String.format("%.2f", jumpStrength));
        player.sendMessage(ChatColor.YELLOW + "체력: " + ChatColor.WHITE + String.format("%.2f", health));

        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendTitle(ChatColor.GREEN + "말 능력치",
                        ChatColor.YELLOW + "속도: " + String.format("%.2f", speed) +
                                " | 점프력: " + String.format("%.2f", jumpStrength) +
                                " | 체력: " + String.format("%.2f", health),
                        10, 70, 20);
            }
        }.runTaskLater(plugin, 10L);
    }

    private void sendZombieHorseStats(Player player, ZombieHorse horse) {
        double speed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
        double jumpStrength = horse.getJumpStrength();
        double health = horse.getHealth();

        player.sendMessage(ChatColor.GREEN + "좀비 말이 길들여졌습니다! 능력치:");
        player.sendMessage(ChatColor.YELLOW + "속도: " + ChatColor.WHITE + String.format("%.2f", speed));
        player.sendMessage(ChatColor.YELLOW + "점프력: " + ChatColor.WHITE + String.format("%.2f", jumpStrength));
        player.sendMessage(ChatColor.YELLOW + "체력: " + ChatColor.WHITE + String.format("%.2f", health));

        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendTitle(ChatColor.GREEN + "좀비 말 능력치",
                        ChatColor.YELLOW + "속도: " + String.format("%.2f", speed) +
                                " | 점프력: " + String.format("%.2f", jumpStrength) +
                                " | 체력: " + String.format("%.2f", health),
                        10, 70, 20);
            }
        }.runTaskLater(plugin, 10L);
    }

    private void sendSkeletonHorseStats(Player player, SkeletonHorse horse) {
        double speed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
        double jumpStrength = horse.getJumpStrength();
        double health = horse.getHealth();

        player.sendMessage(ChatColor.GREEN + "스켈레톤 말이 길들여졌습니다! 능력치:");
        player.sendMessage(ChatColor.YELLOW + "속도: " + ChatColor.WHITE + String.format("%.2f", speed));
        player.sendMessage(ChatColor.YELLOW + "점프력: " + ChatColor.WHITE + String.format("%.2f", jumpStrength));
        player.sendMessage(ChatColor.YELLOW + "체력: " + ChatColor.WHITE + String.format("%.2f", health));

        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendTitle(ChatColor.GREEN + "스켈레톤 말 능력치",
                        ChatColor.YELLOW + "속도: " + String.format("%.2f", speed) +
                                " | 점프력: " + String.format("%.2f", jumpStrength) +
                                " | 체력: " + String.format("%.2f", health),
                        10, 70, 20);
            }
        }.runTaskLater(plugin, 10L);
    }
}
