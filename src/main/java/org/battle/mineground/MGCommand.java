package org.battle.mineground;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MGCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final WorldBorderController worldBorderController;
    private boolean isRunning = false;

    public MGCommand(JavaPlugin plugin, WorldBorderController worldBorderController) {
        this.plugin = plugin;
        this.worldBorderController = worldBorderController;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 명령어를 실행한 사용자가 플레이어인지 확인
        if (sender instanceof Player) {
            Player player = (Player) sender;
            // OP 권한이 있는지 확인
            if (!player.isOp()) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("start")) {
                if (!isRunning) {
                    worldBorderController.startPhases();
                    isRunning = true;
                    sender.sendMessage("World border adjustment started!");
                } else {
                    sender.sendMessage("World border adjustment is already running.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("stop")) {
                if (isRunning) {
                    worldBorderController.stopPhases();
                    isRunning = false;
                    sender.sendMessage("World border adjustment stopped.");
                } else {
                    sender.sendMessage("World border adjustment is not running.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();

                // 설정 값을 다시 로드하여 메모리에 반영
                if (plugin instanceof MineGround) {
                    ((MineGround) plugin).loadConfigValues();
                }

                sender.sendMessage("Configuration reloaded!");
                return true;
            }
        }
        return false;
    }
}
