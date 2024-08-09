package org.battle.mineground;

import org.bukkit.plugin.java.JavaPlugin;

public class MineGround extends JavaPlugin {

    private WorldBorderController worldBorderController;

    @Override
    public void onEnable() {
        saveDefaultConfig();  // 기본 설정 파일 저장
        worldBorderController = new WorldBorderController(this);

        // MGCommand 클래스의 인스턴스를 생성하고 명령어로 등록
        this.getCommand("mg").setExecutor(new MGCommand(this, worldBorderController));
    }

    @Override
    public void onDisable() {
        // 플러그인 종료 시 로직
    }
}