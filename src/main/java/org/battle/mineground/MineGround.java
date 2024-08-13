package org.battle.mineground;

import org.bukkit.plugin.java.JavaPlugin;

public class MineGround extends JavaPlugin {

    private WorldBorderController worldBorderController;
    private double explosionRadius;

    @Override
    public void onEnable() {
        saveDefaultConfig();  // 기본 설정 파일 저장
        loadConfigValues();
        explosionRadius = getConfig().getDouble("explosion-radius", 2.0);
        worldBorderController = new WorldBorderController(this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);

        // MGCommand 클래스의 인스턴스를 생성하고 명령어로 등록
        MGCommand mgCommand = new MGCommand(this, worldBorderController);
        this.getCommand("mg").setExecutor(mgCommand);

        // MGCommand를 이벤트 리스너로 등록
        getServer().getPluginManager().registerEvents(mgCommand, this);

        // 다른 리스너들 등록
        getServer().getPluginManager().registerEvents(new HasteArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new WaterBreathing2ArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new WaterBreathing3ArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new WaterBreathing4ArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new WaterBreathing5ArrowListener(this), this);
    }

    public double getExplosionRadius() {
        return explosionRadius;
    }

    public void loadConfigValues() {
        explosionRadius = getConfig().getDouble("explosion-radius", 2.0);
    }

    @Override
    public void onDisable() {
        // 플러그인 종료 시 로직
    }
}
