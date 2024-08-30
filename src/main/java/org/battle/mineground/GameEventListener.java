package org.battle.mineground;
import org.bukkit.event.Listener;

public class GameEventListener implements Listener {

    private final WorldBorderController controller;

    public GameEventListener(WorldBorderController controller) {
        this.controller = controller;
    }
}


