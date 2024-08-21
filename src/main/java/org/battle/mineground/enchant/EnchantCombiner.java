package org.battle.mineground.enchant;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.Map;
import java.util.Random;

public class EnchantCombiner {

    private final Random random = new Random();
    private final int successRate;

    // 생성자에서 성공 확률을 전달받음
    public EnchantCombiner(int successRate) {
        this.successRate = successRate;  // 성공 확률 저장
    }

    // 인챈트 성공 여부를 결정하는 메서드 (확률 적용)
    public boolean attemptEnchant() {
        return random.nextInt(100) < successRate;  // 성공 여부 반환
    }

    // 두 인챈트 책을 합칠 수 있는지 여부를 판단하는 메서드 (성공 확률이 아닌 실제 가능 여부만 판단)
    public boolean canCombineEnchantedBooks(ItemStack book1, ItemStack book2) {
        if (book1.getType() != Material.ENCHANTED_BOOK || book2.getType() != Material.ENCHANTED_BOOK) {
            return false;
        }

        EnchantmentStorageMeta meta1 = (EnchantmentStorageMeta) book1.getItemMeta();
        EnchantmentStorageMeta meta2 = (EnchantmentStorageMeta) book2.getItemMeta();

        // 책끼리 합칠 수 있는지 확인 (동일한 인챈트가 있는지 확인)
        for (Map.Entry<Enchantment, Integer> entry : meta1.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (meta2.hasStoredEnchant(enchantment)) {
                // 동일한 인챈트가 있다면 합칠 수 있음
                return true;
            }
        }
        return false;  // 합칠 수 있는 인챈트가 없으면 false 반환
    }

    // 인챈트 가능한 아이템과 인챈트 책을 합치는 메서드
    public boolean applyEnchantment(ItemStack item, ItemStack enchantBook) {
        if (enchantBook.getType() != Material.ENCHANTED_BOOK) {
            return false;  // 인챈트 책이 아니면 실패
        }

        // 성공 여부를 한 번만 체크
        boolean success = attemptEnchant();
        if (!success) {
            return false;  // 전체 인챈트 작업 실패
        }

        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) enchantBook.getItemMeta();
        boolean appliedAny = false;

        for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int bookLevel = entry.getValue();

            if (enchantment.canEnchantItem(item)) {
                int currentLevel = item.getEnchantmentLevel(enchantment);

                // 이미 최대 레벨에 도달했는지 확인
                if (currentLevel < enchantment.getMaxLevel()) {
                    int combinedLevel = currentLevel + bookLevel;

                    // 합산된 레벨이 해당 인챈트의 최대 레벨을 초과하지 않도록 제한
                    int maxLevel = enchantment.getMaxLevel();
                    if (combinedLevel > maxLevel) {
                        combinedLevel = maxLevel;
                    }

                    // 새로운 레벨로 인챈트 추가
                    item.addEnchantment(enchantment, combinedLevel);
                    appliedAny = true;  // 적어도 하나의 인챈트가 적용되었음을 표시
                }
            }
        }

        return appliedAny;  // 적어도 하나의 인챈트가 적용되었는지 여부 반환
    }


    // 인챈트 책끼리 합치는 메서드
    public ItemStack combineEnchantedBooks(ItemStack book1, ItemStack book2) {
        if (book1.getType() != Material.ENCHANTED_BOOK || book2.getType() != Material.ENCHANTED_BOOK) {
            return null; // 인챈트 책이 아니면 null 반환
        }

        EnchantmentStorageMeta meta1 = (EnchantmentStorageMeta) book1.getItemMeta();
        EnchantmentStorageMeta meta2 = (EnchantmentStorageMeta) book2.getItemMeta();

        ItemStack combinedBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta combinedMeta = (EnchantmentStorageMeta) combinedBook.getItemMeta();
        boolean anyChanges = false;

        // 첫 번째 책의 인챈트를 복사하고, 동일한 인챈트가 있으면 레벨을 합산
        for (Map.Entry<Enchantment, Integer> entry : meta1.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level1 = entry.getValue();
            int level2 = meta2.getStoredEnchantLevel(enchantment);

            int combinedLevel = level1 + level2;

            int maxLevel = enchantment.getMaxLevel();
            if (combinedLevel > maxLevel) {
                combinedLevel = maxLevel;
            }

            if (combinedLevel != level1) {
                combinedMeta.addStoredEnchant(enchantment, combinedLevel, true);
                anyChanges = true;
            } else {
                combinedMeta.addStoredEnchant(enchantment, level1, true);
            }
        }

        // 두 번째 책의 인챈트를 추가 (이미 추가된 인챈트는 제외됨)
        for (Map.Entry<Enchantment, Integer> entry : meta2.getStoredEnchants().entrySet()) {
            if (!combinedMeta.hasStoredEnchant(entry.getKey())) {
                combinedMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                anyChanges = true;
            }
        }

        // 아무 변화가 없다면 null 반환
        if (!anyChanges) {
            return null;
        }

        // 성공 확률 체크
        if (!attemptEnchant()) {
            return null;  // 인챈트 실패 시 null 반환
        }

        combinedBook.setItemMeta(combinedMeta);
        return combinedBook;
    }

    public boolean canApplyEnchantment(ItemStack item, ItemStack enchantBook) {
        // 인챈트 책이어야 함
        if (enchantBook.getType() != Material.ENCHANTED_BOOK) {
            return false;
        }

        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) enchantBook.getItemMeta();
        boolean canApply = false;  // 적용 가능한 인챈트가 있는지 여부를 추적

        // 인챈트 책에 저장된 각 인챈트에 대해 적용 가능한지 확인
        for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int bookLevel = entry.getValue();

            // 해당 인챈트가 아이템에 적용 가능한지 확인
            if (enchantment.canEnchantItem(item)) {
                // 현재 아이템의 인챈트 레벨 확인
                int currentLevel = item.getEnchantmentLevel(enchantment);

                // 이미 최대 레벨에 도달하지 않았는지 확인
                if (currentLevel < enchantment.getMaxLevel()) {
                    canApply = true;  // 이 인챈트는 적용 가능
                }
            }
        }

        return canApply;  // 하나라도 적용 가능하다면 true 반환
    }

    public String getInapplicableReason(ItemStack item, ItemStack enchantBook) {
        if (enchantBook.getType() != Material.ENCHANTED_BOOK) {
            return "이 아이템은 인챈트 책이 아닙니다.";
        }

        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) enchantBook.getItemMeta();
        boolean atLeastOneApplicable = false;

        for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int bookLevel = entry.getValue();

            if (enchantment.canEnchantItem(item)) {
                int currentLevel = item.getEnchantmentLevel(enchantment);

                // 이미 최대 레벨에 도달하지 않았는지 확인
                if (currentLevel < enchantment.getMaxLevel()) {
                    atLeastOneApplicable = true;  // 적어도 하나는 적용 가능
                }
            }
        }

        if (!atLeastOneApplicable) {
            return "모든 인챈트가 이미 최대 레벨에 도달했거나, 이 아이템에 적용할 수 없습니다.";
        }

        return null;  // 적어도 하나의 인챈트는 적용 가능하므로 null 반환
    }
}