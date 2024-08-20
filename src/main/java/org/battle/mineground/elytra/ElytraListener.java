package org.battle.mineground.elytra;

import org.battle.mineground.MineGround;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ElytraListener implements Listener {

    private final MineGround plugin;

    public ElytraListener(MineGround plugin) {
        this.plugin = plugin;
    }

    // 플레이어가 이동할 때 Y좌표를 확인
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        double minY = plugin.getConfig().getDouble("elytra-remove-y"); // config에서 Y좌표 값 불러오기

        // 플레이어의 Y좌표가 설정된 값 이하일 경우
        if (player.getLocation().getY() <= minY) {
            // Elytra가 있는지 확인하고 제거
            removeSpecialElytra(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack currentItem = event.getCurrentItem();
        if (isNamedElytra(currentItem)) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(ChatColor.RED + "You can't move this special Elytra!");
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (isNamedElytra(droppedItem)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You can't drop this special Elytra!");
        }
    }

    private boolean isNamedElytra(ItemStack item) {
        if (item != null && item.getType() == Material.ELYTRA) {
            ItemMeta meta = item.getItemMeta();
            String configName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("elytra-name"));
            return meta != null && configName.equals(meta.getDisplayName());
        }
        return false;
    }

    // Elytra 제거 및 느린 낙하 효과 부여
    private void removeSpecialElytra(Player player) {
        String configName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("elytra-name"));

        // 플레이어가 착용한 Elytra를 검사하여 제거
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null && chestplate.getType() == Material.ELYTRA && configName.equals(chestplate.getItemMeta().getDisplayName())) {
            player.getInventory().setChestplate(null); // Elytra를 착용 슬롯에서 제거
            player.sendMessage(ChatColor.RED + "Your Special Elytra has been removed!");

            // Elytra 제거 후 느린 낙하 효과 부여
            giveSlowFallingEffect(player);
            return;
        }

        // 인벤토리 전체를 검사하여 Elytra를 제거
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.ELYTRA && configName.equals(item.getItemMeta().getDisplayName())) {
                player.getInventory().remove(item);
                player.sendMessage(ChatColor.RED + "Your Special Elytra has been removed!");

                // Elytra 제거 후 느린 낙하 효과 부여
                giveSlowFallingEffect(player);
                break;
            }
        }
    }

    // 느린 낙하 효과 부여
    private void giveSlowFallingEffect(Player player) {
        long slowFallingDuration = plugin.getConfig().getLong("slow-falling-duration") * 20L; // config에서 느린 낙하 지속 시간 불러오기 (초 -> 틱 변환)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (int) slowFallingDuration, 1)); // 느린 낙하 효과 부여
    }
}