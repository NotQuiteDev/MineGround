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
}


