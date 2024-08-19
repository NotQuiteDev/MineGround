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

    // 플레이어의 마지막 화살을 저장할 맵
    private final HashMap<UUID, Integer> lastArrowIndexMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // 오프핸드에 있는 아이템이 화살인지 확인
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            if (!ArrowUtils.isArrow(offHandItem)) {
                player.sendMessage("You need to hold an arrow in your off-hand.");
                return true;
            }

            // 화살 교체 로직 실행
            switchArrow(player);
            return true;
        }
        sender.sendMessage("This command can only be used by players.");
        return false;
    }

    private void switchArrow(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack currentArrow = player.getInventory().getItemInOffHand(); // 오프핸드에 있는 화살
        UUID playerUUID = player.getUniqueId();

        // 인벤토리 내 화살 종류와 위치 수집 (오프핸드 제외)
        List<Integer> arrowSlots = new ArrayList<>();
        int offHandSlot = 40; // 오프핸드 슬롯 번호

        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && ArrowUtils.isArrow(item) && i != offHandSlot) {
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

        // 오프핸드 화살과 인벤토리의 다음 화살 교체
        ItemStack nextArrow = inventory[nextSlot];
        player.getInventory().setItem(offHandSlot, nextArrow); // 오프핸드에 새로운 화살 배치
        player.getInventory().setItem(nextSlot, currentArrow);  // 현재 오프핸드 화살을 인벤토리로 이동

        // 교체된 화살의 인덱스를 저장 (이제 교체된 화살이 lastArrow가 됨)
        lastArrowIndexMap.put(playerUUID, nextIndex);

        // 메시지 출력 - 교체된 화살의 이름을 출력
        player.sendMessage("Switched to " + (nextArrow.getItemMeta() != null && nextArrow.getItemMeta().hasDisplayName() ? nextArrow.getItemMeta().getDisplayName() : nextArrow.getType().name()));
    }
}