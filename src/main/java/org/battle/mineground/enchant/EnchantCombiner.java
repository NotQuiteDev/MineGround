package org.battle.mineground.enchant;

import org.bukkit.configuration.file.FileConfiguration;
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

    // 인챈트 가능한 아이템과 인챈트 책을 합치는 메서드 (기존 코드)
    public boolean applyEnchantment(ItemStack item, ItemStack enchantBook) {
        // 두 번째 아이템이 인챈트 책이어야 함
        if (enchantBook.getType() != Material.ENCHANTED_BOOK) {
            return false; // 인챈트 책이 아니면 실패
        }

        // 인챈트 성공 확률 체크
        if (!attemptEnchant()) {
            return false;  // 실패 시 책 소모
        }

        // 인챈트 책의 메타데이터 가져오기
        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) enchantBook.getItemMeta();
        boolean appliedAny = false;  // 하나라도 적용되었는지 확인

        // 인챈트 책에 저장된 각 인챈트에 대해 적용 가능한지 확인
        for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int bookLevel = entry.getValue();

            // 해당 인챈트가 아이템에 적용 가능한지 확인
            if (enchantment.canEnchantItem(item)) {
                // 현재 아이템의 인챈트 레벨 확인
                int currentLevel = item.getEnchantmentLevel(enchantment);

                // 이미 최대 레벨에 도달했는지 확인
                if (currentLevel >= enchantment.getMaxLevel()) {
                    continue;  // 해당 인챈트는 이미 최대 레벨이므로 건너뜀
                }

                // 합산된 레벨 계산
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

        return appliedAny;  // 인챈트가 적용되었는지 여부 반환
    }

    // 인챈트 책끼리 합치는 메서드 (기존 코드)
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

        boolean anyChanges = false;  // 인챈트가 변화했는지 여부 확인

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

            // 변화가 있는 경우에만 인챈트 추가
            if (combinedLevel != level1) {
                combinedMeta.addStoredEnchant(enchantment, combinedLevel, true);
                anyChanges = true;  // 변화가 있었음을 표시
            } else {
                combinedMeta.addStoredEnchant(enchantment, level1, true);  // 그대로 복사
            }
        }

        // 두 번째 책의 인챈트를 추가 (이미 추가된 인챈트는 제외됨)
        for (Map.Entry<Enchantment, Integer> entry : meta2.getStoredEnchants().entrySet()) {
            if (!combinedMeta.hasStoredEnchant(entry.getKey())) {
                combinedMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                anyChanges = true;  // 변화가 있었음을 표시
            }
        }

        // 만약 아무런 변화가 없다면 null 반환 (책이 소모되지 않도록)
        if (!anyChanges) {
            return null;
        }

        combinedBook.setItemMeta(combinedMeta);
        return combinedBook;
    }
}