package org.battle.mineground.arrow_switcher;

import org.bukkit.Material;
import org.bukkit.entity.TippedArrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.stream.Collectors;

public class ArrowUtils {

    // 화살 및 포션 종류 확인 유틸리티 메서드
    public static boolean isSwitchableItem(ItemStack item) {
        return item.getType() == Material.ARROW ||
                item.getType() == Material.TIPPED_ARROW ||
                item.getType() == Material.SPECTRAL_ARROW ||
                item.getType() == Material.SPLASH_POTION ||
                item.getType() == Material.LINGERING_POTION;
    }

    // 포션 효과를 문자열로 반환하는 메서드 (Tipped Arrow, Splash Potion, Lingering Potion 범용)
    public static String getPotionEffectsAsString(ItemStack item) {
        if (item.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            if (meta == null || meta.getCustomEffects().isEmpty()) {
                return "None";
            }

            return meta.getCustomEffects().stream()
                    .map(effect -> effect.getType().getName() + " (Amplifier: " + effect.getAmplifier() + ", Duration: " + effect.getDuration() + ")")
                    .collect(Collectors.joining(", "));
        }
        return "None";
    }
}