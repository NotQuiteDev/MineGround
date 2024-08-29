package org.battle.mineground.arrow_switcher;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ArrowSwitcherCommand implements CommandExecutor {

    // 플레이어의 마지막 화살과 포션을 저장할 맵
    private final HashMap<UUID, Integer> lastArrowIndexMap = new HashMap<>();
    private final HashMap<UUID, Integer> lastPotionIndexMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // 메인 핸드에 있는 아이템이 화살 또는 포션인지 확인
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            if (!ArrowUtils.isSwitchableItem(mainHandItem)) {
                player.sendMessage("You need to hold a switchable item (Arrow or Potion) in your main hand.");
                return true;
            }

            // 아이템이 화살인지 포션인지 확인하여 교체 로직 실행
            if (isArrow(mainHandItem)) {
                switchArrow(player);
            } else if (isPotion(mainHandItem)) {
                switchPotion(player);
            }
            return true;
        }
        sender.sendMessage("This command can only be used by players.");
        return false;
    }

    private void switchArrow(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack currentArrow = player.getInventory().getItemInMainHand(); // 메인 핸드에 있는 화살
        UUID playerUUID = player.getUniqueId();

        // 인벤토리 내 화살 종류와 위치 수집 (메인 핸드 제외)
        List<Integer> arrowSlots = new ArrayList<>();
        int mainHandSlot = player.getInventory().getHeldItemSlot(); // 메인 핸드 슬롯 번호

        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && isArrow(item) && i != mainHandSlot) {
                arrowSlots.add(i);
            }
        }

        // 화살이 없거나 1종류만 있는 경우 순환할 필요 없음
        if (arrowSlots.size() <= 0) {
            player.sendMessage("No other arrows to switch to.");
            return;
        }

        // 이전 화살의 인덱스 확인
        int lastArrowIndex = lastArrowIndexMap.getOrDefault(playerUUID, -1);

        // 첫 명령어 실행 시: 첫 번째 화살로 설정
        if (lastArrowIndex == -1) {
            lastArrowIndex = 0;
        }

        // 다음 화살 슬롯으로 이동 (리스트의 마지막 화살이라면 첫 번째로 돌아옴)
        int nextIndex = (lastArrowIndex + 1) % arrowSlots.size();
        int nextSlot = arrowSlots.get(nextIndex);

        // 메인 핸드 화살과 인벤토리의 다음 화살 교체
        ItemStack nextArrow = inventory[nextSlot];
        player.getInventory().setItem(mainHandSlot, nextArrow); // 메인 핸드에 새로운 화살 배치
        player.getInventory().setItem(nextSlot, currentArrow);  // 현재 메인 핸드 화살을 인벤토리로 이동

        // 교체된 화살의 인덱스를 저장 (이제 교체된 화살이 lastArrow가 됨)
        lastArrowIndexMap.put(playerUUID, nextIndex);

        // 메시지 출력 - 교체된 화살의 이름을 출력
        player.sendMessage("Switched to " + (nextArrow.getItemMeta() != null && nextArrow.getItemMeta().hasDisplayName() ? nextArrow.getItemMeta().getDisplayName() : nextArrow.getType().name()));
    }

    private void switchPotion(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack currentPotion = player.getInventory().getItemInMainHand(); // 메인 핸드에 있는 포션
        UUID playerUUID = player.getUniqueId();

        // 인벤토리 내 포션 종류와 위치 수집 (메인 핸드 제외)
        List<Integer> potionSlots = new ArrayList<>();
        int mainHandSlot = player.getInventory().getHeldItemSlot(); // 메인 핸드 슬롯 번호

        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && isPotion(item) && i != mainHandSlot) {
                potionSlots.add(i);
            }
        }

        // 포션이 없거나 1종류만 있는 경우 순환할 필요 없음
        if (potionSlots.size() <= 0) {
            player.sendMessage("No other potions to switch to.");
            return;
        }

        // 이전 포션의 인덱스 확인
        int lastPotionIndex = lastPotionIndexMap.getOrDefault(playerUUID, -1);

        // 첫 명령어 실행 시: 첫 번째 포션으로 설정
        if (lastPotionIndex == -1) {
            lastPotionIndex = 0;
        }

        // 다음 포션 슬롯으로 이동 (리스트의 마지막 포션이라면 첫 번째로 돌아옴)
        int nextIndex = (lastPotionIndex + 1) % potionSlots.size();
        int nextSlot = potionSlots.get(nextIndex);

        // 메인 핸드 포션과 인벤토리의 다음 포션 교체
        ItemStack nextPotion = inventory[nextSlot];
        player.getInventory().setItem(mainHandSlot, nextPotion); // 메인 핸드에 새로운 포션 배치
        player.getInventory().setItem(nextSlot, currentPotion);  // 현재 메인 핸드 포션을 인벤토리로 이동

        // 교체된 포션의 인덱스를 저장 (이제 교체된 포션이 lastPotion이 됨)
        lastPotionIndexMap.put(playerUUID, nextIndex);

        // 메시지 출력 - 교체된 포션의 이름을 출력
        player.sendMessage("Switched to " + (nextPotion.getItemMeta() != null && nextPotion.getItemMeta().hasDisplayName() ? nextPotion.getItemMeta().getDisplayName() : nextPotion.getType().name()));
    }

    private boolean isArrow(ItemStack item) {
        return item.getType() == Material.ARROW ||
                item.getType() == Material.TIPPED_ARROW ||
                item.getType() == Material.SPECTRAL_ARROW;
    }

    private boolean isPotion(ItemStack item) {
        return item.getType() == Material.SPLASH_POTION ||
                item.getType() == Material.LINGERING_POTION;
    }
}