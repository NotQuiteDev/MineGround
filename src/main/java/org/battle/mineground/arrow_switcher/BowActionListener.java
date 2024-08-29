package org.battle.mineground.arrow_switcher;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BowActionListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material itemInHand = player.getInventory().getItemInMainHand().getType();

        // 플레이어가 Tipped Arrow를 들고 있는지 확인
        if (itemInHand == Material.TIPPED_ARROW) {
            // 플레이어가 좌클릭(블록 공격 또는 공기 공격) 했는지 확인
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
                // /switcharrow 명령어 실행
                player.performCommand("switcharrow");
            }
        }
    }
}