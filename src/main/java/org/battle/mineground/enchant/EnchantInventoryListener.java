package org.battle.mineground.enchant;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.Material;

public class EnchantInventoryListener implements Listener {

    private final EnchantCombiner enchantCombiner;

    public EnchantInventoryListener(EnchantCombiner enchantCombiner) {
        this.enchantCombiner = enchantCombiner;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 우클릭 확인
        if (event.getClick() == ClickType.RIGHT) {
            ItemStack clickedItem = event.getCurrentItem();  // 인벤토리에서 클릭된 아이템
            ItemStack cursorItem = event.getCursor();  // 플레이어가 들고 있는 아이템 (커서에 있는 아이템)

            Player player = (Player) event.getWhoClicked();  // 이벤트를 발생시킨 플레이어
            Location location = player.getLocation();  // 플레이어 위치

            // 1. 인챈트 책끼리 합치기
            if (clickedItem != null && cursorItem != null &&
                    clickedItem.getType() == Material.ENCHANTED_BOOK &&
                    cursorItem.getType() == Material.ENCHANTED_BOOK) {

                // 합칠 수 있는지 확인 (실제 합칠 수 있는지 여부만 판단)
                boolean canCombine = enchantCombiner.canCombineEnchantedBooks(clickedItem, cursorItem);

                if (canCombine) {
                    // 성공 확률 계산 (합칠 수 있는 경우에만 확률 적용)
                    boolean success = enchantCombiner.attemptEnchant();

                    if (success) {
                        // 성공 시 책 합치기
                        ItemStack combinedBook = enchantCombiner.combineEnchantedBooks(clickedItem, cursorItem);
                        event.setCurrentItem(combinedBook);
                        event.setCursor(null);  // 손에 들고 있던 책은 사라짐

                        // 인챈트 소리 재생 (성공)
                        player.getWorld().playSound(location, Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                        player.sendMessage(ChatColor.GREEN + "성공! 인챈트가 합쳐졌습니다.");
                    } else {
                        // 실패 시 실패 소리 재생 및 손에 있는 책 제거
                        player.getWorld().playSound(location, Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
                        event.setCursor(null);  // 손에 들고 있던 책을 제거
                        player.sendMessage(ChatColor.RED + "실패! 인챈트 합치기에 실패했습니다. 손에 있던 책이 파괴되었습니다.");
                    }
                } else {
                    // 합칠 수 없는 경우 (레벨이 최대이거나 다른 이유로)
                    player.getWorld().playSound(location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(ChatColor.YELLOW + "시도할 수 없음: 이 두 책을 합칠 수 없습니다.");
                }

                // 이벤트 종료
                event.setCancelled(true);
                return;
            }

            // 2. 아이템에 인챈트 적용
            if (clickedItem != null && cursorItem != null &&
                    cursorItem.getType() == Material.ENCHANTED_BOOK) {

                // 인챈트 아이템과 인챈트 책을 합칠 수 있는지 확인
                String reason = enchantCombiner.getInapplicableReason(clickedItem, cursorItem);

                if (reason == null) {
                    // 성공 확률 계산 (합칠 수 있는 경우에만 확률 적용)
                    boolean success = enchantCombiner.attemptEnchant();

                    if (success) {
                        // 인챈트 아이템 적용
                        enchantCombiner.applyEnchantment(clickedItem, cursorItem);
                        player.getWorld().playSound(location, Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);  // 인챈트 소리 재생
                        event.setCurrentItem(clickedItem);  // 인챈트된 아이템 적용
                        event.setCursor(null);  // 인챈트 성공 시 책 소모

                        // 플레이어에게 성공 메시지 전송
                        player.sendMessage(ChatColor.GREEN + "성공! 인챈트가 적용되었습니다.");
                    } else {
                        // 인챈트 실패 시 모루 실패 소리 재생 및 손에 있는 책 제거
                        player.getWorld().playSound(location, Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
                        event.setCursor(null);  // 손에 들고 있던 책을 제거
                        player.sendMessage(ChatColor.RED + "실패! 인챈트 적용에 실패했습니다. 손에 있던 책이 파괴되었습니다.");
                    }
                } else {
                    // 인챈트를 적용할 수 없는 경우 실패 소리 재생
                    player.getWorld().playSound(location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    player.sendMessage(ChatColor.YELLOW + "시도할 수 없음: " + reason);
                }

                // 이벤트 종료
                event.setCancelled(true);
            }
        }
    }
}