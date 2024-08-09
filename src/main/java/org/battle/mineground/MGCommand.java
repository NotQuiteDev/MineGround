package org.battle.mineground;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
            }
        }
        return false;
    }
}