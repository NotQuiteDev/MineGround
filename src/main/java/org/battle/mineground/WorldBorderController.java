package org.battle.mineground;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldBorderController implements Listener{

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
    private BossBar bossBar; // 보스바 추가
    private int survivingPlayers;
    private BukkitRunnable particleTask;

    public WorldBorderController(JavaPlugin plugin) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld("world"); // 월드 이름을 필요에 따라 변경하세요
        this.config = plugin.getConfig();
        this.survivingPlayers = Bukkit.getOnlinePlayers().size();
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
        // 현재 서버에 있는 생존자(서바이벌 모드)의 수를 카운트




        List<String> phaseKeys = config.getConfigurationSection("").getKeys(false).stream().toList();
        calculateRandomCenter();
        calculateTotalDistance();
        teleportPlayers();
        showTargetLocation();
        showTargetLocation2();
        calculateTotalShrinkTime(phaseKeys);
        performAdditionalCommands();
        executePhase(phaseKeys, 0);  // 첫 번째 페이즈부터 시작

        int totalPlayers = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                totalPlayers++;
            }
        }
        survivingPlayers = totalPlayers;

        // 보스바 초기화
        bossBar = Bukkit.createBossBar("Survivors: " + survivingPlayers + "/" + totalPlayers, BarColor.GREEN, BarStyle.SOLID);
        bossBar.setVisible(true);

        // 모든 플레이어를 보스바에 추가
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.setGameMode(GameMode.SPECTATOR); // 플레이어를 관전자 모드로 변경
        if (survivingPlayers > 0) {
            survivingPlayers--;
        }
        updateBossBar();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SURVIVAL && survivingPlayers > 0) {
            survivingPlayers--;
        }
        updateBossBar();
    }
    private void updateBossBar() {
        int totalPlayers = Bukkit.getOnlinePlayers().size();
        double progress = (double) survivingPlayers / totalPlayers;
        bossBar.setTitle("Survivors: " + survivingPlayers + "/" + totalPlayers);
        bossBar.setProgress(progress);

        // 보스바 색상 업데이트 (예: 생존자가 적을수록 색상 변경)
        if (progress > 0.5) {
            bossBar.setColor(BarColor.GREEN);
        } else if (progress > 0.2) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.RED);
        }
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

        // 5. 모든 플레이어 서바이벌 모드로 변경 (gamemode survival *)
        String survivalCommand = "gamemode survival *";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), survivalCommand);
    }

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

        // Break Time 동안의 액션바 메시지
        BukkitRunnable breakTimeTask = new BukkitRunnable() {
            int remainingBreakTime = breaktime - shrinktime; // Shrink Time과 겹치는 시간 계산

            @Override
            public void run() {
                if (remainingBreakTime > 0) {
                    String actionBarMessage = String.format("Next phase in %d seconds", remainingBreakTime);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
                    }
                    remainingBreakTime--;
                } else {
                    this.cancel();
                    // Shrink Time 시작
                    startShrink(phaseKey, add, shrinktime, damage, index, phaseKeys);
                }
            }
        };

        breakTimeTask.runTaskTimer(plugin, 0L, 20L); // 1초마다 실행
        tasks.add(breakTimeTask);
    }

    private void startShrink(String phaseKey, int add, int shrinktime, double damage, int index, List<String> phaseKeys) {
        // 월드보더 크기 조절 명령어 실행
        String command = String.format("worldborder add %d %d", add, shrinktime);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        // 월드보더 데미지 설정 명령어 실행
        String damageCommand = String.format("worldborder damage amount %f", damage);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), damageCommand);

        moveCenter(shrinktime);

        String randomSpawnCommand = String.format("lc randomspawn special%d 500", index + 1);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), randomSpawnCommand);

        // Shrink Time 동안의 경고 메시지
        BukkitRunnable shrinkTimeTask = new BukkitRunnable() {
            int remainingShrinkTime = shrinktime;

            @Override
            public void run() {
                if (remainingShrinkTime > 0) {
                    // 진행 상태 계산 (0 ~ 100%)
                    int progress = (int) ((1 - (double) remainingShrinkTime / shrinktime) * 100);

                    // 경고 메시지에 진행 상태를 추가
                    String cautionMessage = String.format("§c§lCaution! The border is shrinking! §e(%d%%)", progress);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(cautionMessage));
                    }
                    remainingShrinkTime--;
                } else {
                    this.cancel();
                    // 다음 페이즈로 넘어감
                    executePhase(phaseKeys, index + 1);
                }
            }
        };

        shrinkTimeTask.runTaskTimer(plugin, 0L, 20L); // 1초마다 실행
        tasks.add(shrinkTimeTask);
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

        // 월드보더 설정 초기화
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(fixedCenterX, fixedCenterZ); // 센터 위치를 기본 위치로 변경
        worldBorder.setSize(500); // 월드보더 크기를 500으로 설정
        Bukkit.broadcastMessage("WorldBorder has been reset to the original center (-63, -113) and size (500).");

        // lc randomradius를 모두 0으로 설정
        for (int i = 1; i <= 5; i++) {
            String randomRadiusCommand = String.format("lc randomspawn special%d 0", i);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), randomRadiusCommand);
        }

        // 보스바 제거
        if (bossBar != null) {
            bossBar.removeAll(); // 보스바를 모든 플레이어로부터 제거
            bossBar = null; // 보스바 참조 제거
        }
    }

    private int rand(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}