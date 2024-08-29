package org.battle.mineground;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.battle.mineground.elytra.ElytraCommand;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WorldBorderController implements Listener {

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
    private BukkitRunnable beamParticleTask;
    private boolean isGameRunning = false;
    private BukkitRunnable fireworkTask;
    private final ElytraCommand elytraCommand; // ElytraCommand 클래스 참조
    private int maxPlayers;
    // 나간 플레이어와 나간 시간 기록을 위한 맵
    private final Map<UUID, Long> playerQuitTimestamps = new HashMap<>();
    private final Map<UUID, Integer> quitTimers = new HashMap<>();  // 각 플레이어의 타이머 ID 저장
    private double stepX;
    private double stepZ;

    // 보스바를 반환하는 메서드 추가
    public BossBar getBossBar() {
        return bossBar;
    }

    // 생존자 수 접근자 메서드 (Getter and Setter)
    public int getSurvivingPlayers() {
        return survivingPlayers;
    }

    public void setSurvivingPlayers(int survivingPlayers) {
        this.survivingPlayers = survivingPlayers;
    }

    // 플레이어 나간 시간 기록을 위한 맵 접근자 메서드
    public Map<UUID, Long> getPlayerQuitTimestamps() {
        return playerQuitTimestamps;
    }


    public boolean isGameRunning() {
        return isGameRunning;
    }

    public void startGame() {
        isGameRunning = true;
        startPhases();
    }

    public void stopGame() {
        isGameRunning = false;
        stopPhases();
    }

    public WorldBorderController(JavaPlugin plugin) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld("world"); // 월드 이름을 필요에 따라 변경하세요
        this.config = plugin.getConfig();
        this.survivingPlayers = Bukkit.getOnlinePlayers().size();
        this.elytraCommand = new ElytraCommand((MineGround) plugin); // ElytraCommand 인스턴스 생성
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

        // 게임을 시작하므로 진행 상태를 true로 설정
        isGameRunning = true;
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
        double speed = plugin.getConfig().getDouble("player-walk-speed", 1.7);  // config에서 플레이어 속도를 가져옴

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                totalPlayers++;
                elytraCommand.giveSpecialElytra(player);

                // Essentials 명령어로 플레이어 속도 설정
                String command = "speed walk " + speed + " " + player.getName();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
        survivingPlayers = totalPlayers;
        maxPlayers = totalPlayers; // 게임 시작 시 고정된 최대 플레이어 수
        // 보스바 초기화
        bossBar = Bukkit.createBossBar("Survivors: " + survivingPlayers + "/" + maxPlayers, BarColor.GREEN, BarStyle.SOLID);
        bossBar.setVisible(true);

        // 모든 플레이어를 보스바에 추가
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // 사망 당시의 위치와 각도 저장
        Location deathLocation = player.getLocation();
        float pitch = deathLocation.getPitch();
        float yaw = deathLocation.getYaw();

        // 1틱 뒤에 플레이어를 리스폰 시킨 후 관전 모드로 변경
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.spigot().respawn();  // 플레이어를 즉시 리스폰

            // 리스폰 후 사망 위치로 이동 (pitch와 yaw 포함)
            player.teleport(new Location(deathLocation.getWorld(), deathLocation.getX(), deathLocation.getY(), deathLocation.getZ(), yaw, pitch));

            // 관전 모드로 변경
            player.setGameMode(GameMode.SPECTATOR);
        }, 1L);  // 1틱 지연 후 실행

        if (isGameRunning) {
            // 게임 진행 중일 때만 카운트 감소 및 보스바 업데이트
            if (survivingPlayers > 0) {
                survivingPlayers--;
            }

            // mg check 명령어 실행
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mg check");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // 플레이어가 서바이벌 모드일 경우 처리
        if (player.getGameMode() == GameMode.SURVIVAL && survivingPlayers > 0) {
            survivingPlayers--;
            playerQuitTimestamps.put(playerUUID, System.currentTimeMillis()); // 나간 시간 기록

            // 기존 타이머가 있다면 취소
            if (quitTimers.containsKey(playerUUID)) {
                Bukkit.getScheduler().cancelTask(quitTimers.get(playerUUID));  // 기존 타이머 취소
            }

            // 새로운 30초 타이머 설정
            int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                playerQuitTimestamps.remove(playerUUID);  // 30초 후 기록 제거
                quitTimers.remove(playerUUID);  // 타이머 ID 제거
            }, 30 * 20L).getTaskId();  // 30초 후 실행 (20L = 1초)

            // 타이머 ID 저장
            quitTimers.put(playerUUID, taskId);
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mg check");  // mg check 명령어 실행
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
        String respawnCommand = "spawnchests";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), respawnCommand);

        // 5. 모든 플레이어 서바이벌 모드로 변경 (gamemode survival *)
        String survivalCommand = "gamemode survival *";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), survivalCommand);
    }

    // 파티클 빔 생성 메소드
    public void spawnParticleBeam(World world, double x, double z) {
        beamParticleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (int y = 0; y <= world.getMaxHeight(); y += 5) { // Y축 간격을 늘려 파티클 수 줄이기
                    Location location = new Location(world, x, y, z);
                    world.spawnParticle(Particle.GLOW_SQUID_INK, location, 100, 0.1, 0.1, 0.1, 0, null, true);
                    // 파티클 수를 줄여서 성능 최적화
                }
            }
        };
        beamParticleTask.runTaskTimer(plugin, 0L, 20L); // 20틱(1초) 간격으로 파티클 생성
    }

    // 파티클 소환 취소 메소드
    public void cancelParticleBeam() {
        if (beamParticleTask != null) {
            beamParticleTask.cancel();
            beamParticleTask = null;
        }
    }

    private void teleportPlayers() {
        int yCoordinate = plugin.getConfig().getInt("teleport-y-coordinate");  // Y좌표를 config에서 불러옴

        for (Player player : Bukkit.getOnlinePlayers()) {
            double randomX = -313 + rand(25, 475);
            double randomZ = -363 + rand(25, 475);
            Location randomLocation = new Location(world, randomX, yCoordinate, randomZ);
            player.teleport(randomLocation);
            //player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 15 * 20, 1)); // 느린 낙하 효과 적용 (15초)
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

    // 클래스 필드로 데미지 루프를 관리하는 변수 추가
    private BukkitRunnable currentDamageTask;

    private void executePhase(List<String> phaseKeys, int index) {
        if (!isGameRunning()) {
            return;  // 게임이 진행 중이 아니면 실행하지 않음
        }

        if (index >= phaseKeys.size()) {
            return;  // 모든 페이즈가 완료되면 종료
        }

        String phaseKey = phaseKeys.get(index);
        int breaktime = config.getInt(phaseKey + ".breaktime");
        int add = config.getInt(phaseKey + ".add");
        int shrinktime = config.getInt(phaseKey + ".shrinktime");
        double damage = config.getDouble(phaseKey + ".damage");  // config에서 데미지 값 불러오기

        // 현재 WorldBorder 목표 크기 계산 (500 + 모든 add 값의 합)
        double targetBorderSize = calculateTargetBorderSize(phaseKeys, index);


        // 기존 데미지 루프가 실행 중이라면 취소
        if (currentDamageTask != null && !currentDamageTask.isCancelled()) {
            currentDamageTask.cancel();
        }

        // 새로운 데미지 루프 시작
        currentDamageTask = applyBorderDamage(damage);

        // Break Time 동안의 액션바 메시지
        BukkitRunnable breakTimeTask = new BukkitRunnable() {
            int remainingBreakTime = breaktime - shrinktime;

            @Override
            public void run() {
                if (!isGameRunning()) {
                    this.cancel(); // 게임이 중지되면 실행 중인 작업도 취소
                    return;
                }

                if (remainingBreakTime > 0) {
                    // 보스바 메시지 업데이트
                    String bossBarMessage = String.format("§r§ePhase %d §r§a%d초 후 자기장이 줄어듭니다. §r§f생존: §r%d", index, remainingBreakTime, survivingPlayers);
                    bossBar.setTitle(bossBarMessage);

                    // 보스바 진행률 업데이트 (시간이 줄어들수록 보스바의 진행률도 감소)
                    double progress = (double) remainingBreakTime / breaktime;  // 0.0 ~ 1.0 사이로 계산
                    bossBar.setProgress(progress);

                    // 남은 시간에 따른 보스바 색상 변화
                    if (remainingBreakTime > 60) {
                        bossBar.setColor(BarColor.GREEN);  // 안정적인 상태
                    } else if (remainingBreakTime > 30) {
                        bossBar.setColor(BarColor.YELLOW);  // 경고 신호
                    } else if (remainingBreakTime > 10) {
                        bossBar.setColor(BarColor.PURPLE);  // 위험 신호
                    } else {
                        bossBar.setColor(BarColor.RED);  // 매우 위험한 상태
                    }

                    // 플레이어가 WorldBorder 내에 있는지 확인하고, 밖에 있다면 명령어 실행
                    checkPlayersPosition(targetBorderSize, phaseKeys, index);

                    // 경계에 파티클 생성
                    createBorderParticles(targetBorderSize, phaseKeys, index);

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
        if (!isGameRunning()) {
            return;  // 게임이 진행 중이 아니면 실행하지 않음
        }

        // 월드보더 크기 조절 명령어 실행 (기존 로직 유지)
        String command = String.format("worldborder add %d %d", add, shrinktime);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        // 기존 데미지 루프가 실행 중이라면 취소
        if (currentDamageTask != null && !currentDamageTask.isCancelled()) {
            currentDamageTask.cancel();
        }
        // 현재 WorldBorder 목표 크기 계산 (500 + 모든 add 값의 합)
        double targetBorderSize = calculateTargetBorderSize(phaseKeys, index);


        // 새로운 데미지 루프 시작
        currentDamageTask = applyBorderDamage(damage);

        moveCenter(shrinktime);

        // Shrink Time 동안의 경고 메시지
        BukkitRunnable shrinkTimeTask = new BukkitRunnable() {
            int remainingShrinkTime = shrinktime;

            @Override
            public void run() {
                if (!isGameRunning()) {
                    this.cancel(); // 게임이 중지되면 실행 중인 작업도 취소
                    return;
                }

                if (remainingShrinkTime > 0) {
                    // 진행 상태 계산 (0 ~ 100%)
                    double progress = 1.0 - (double) remainingShrinkTime / shrinktime;  // 진행 상황을 0.0에서 1.0으로 계산

                    // 보스바 메시지 업데이트 (페이즈와 생존자 수 포함)
                    String bossBarMessage = String.format("§r§ePhase %d §r§a경계가 수축 중입니다! §r§f생존: §r%d", index, survivingPlayers);
                    bossBar.setTitle(bossBarMessage);

                    // 보스바 진행률 업데이트 (경계 수축에 따라 증가)
                    bossBar.setProgress(progress);

                    // 보스바 색상은 진행률에 따라 변경 (점점 더 위험한 색으로)
                    if (progress < 0.5) {
                        bossBar.setColor(BarColor.GREEN);  // 초기 안정 상태
                    } else if (progress < 0.8) {
                        bossBar.setColor(BarColor.YELLOW);  // 중간 경고
                    } else {
                        bossBar.setColor(BarColor.RED);  // 매우 위험한 상태
                    }

                    // 플레이어가 WorldBorder 내에 있는지 확인하고, 밖에 있다면 명령어 실행
                    checkPlayersPosition(targetBorderSize, phaseKeys, index);
                    // 경계에 파티클 생성
                    createBorderParticles(targetBorderSize, phaseKeys, index);

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

    // 월드보더 밖에 있는 플레이어에게 주기적으로 데미지 적용 (config에서 불러온 값 사용)
    private BukkitRunnable applyBorderDamage(double damagePerSecond) {
        BukkitRunnable damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isGameRunning()) {
                    this.cancel(); // 게임이 중지되면 실행 중인 작업도 취소
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    WorldBorder border = player.getWorld().getWorldBorder();
                    if (!border.isInside(player.getLocation())) {
                        player.damage(damagePerSecond); // 월드보더 밖에 있는 플레이어에게 config에서 불러온 데미지 적용
                    }
                }
            }
        };

        damageTask.runTaskTimer(plugin, 0L, 20L); // 매초마다 실행
        return damageTask; // 현재 실행 중인 데미지 작업을 반환
    }

    private void moveCenter(int shrinktime) {
        WorldBorder worldBorder = world.getWorldBorder();

        // 현재 페이즈에서 이동해야 할 거리 계산
        this.stepX = (totalDistanceX * shrinktime) / totalShrinkTime;
        this.stepZ = (totalDistanceZ * shrinktime) / totalShrinkTime;

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
        // 우승자를 위한 축하 파티 종료
        stopCelebration();

        // 실행 중인 모든 작업 취소
        for (BukkitRunnable task : tasks) {
            task.cancel();
        }
        tasks.clear();  // 작업 리스트 초기화

        // 파티클 빔 소환 취소
        cancelParticleBeam();

        // 월드보더 설정 초기화
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(fixedCenterX, fixedCenterZ); // 센터 위치를 기본 위치로 변경
        worldBorder.setSize(500); // 월드보더 크기를 500으로 설정
        Bukkit.broadcastMessage("WorldBorder has been reset to the original center (-63, -113) and size (500).");

        // 보스바 제거
        if (bossBar != null) {
            bossBar.removeAll(); // 보스바를 모든 플레이어로부터 제거
            bossBar = null; // 보스바 참조 제거
        }
    }

    public void checkForWinner() {
        if (survivingPlayers == 1) { // 생존자가 1명 남았을 때
            Player winner = null;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    winner = player;
                    break;
                }
            }

            if (winner != null) {
                // 우승자에게 축하 메시지 전송 및 효과
                String winnerMessage = String.format("§6§lCongratulations! %s is the last survivor and the WINNER!", winner.getName());
                Bukkit.broadcastMessage(winnerMessage);
                celebrateWinner(winner);

                // 게임 종료 상태로 변경
                isGameRunning = false; // 게임이 종료됨을 표시
            }
        }
    }


    private void celebrateWinner(Player winner) {
        // 모든 플레이어에게 우승자 메시지 타이틀로 표시
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle("§6§lWinner Winner Chicken Dinner!",
                    "§e" + winner.getName() + " is the last survivor!",
                    10, 70, 20); // 타이틀 표시 (10틱 딜레이, 70틱 표시, 20틱 페이드 아웃)
        }

        // 불꽃 발사
        fireworkTask = new BukkitRunnable() {
            @Override
            public void run() {
                // 게임이 종료된 상태라도 불꽃 발사를 계속
                for (int i = 0; i < random.nextInt(4) + 1; i++) {
                    Location location = winner.getLocation();

                    // 20-30 블록 랜덤 거리 설정
                    double offsetX = random.nextDouble() * 20 + 10 * (random.nextBoolean() ? 1 : -1);
                    double offsetZ = random.nextDouble() * 20 + 10 * (random.nextBoolean() ? 1 : -1);
                    Location fireworkLocation = location.clone().add(offsetX, 0, offsetZ);

                    Firework firework = fireworkLocation.getWorld().spawn(fireworkLocation, Firework.class);
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(randomFireworkEffect());
                    meta.setPower(3);  // 데미지를 주지 않도록 파워를 낮게 설정
                    firework.setFireworkMeta(meta);
                }
            }
        };

        // 불꽃을 랜덤하게 발사 (랜덤 시간 간격: 10틱 ~ 40틱)
        fireworkTask.runTaskTimer(plugin, 0L, random.nextInt(31) + 10); // 10~40틱 간격으로 실행
        Bukkit.getScheduler().runTaskLater(plugin, fireworkTask::cancel, 600L); // 30초 후 불꽃 중단

        // 파티클 효과 추가
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                // 게임이 종료된 상태라도 30초 동안 파티클 생성
                Location location = winner.getLocation();
                location.getWorld().spawnParticle(Particle.TOTEM, location, 200, 1, 1, 1, 0.1);
            }
        };

        // 파티클을 30초 동안 생성
        particleTask.runTaskTimer(plugin, 0L, 20L); // 20틱(1초) 간격으로 실행
        Bukkit.getScheduler().runTaskLater(plugin, particleTask::cancel, 600L); // 30초 후 파티클 중단
        // 20초 후 mg stop 명령어 실행
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mg stop");
        }, 400L); // 20초 후 실행
    }


    private FireworkEffect randomFireworkEffect() {
        FireworkEffect.Builder builder = FireworkEffect.builder();

        // 랜덤 색상 선택 (최소 2개의 색상)
        int colorCount = random.nextInt(3) + 2; // 2~4개의 색상
        for (int i = 0; i < colorCount; i++) {
            builder.withColor(Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        }

        // 랜덤 불꽃 타입 선택
        FireworkEffect.Type[] types = FireworkEffect.Type.values();
        builder.with(types[random.nextInt(types.length)]);

        // 랜덤으로 깜빡임과 꼬리 효과 추가
        if (random.nextBoolean()) {
            builder.withFlicker();
        }
        if (random.nextBoolean()) {
            builder.withTrail();
        }

        return builder.build();
    }

    private void stopCelebration() {
        if (fireworkTask != null) {
            fireworkTask.cancel(); // 폭죽 작업 중단
            fireworkTask = null;
        }

        if (particleTask != null) {
            particleTask.cancel(); // 파티클 작업 중단
            particleTask = null;
        }
    }

    // WorldBorder 목표 크기 계산 메소드
    private double calculateTargetBorderSize(List<String> phaseKeys, int index) {
        double targetBorderSize = 500;  // 기본 크기 500
        for (int i = 0; i <= index; i++) {
            String phaseKey = phaseKeys.get(i);
            int add = config.getInt(phaseKey + ".add");
            targetBorderSize += add;  // add 값은 음수로 설정하여 줄어들도록 구현되어야 함
        }
        return targetBorderSize;
    }

// 플레이어가 WorldBorder 내에 있는지 확인하는 메소드
    private void checkPlayersPosition(double targetBorderSize, List<String> phaseKeys, int currentIndex) {
        // 현재 월드보더의 실제 중심 좌표 계산
        double[] currentCenter = calculateCurrentCenter(phaseKeys, currentIndex);
        double currentCenterX = currentCenter[0];
        double currentCenterZ = currentCenter[1];

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location playerLocation = player.getLocation();
            double distanceX = Math.abs(playerLocation.getX() - currentCenterX);
            double distanceZ = Math.abs(playerLocation.getZ() - currentCenterZ);

            // 플레이어가 WorldBorder 밖에 있는 경우
            if (distanceX > targetBorderSize / 2 || distanceZ > targetBorderSize / 2) {
                if (player.getGameMode() == GameMode.SURVIVAL) {
                // showpointer 명령어 실행
                player.performCommand(String.format("showpointer %.1f 63 %.1f", randomCenterX, randomCenterZ));
                }
            }
        }
    }
    // 경계에 파티클 생성 메소드
