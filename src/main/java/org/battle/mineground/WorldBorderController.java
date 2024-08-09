package org.battle.mineground;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldBorderController {

    private final JavaPlugin plugin;
    private final World world;
    private final FileConfiguration config;
    private final List<BukkitRunnable> tasks = new ArrayList<>(); // 실행 중인 작업을 추적
    private final Random random = new Random();
    private final double fixedCenterX = -63;
    private final double fixedCenterZ = -113;
    private double randomCenterX;
    private double randomCenterZ;
    private double totalDistanceX;
    private double totalDistanceZ;
    private double totalShrinkTime;

    public WorldBorderController(JavaPlugin plugin) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld("world"); // 월드 이름을 필요에 따라 변경하세요
        this.config = plugin.getConfig();

    }



    private void showTargetLocation() {
        String targetMessage = String.format("Target Location: X=%.1f, Z=%.1f", randomCenterX, randomCenterZ);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("", targetMessage, 10, 70, 20); // 타이틀로 목표 지점 표시
        }
    }
    public void startPhases() {
        List<String> phaseKeys = config.getConfigurationSection("").getKeys(false).stream().toList();
        calculateRandomCenter();
        calculateTotalDistance();
        calculateTotalShrinkTime(phaseKeys);
        executePhase(phaseKeys, 0);  // 첫 번째 페이즈부터 시작
    }

    private void calculateRandomCenter() {
        this.randomCenterX = -313 + rand(25, 475);
        this.randomCenterZ = -363 + rand(25, 475);
        // HUD에 목표 지점 표시
        showTargetLocation();
    }

    private void calculateTotalDistance() {
        this.totalDistanceX = randomCenterX - fixedCenterX;
        this.totalDistanceZ = randomCenterZ - fixedCenterZ;
    }

    private void calculateTotalShrinkTime(List<String> phaseKeys) {
        this.totalShrinkTime = phaseKeys.stream()
                .mapToInt(phaseKey -> config.getInt(phaseKey + ".shrinktime"))
                .sum();
    }

    private void executePhase(List<String> phaseKeys, int index) {
        if (index >= phaseKeys.size()) {
            return;  // 모든 페이즈가 완료되면 종료
        }

        String phaseKey = phaseKeys.get(index);
        int breaktime = config.getInt(phaseKey + ".breaktime");
        int add = config.getInt(phaseKey + ".add");
        int shrinktime = config.getInt(phaseKey + ".shrinktime");
        double damage = config.getDouble(phaseKey + ".damage");

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // 월드보더 크기 조절 명령어 실행
                String command = String.format("worldborder add %d %d", add, shrinktime);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                // 월드보더 데미지 설정 명령어 실행
                String damageCommand = String.format("worldborder damage amount %f", damage);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), damageCommand);

                // 월드보더 센터 이동
                moveCenter(shrinktime);

                // 다음 페이즈 실행
                executePhase(phaseKeys, index + 1);
            }
        };

        task.runTaskLater(plugin, breaktime * 20L); // breaktime 초 후 실행 (틱 단위로 변환)
        tasks.add(task);  // 실행된 작업을 리스트에 추가
    }

    private void moveCenter(int shrinktime) {
        WorldBorder worldBorder = world.getWorldBorder();

        // 현재 페이즈에서 이동해야 할 거리 계산
        double stepX = (totalDistanceX * shrinktime) / totalShrinkTime;
        double stepZ = (totalDistanceZ * shrinktime) / totalShrinkTime;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= shrinktime * 20) {
                    this.cancel();
                    return;
                }

                // 현재 센터 좌표를 계산
                double newX = worldBorder.getCenter().getX() + stepX / (shrinktime * 20);
                double newZ = worldBorder.getCenter().getZ() + stepZ / (shrinktime * 20);

                // 센터를 이동
                worldBorder.setCenter(newX, newZ);
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // 매 틱(1/20초)마다 실행
    }

    public void stopPhases() {
        for (BukkitRunnable task : tasks) {
            task.cancel(); // 실행 중인 모든 작업 취소
        }
        tasks.clear();  // 작업 리스트 초기화
    }

    private int rand(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}