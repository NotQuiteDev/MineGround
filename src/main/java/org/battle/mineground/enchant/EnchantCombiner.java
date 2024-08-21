package org.battle.mineground.enchant;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.Map;

public class EnchantCombiner {

    // 인챈트 책끼리 합치는 메서드
    // 인챈트 책끼리 합치는 메서드
    public ItemStack combineEnchantedBooks(ItemStack book1, ItemStack book2) {
        // 두 아이템 모두 인챈트 책이어야 함
        if (book1.getType() != Material.ENCHANTED_BOOK || book2.getType() != Material.ENCHANTED_BOOK) {
            return null; // 인챈트 책이 아니면 null 반환
        }

        // 두 책의 메타데이터 가져오기
        EnchantmentStorageMeta meta1 = (EnchantmentStorageMeta) book1.getItemMeta();
        EnchantmentStorageMeta meta2 = (EnchantmentStorageMeta) book2.getItemMeta();

        // 새로 합친 인챈트 책 생성
        ItemStack combinedBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta combinedMeta = (EnchantmentStorageMeta) combinedBook.getItemMeta();

        // 첫 번째 책의 인챈트를 복사하고, 동일한 인챈트가 있으면 레벨을 합산
        for (Map.Entry<Enchantment, Integer> entry : meta1.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level1 = entry.getValue();
            int level2 = meta2.getStoredEnchantLevel(enchantment);

            // 합산된 레벨 계산
            int combinedLevel = level1 + level2;

            // 합산된 레벨이 해당 인챈트의 최대 레벨을 초과하지 않도록 제한
            int maxLevel = enchantment.getMaxLevel();
            if (combinedLevel > maxLevel) {
                combinedLevel = maxLevel;
            }

            // 새로운 레벨로 인챈트 추가
            combinedMeta.addStoredEnchant(enchantment, combinedLevel, true);
        }

        // 두 번째 책의 인챈트를 추가 (이미 추가된 인챈트는 제외됨)
        for (Map.Entry<Enchantment, Integer> entry : meta2.getStoredEnchants().entrySet()) {
            if (!combinedMeta.hasStoredEnchant(entry.getKey())) {
                combinedMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        combinedBook.setItemMeta(combinedMeta);
        return combinedBook;
    }

    // 인챈트 가능한 아이템과 인챈트 책을 합치는 메서드
    public boolean applyEnchantment(ItemStack item, ItemStack enchantBook) {
        // 두 번째 아이템이 인챈트 책이어야 함
        if (enchantBook.getType() != Material.ENCHANTED_BOOK) {
            return false; // 인챈트 책이 아니면 실패
        }

        // 인챈트 책의 메타데이터 가져오기
        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) enchantBook.getItemMeta();
        boolean applied = false;

        // 인챈트 책에 저장된 각 인챈트에 대해 적용 가능한지 확인
        for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int bookLevel = entry.getValue();

            // 해당 인챈트가 아이템에 적용 가능한지 확인
            if (enchantment.canEnchantItem(item)) {
                // 현재 아이템의 인챈트 레벨 확인
                int currentLevel = item.getEnchantmentLevel(enchantment);

                // 합산된 레벨 계산
                int combinedLevel = currentLevel + bookLevel;

                // 합산된 레벨이 해당 인챈트의 최대 레벨을 초과하지 않도록 제한
                int maxLevel = enchantment.getMaxLevel();
                if (combinedLevel > maxLevel) {
                    combinedLevel = maxLevel;
                }

                // 새로운 레벨로 인챈트 추가
                item.addEnchantment(enchantment, combinedLevel);
                applied = true;
            }
        }

        return applied; // 인챈트가 적용되었는지 여부 반환
    }
}