// 경계에 파티클 생성 메소드
// 경계에 파티클 생성 메소드
    private void createBorderParticles(double targetBorderSize, List<String> phaseKeys, int currentIndex) {
        World world = Bukkit.getWorld("world");
        double halfSize = targetBorderSize / 2;

        // 현재 월드보더의 실제 중심 좌표 계산
        double[] currentCenter = calculateCurrentCenter(phaseKeys, currentIndex);
        double currentCenterX = currentCenter[0];
        double currentCenterZ = currentCenter[1];

        // 경계의 네모난 모서리 좌표 계산
        double minX = currentCenterX - halfSize;
        double maxX = currentCenterX + halfSize;
        double minZ = currentCenterZ - halfSize;
        double maxZ = currentCenterZ + halfSize;

        // 네 변을 따라 파티클 생성 (1블록 간격)
        for (double x = minX; x <= maxX; x++) {
            spawnParticleAtHighestBlock(world, x, minZ);
            spawnParticleAtHighestBlock(world, x, maxZ);
        }
        for (double z = minZ; z <= maxZ; z++) {
            spawnParticleAtHighestBlock(world, minX, z);
            spawnParticleAtHighestBlock(world, maxX, z);
        }
    }

    // 최상단 블록 위에 파티클 생성 메소드
    private void spawnParticleAtHighestBlock(World world, double x, double z) {
        // 해당 좌표의 최상단 블록을 찾음
        Location highestBlockLocation = world.getHighestBlockAt((int) x, (int) z).getLocation().add(0, 1, 0);

        // 해당 위치에 파티클 생성
        world.spawnParticle(Particle.FLAME, highestBlockLocation, 1, 0, 0, 0, 0);
    }

