package org.battle.mineground.elytra;

import org.battle.mineground.MineGround;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ElytraCommand implements CommandExecutor {

    private final MineGround plugin;

    public ElytraCommand(MineGround plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            giveSpecialElytra(player);
            return true;
        }
        return false;
    }

    public void giveSpecialElytra(Player player) {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        String elytraName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("elytra-name"));
        meta.setDisplayName(elytraName);
        elytra.setItemMeta(meta);
        player.getInventory().addItem(elytra);

        player.sendMessage(ChatColor.GREEN + "You have been given a Special Elytra!");
    }
}