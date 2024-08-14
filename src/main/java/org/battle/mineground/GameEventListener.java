package org.battle.mineground;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameEventListener implements Listener {

    private final WorldBorderController controller;

    public GameEventListener(WorldBorderController controller) {
        this.controller = controller;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (controller.isGameRunning()) { // 게임이 진행 중일 때만 처리
            controller.handlePlayerDeath(event);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (controller.isGameRunning()) { // 게임이 진행 중일 때만 처리
            controller.handlePlayerQuit(event);
        }
    }
}