// 현재까지 이동한 거리를 계산하는 메소드
    private double[] calculateCurrentCenter(List<String> phaseKeys, int currentIndex) {
        // 현재까지의 shrinktime 누적 합 계산
        int elapsedShrinkTime = 0;
        for (int i = 0; i <= currentIndex; i++) {
            String phaseKey = phaseKeys.get(i);
            elapsedShrinkTime += config.getInt(phaseKey + ".shrinktime");
        }

        // 이동 비율 계산 (elapsedShrinkTime / totalShrinkTime)
        double ratio = (double) elapsedShrinkTime / totalShrinkTime;

        // 이동한 거리 계산
        double currentX = fixedCenterX + (totalDistanceX * ratio);
        double currentZ = fixedCenterZ + (totalDistanceZ * ratio);

        return new double[]{currentX, currentZ};  // 현재 센터 좌표 반환
    }



    public void startSpectatorParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 서버에 있는 모든 플레이어를 확인
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // 플레이어가 관전 모드인 경우 파티클을 생성
                    if (player.getGameMode() == GameMode.SPECTATOR) {
                        // 파티클 수를 줄이고 Y축을 1 높임, 더 자주 생성
                        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 1, 0.05, 0.05, 0.05, 0.02);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5L); // 매 5틱마다 실행 (0.25초마다)
    }

    private int rand(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}