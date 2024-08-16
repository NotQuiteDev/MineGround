package org.battle.mineground;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
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

    // 생성자를 통해 플러그인 인스턴스를 전달받음
    public HorseTameListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        // 플레이어가 상호작용한 엔티티가 말 종류인지 확인
        if (event.getRightClicked() instanceof Horse) {
            tameHorse(player, (Horse) event.getRightClicked());
        } else if (event.getRightClicked() instanceof ZombieHorse) {
            tameZombieHorse(player, (ZombieHorse) event.getRightClicked());
        } else if (event.getRightClicked() instanceof SkeletonHorse) {
            tameSkeletonHorse(player, (SkeletonHorse) event.getRightClicked());
        }
    }

    // 일반 말을 길들일 때 호출되는 메서드
    private void tameHorse(Player player, Horse horse) {
        if (!horse.isTamed()) {
            horse.setOwner(player);
            horse.setTamed(true);
            sendHorseStats(player, horse);
        }
    }

    // 좀비 말을 길들일 때 호출되는 메서드
    private void tameZombieHorse(Player player, ZombieHorse horse) {
        if (!horse.isTamed()) {
            horse.setOwner(player);
            horse.setTamed(true);
            sendZombieHorseStats(player, horse);
        }
    }

    // 스켈레톤 말을 길들일 때 호출되는 메서드
    private void tameSkeletonHorse(Player player, SkeletonHorse horse) {
        if (!horse.isTamed()) {
            horse.setOwner(player);
            horse.setTamed(true);
            sendSkeletonHorseStats(player, horse);
        }
    }

    // 일반 말의 능력치를 플레이어에게 메시지로 보내고 화면 중앙에 표시
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

    // 좀비 말의 능력치를 플레이어에게 메시지로 보내고 화면 중앙에 표시
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

    // 스켈레톤 말의 능력치를 플레이어에게 메시지로 보내고 화면 중앙에 표시
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
