package org.battle.mineground;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

        // MGCommand 클래스의 인스턴스를 생성하고 명령어로 등록
        this.getCommand("mg").setExecutor(new MGCommand(this, worldBorderController));
        getServer().getPluginManager().registerEvents(new HasteArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new WaterBreathing2ArrowListener(this), this);  // 새 리스너 등록
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