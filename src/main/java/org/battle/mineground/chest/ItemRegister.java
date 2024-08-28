package org.battle.mineground.chest;


import org.battle.mineground.MineGround;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemRegister implements TabExecutor {

    private final MineGround plugin;

    public ItemRegister(MineGround plugin) {
        this.plugin = plugin;
    }

    // 명령어 처리
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;

        // /registeritem 명령어 처리
        if (label.equalsIgnoreCase("registeritem")) {
            String itemName = null;

            // 아이템 이름이 주어지지 않았을 때 손에 든 아이템 이름으로 설정
            if (args.length < 1) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName()) {
                    itemName = itemInHand.getItemMeta().getDisplayName();
                } else {
                    itemName = itemInHand.getType().toString();
                }
            } else {
                itemName = args[0];
            }

            registerItem(player, itemName);
            return true;
        }

        // /spawnitem 명령어 처리
        if (label.equalsIgnoreCase("spawnitem")) {
            String itemName = null;

            // 아이템 이름이 주어지지 않았을 때 손에 든 아이템 이름으로 설정
            if (args.length < 1) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName()) {
                    itemName = itemInHand.getItemMeta().getDisplayName();
                } else {
                    itemName = itemInHand.getType().toString();
                }
            } else {
                itemName = args[0];
            }

            spawnItem(player, itemName);
            return true;
        }

        return false;
    }

    // 플레이어가 손에 든 아이템을 등록
    private void registerItem(Player player, String itemName) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            player.sendMessage("손에 아이템을 들고 있어야 합니다!");
            return;
        }

        if (itemName == null || itemName.isEmpty()) {
            player.sendMessage("아이템 이름을 입력하세요: /registeritem <itemName>");
            return;
        }

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection dummyData = config.getConfigurationSection("chest_contents.dummydata");
        if (dummyData == null) {
            dummyData = config.createSection("chest_contents.dummydata");
        }

        // Bukkit의 set 메서드를 사용해 아이템을 직렬화하여 저장
        dummyData.set(itemName + ".item", item);

        plugin.saveConfig();
        player.sendMessage(itemName + " 아이템이 성공적으로 등록되었습니다.");
    }


    // config.yml에서 아이템을 소환하여 플레이어 인벤토리에 추가
    private void spawnItem(Player player, String itemName) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection dummyData = config.getConfigurationSection("chest_contents.dummydata");

        if (dummyData == null) {
            player.sendMessage("아이템 데이터를 찾을 수 없습니다. 설정 파일에 데이터가 없을 수 있습니다.");
            return;
        }

        if (itemName == null || itemName.isEmpty()) {
            player.sendMessage("아이템 이름을 입력하세요: /spawnitem <itemName>");
            return;
        }

        // 설정 파일에서 아이템 불러오기
        ItemStack item = dummyData.getItemStack(itemName + ".item"); // .item 경로에서 불러옴

        if (item == null) {
            player.sendMessage(itemName + " 아이템 데이터를 찾을 수 없습니다.");
            return;
        }

        // 플레이어에게 아이템 지급
        player.getInventory().addItem(item);
        player.sendMessage(itemName + " 아이템이 소환되었습니다.");
    }

    // 명령어 자동 완성 기능
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("spawnitem")) {
            if (args.length == 1) {
                FileConfiguration config = plugin.getConfig();
                ConfigurationSection dummyData = config.getConfigurationSection("chest_contents.dummydata");

                if (dummyData != null) {
                    completions.addAll(dummyData.getKeys(false)); // dummydata에 등록된 아이템 이름을 자동 완성 제안
                }
            }
        }
        return completions;
    }
}