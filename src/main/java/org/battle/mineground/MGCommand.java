package org.battle.mineground;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MGCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final WorldBorderController worldBorderController;
    private boolean isRunning = false;
    private boolean loopEnabled = false; // loop 기능 활성화 여부
    private BukkitRunnable loopTask;

    private boolean isCountdownActive = false;  // 타이머 상태 추적

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
                if (worldBorderController.isGameRunning()) {  // 게임이 실행 중인지 확인
                    worldBorderController.stopGame();  // 게임 중지
                    sender.sendMessage("World border adjustment stopped.");
                } else {
                    sender.sendMessage("World border adjustment is not running.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                if (plugin instanceof MineGround) {
                    ((MineGround) plugin).loadConfigValues();
                }
                sender.sendMessage("Configuration reloaded!");
                return true;
            } else if (args[0].equalsIgnoreCase("load")) {
                SchematicLoader loader = new SchematicLoader();
                Location location = new Location(plugin.getServer().getWorld("world"), -313, 64, 136);
                loader.loadSchematic("MHS", location);
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

                if (Bukkit.getOnlinePlayers().size() < 2) {
                    Bukkit.broadcastMessage("§cNot enough players. Countdown paused.");
                    isCountdownActive = false;
                    this.cancel();  // 플레이어가 부족하면 타이머 중지
                    return;
                }

                String actionBarMessage = "§eNext game starts in " + timeLeft + " seconds!";
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
                }

                if (timeLeft == 30) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mg stop");
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
        if (worldBorderController.isGameRunning()) {
            Player player = event.getPlayer();
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("The game is running, you are now in spectator mode.");
        } else if (loopEnabled) { // 루프가 활성화된 상태에서 새로운 플레이어가 접속하면 타이머 체크
            startLoopTask();
        }
    }
}