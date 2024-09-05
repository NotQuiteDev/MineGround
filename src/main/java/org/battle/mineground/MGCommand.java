package org.battle.mineground;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class MGCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final WorldBorderController worldBorderController;
    private boolean isRunning = false;
    private boolean loopEnabled = false; // loop 기능 활성화 여부
    private BukkitRunnable loopTask;

    private boolean isCountdownActive = false;  // 타이머 상태 추적
    // 나간 플레이어와 나간 시간 기록을 위한 맵을 WorldBorderController에서 가져옴
    public MGCommand(JavaPlugin plugin, WorldBorderController worldBorderController) {
        this.plugin = plugin;
        this.worldBorderController = worldBorderController;
        // 이벤트 리스너 등록
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.isOp()) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("start")) {
                if (!worldBorderController.isGameRunning()) {  // 게임이 실행 중인지 확인
                    worldBorderController.startGame();  // 게임 시작
                    sender.sendMessage("World border adjustment started!");
                } else {
                    sender.sendMessage("World border adjustment is already running.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("stop")) {
                worldBorderController.stopGame();  // 게임 중지
                sender.sendMessage("World border adjustment stopped.");

                return true;
            } else if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (plugin instanceof MineGround) {
                    MineGround mineGround = (MineGround) plugin;
                    mineGround.reloadPlugin();  // 플러그인 리로드 처리
                }
                sender.sendMessage("Configuration reloaded!");
                return true;
            } else if (args[0].equalsIgnoreCase("load")) {
                SchematicLoader loader = new SchematicLoader();
                Location location = new Location(plugin.getServer().getWorld("world"), -313, 64, 136);
                // 1번째 스키메틱 로드
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    loader.loadSchematic("MHSpart1", location);
                    sender.sendMessage("MHSpart1.schematic has been loaded.");
                }, 0L); // 즉시 실행

                // 2번째 스키메틱 로드
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    loader.loadSchematic("MHSpart2", location);
                    sender.sendMessage("MHSpart2.schematic has been loaded.");
                }, 20L); // 1초 지연

                // 3번째 스키메틱 로드
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    loader.loadSchematic("MHSpart3", location);
                    sender.sendMessage("MHSpart3.schematic has been loaded.");
                }, 40L); // 2초 지연

                // 4번째 스키메틱 로드
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    loader.loadSchematic("MHSpart4", location);
                    sender.sendMessage("MHSpart4.schematic has been loaded.");
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "minecraft:kill @e[type=!minecraft:player]");
                }, 60L); // 3초 지연

                sender.sendMessage("MHS.schematic has been loaded at -313, 64, 136.");

                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "minecraft:kill @e[type=!minecraft:player]");
                sender.sendMessage("All entities except players have been killed.");
                return true;
            } else if (args[0].equalsIgnoreCase("check")) {
                if (worldBorderController.isGameRunning()) {  // 게임이 실행 중인지 확인
                    worldBorderController.checkForWinner();
                    sender.sendMessage("Winner check executed.");
                } else {
                    sender.sendMessage("The game is not running.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("loop")) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("true")) {
                        if (!loopEnabled) {
                            loopEnabled = true;
                            startLoopTask();  // 루프 작업 시작
                            sender.sendMessage("Auto-start loop enabled.");
                        } else {
                            sender.sendMessage("Auto-start loop is already enabled.");
                        }
                    } else if (args[1].equalsIgnoreCase("false")) {
                        if (loopEnabled) {
                            loopEnabled = false;
                            stopLoopTask();  // 루프 작업 중지
                            sender.sendMessage("Auto-start loop disabled.");
                        } else {
                            sender.sendMessage("Auto-start loop is already disabled.");
                        }
                    }
                } else {
                    sender.sendMessage("Usage: /mg loop <true|false>");
                }
                return true;
            }
        }
        return false;
    }

    private void startLoopTask() {
        loopTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (loopEnabled) {
                    if (!worldBorderController.isGameRunning() && !isCountdownActive) {
                        // 게임이 종료되었을 때 타이머 시작
                        startCountdown();
                    }
                } else {
                    this.cancel();  // 루프가 비활성화되면 작업 중지
                }
            }
        };

        loopTask.runTaskTimer(plugin, 0L, 20L); // 매 1초마다 실행
    }

    private void startCountdown() {
        isCountdownActive = true;
        int countdown = 60;

        new BukkitRunnable() {
            int timeLeft = countdown;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mg start");
                    isCountdownActive = false;
                    this.cancel();  // 타이머 종료 후 작업 중지
                    return;
                }

                if (timeLeft == 10) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mg load");
                }
                if (timeLeft == 20) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mg stop");
                }

                if (Bukkit.getOnlinePlayers().size() < 2) {
                    String actionBarMessage = "§cNot enough players. Countdown paused.";

                    // 모든 플레이어에게 액션바 메시지 전송
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
                    }

                    isCountdownActive = false;
                    this.cancel();  // 플레이어가 부족하면 타이머 중지
                    return;
                }

                String actionBarMessage = "§eNext game starts in " + timeLeft + " seconds!";
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
                }


                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 매 1초마다 실행
    }


    private void stopLoopTask() {
        if (loopTask != null) {
            loopTask.cancel();
            loopTask = null;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Long quitTime = worldBorderController.getPlayerQuitTimestamps().get(playerUUID);

        Bukkit.getLogger().info("Player " + player.getName() + " joined. Quit time: " + quitTime);



        worldBorderController.updateSurvivingPlayers();

        // 게임이 진행 중일 때만 보스바 업데이트 및 추가 작업 진행
        if (worldBorderController.isGameRunning()) {

            if (quitTime != null && System.currentTimeMillis() - quitTime <= 30 * 1000) {
                Bukkit.getLogger().info("Player rejoined within 30 seconds.");
                player.sendMessage("You have rejoined within 30 seconds. You are still a survivor!");
                player.setGameMode(GameMode.SURVIVAL);

                // BossBar에 플레이어 추가
                BossBar bossBar = worldBorderController.getBossBar();
                if (bossBar != null) {
                    bossBar.addPlayer(player);
                }

                // 30초 후에 quitTime 기록을 삭제
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    worldBorderController.getPlayerQuitTimestamps().remove(playerUUID);
                    Bukkit.getLogger().info("Player " + player.getName() + "'s quit time has been removed after 30 seconds.");
                }, 30 * 20L); // 30초 후 실행 (20L = 1초)
            } else {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage("The game is running, you are now in spectator mode.");

                // BossBar에 관전 모드 플레이어 추가
                BossBar bossBar = worldBorderController.getBossBar();
                if (bossBar != null) {
                    bossBar.addPlayer(player);
                }
            }
        } else {
            // 게임이 진행 중이지 않으면 loop 작업 실행
            if (loopEnabled) {
                startLoopTask();
            }
        }
    }
}