package org.battle.mineground.arrow_switcher;

import org.bukkit.Material;
import org.bukkit.entity.TippedArrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.stream.Collectors;

public class ArrowUtils {

    // 화살 종류 확인 유틸리티 메서드
    public static boolean isArrow(ItemStack item) {
        return item.getType() == Material.ARROW ||
                item.getType() == Material.TIPPED_ARROW ||
                item.getType() == Material.SPECTRAL_ARROW;
    }

    // Tipped Arrow의 포션 효과를 문자열로 반환하는 메서드
    public static String getPotionEffectsAsString(ItemStack item) {
        if (item.getType() != Material.TIPPED_ARROW) {
            return "None";
        }

        // ItemStack에서 PotionMeta를 사용해 포션 효과를 가져옴
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null || meta.getCustomEffects().isEmpty()) {
            return "None";
        }

        List<PotionEffect> effects = meta.getCustomEffects();
        return effects.stream()
                .map(effect -> effect.getType().getName() + " (Amplifier: " + effect.getAmplifier() + ", Duration: " + effect.getDuration() + ")")
                .collect(Collectors.joining(", "));
    }
}