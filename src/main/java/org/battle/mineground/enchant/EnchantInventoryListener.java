package org.battle.mineground.enchant;

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

            // 클릭된 아이템이 인챈트 가능한 아이템이고, 커서에 있는 아이템이 인챈트 책인지 확인
            if (clickedItem != null && cursorItem != null &&
                    cursorItem.getType() == Material.ENCHANTED_BOOK) {

                // 인챈트 아이템과 인챈트 책을 합성
                boolean success = enchantCombiner.applyEnchantment(clickedItem, cursorItem);

                // 인챈트가 성공했으면 인챈트 소리 재생 및 책 소모
                if (success) {
                    player.getWorld().playSound(location, Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);  // 인챈트 소리 재생
                    event.setCurrentItem(clickedItem);  // 인챈트된 아이템 적용
                    event.setCursor(null);  // 인챈트 성공 시 책 소모
                } else {
                    // 인챈트 실패 시 실패 소리 재생
                    player.getWorld().playSound(location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);  // 실패 소리 재생
                }

                // 인챈트 실패 시 책은 유지됨
                event.setCancelled(true);
            }

            // 인챈트 책끼리 합치기
            if (clickedItem != null && cursorItem != null &&
                    clickedItem.getType() == Material.ENCHANTED_BOOK &&
                    cursorItem.getType() == Material.ENCHANTED_BOOK) {

                // 인챈트 책끼리 합성
                ItemStack combinedBook = enchantCombiner.combineEnchantedBooks(clickedItem, cursorItem);

                if (combinedBook != null) {
                    // 합쳐진 인챈트 책을 클릭된 자리에 적용
                    event.setCurrentItem(combinedBook);
                    event.setCursor(null);  // 손에 들고 있던 책은 사라짐

                    // 인챈트 소리 재생 (성공)
                    player.getWorld().playSound(location, Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);  // 인챈트 소리 재생

                    // 기본 클릭 동작 취소
                    event.setCancelled(true);
                } else {
                    // 합치기 실패 시 실패 소리 재생 (책이 합쳐지지 않았을 경우)
                    player.getWorld().playSound(location, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);  // 실패 소리 재생
                }
            }
        }
    }
}