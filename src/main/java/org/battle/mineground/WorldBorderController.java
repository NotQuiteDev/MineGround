package org.battle.mineground;


import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    private BukkitRunnable particleTask;
    public WorldBorderController(JavaPlugin plugin) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld("world"); // 월드 이름을 필요에 따라 변경하세요
        this.config = plugin.getConfig();

    }



    private void showTargetLocation() {
        String titleMessage = "§c§lFinal Safe Zone";
        String subtitleMessage = String.format("§e§lMove to X=%.1f, Z=%.1f", randomCenterX, randomCenterZ);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(titleMessage, subtitleMessage, 10, 70, 20); // 타이틀로 마지막 안전 구역 안내
        }
    }
    private void showTargetLocation2() {
        String targetMessage = String.format("§c§lAttention! §eThe final safe zone is located at §c§lX=%.1f, Z=%.1f§e. Move quickly!", randomCenterX, randomCenterZ);
        Bukkit.broadcastMessage(targetMessage); // 채팅으로 마지막 안전 구역 알림
    }
    public void startPhases() {
        List<String> phaseKeys = config.getConfigurationSection("").getKeys(false).stream().toList();
        calculateRandomCenter();
        calculateTotalDistance();
        teleportPlayers();
        showTargetLocation();
        showTargetLocation2();
        calculateTotalShrinkTime(phaseKeys);
        performAdditionalCommands();
        executePhase(phaseKeys, 0);  // 첫 번째 페이즈부터 시작
    }
    private void performAdditionalCommands() {
        // 1. 경험치 설정 (exp set * <경험치>)
        int experience = config.getInt("experience");
        String expCommand = String.format("exp set * %d", experience);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), expCommand);

        // 2. 모든 플레이어 치유 (heal *)
        String healCommand = "heal *";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), healCommand);

        // 3. 아이템 초기화 (clear * **)
        String clearCommand = "clear * **";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clearCommand);

        // 4. 모든 상자 초기화 (lc respawnall)
        String respawnCommand = "lc respawnall";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), respawnCommand);
        // 3. 모든 플레이어 서바이벌 모드로 변경 (gamemode survival *)
        String survivalCommand = "gamemode survival *";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), survivalCommand);

    }
    // 파티클 소환 메소드
    // 파티클 소환 메소드
    // 파티클 빔 생성 메소드
    public void spawnParticleBeam(World world, double x, double z) {
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (int y = 0; y <= world.getMaxHeight(); y += 5) { // Y축 간격을 늘려 파티클 수 줄이기
                    Location location = new Location(world, x, y, z);
                    world.spawnParticle(Particle.GLOW_SQUID_INK, location, 100, 0.1, 0.1, 0.1, 0, null, true);
                    // 파티클 수를 줄여서 성능 최적화
                }
            }
        };
        particleTask.runTaskTimer(plugin, 0L, 20L); // 20틱(1초) 간격으로 파티클 생성
    }
    // 파티클 소환 취소 메소드
    public void cancelParticleBeam() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }

    private void teleportPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            double randomX = -313 + rand(25, 475);
            double randomZ = -363 + rand(25, 475);
            Location randomLocation = new Location(world, randomX, 150, randomZ);
            player.teleport(randomLocation);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 15 * 20, 1)); // 느린 낙하 효과 적용 (15초)
        }
    }
    private void calculateRandomCenter() {
        this.randomCenterX = -313 + rand(25, 475);
        this.randomCenterZ = -363 + rand(25, 475);
        // HUD에 목표 지점 표시
        showTargetLocation();
        spawnParticleBeam(Bukkit.getWorld("world"), randomCenterX, randomCenterZ); // 파티클 소환
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
        // 파티클 빔 소환 취소
        cancelParticleBeam();

        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(fixedCenterX, fixedCenterZ); // 센터 위치를 기본 위치로 변경
        worldBorder.setSize(500); // 월드보더 크기를 500으로 설정
        Bukkit.broadcastMessage("WorldBorder has been reset to the original center (-63, -113) and size (500).");
    }

    private int rand(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}