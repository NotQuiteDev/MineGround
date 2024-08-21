package org.battle.mineground;

import org.battle.mineground.arrow_switcher.ArrowSwitcherCommand;
import org.battle.mineground.arrow_switcher.BowActionListener;
import org.battle.mineground.arrows.*;
import org.battle.mineground.elytra.ElytraCommand;
import org.battle.mineground.elytra.ElytraListener;
import org.battle.mineground.enchant.EnchantCombiner;
import org.battle.mineground.enchant.EnchantInventoryListener;
import org.bukkit.plugin.java.JavaPlugin;

public class MineGround extends JavaPlugin {

    private WorldBorderController worldBorderController;
    private GameBossBar gameBossBar;
    private double explosionRadius;
    @Override
    public void onEnable() {
        saveDefaultConfig();  // 기본 설정 파일 저장
        loadConfigValues();
        explosionRadius = getConfig().getDouble("explosion-radius", 2.0);
        worldBorderController = new WorldBorderController(this);
        worldBorderController.startSpectatorParticleTask();
        EnchantCombiner enchantCombiner = new EnchantCombiner();
        getServer().getPluginManager().registerEvents(new HorseTameListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosionDamageListener(this), this);
        // GameBossBar 생성 및 이벤트 등록
        gameBossBar = new GameBossBar(this);
        getServer().getPluginManager().registerEvents(gameBossBar, this);
        getServer().getPluginManager().registerEvents(new ElytraListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantInventoryListener(enchantCombiner), this);
        // MGCommand 클래스의 인스턴스를 생성하고 명령어로 등록
        MGCommand mgCommand = new MGCommand(this, worldBorderController);
        getCommand("switcharrow").setExecutor(new ArrowSwitcherCommand());
        this.getCommand("giveelytra").setExecutor(new ElytraCommand(this));
        this.getCommand("mg").setExecutor(mgCommand);

        // MGCommand를 이벤트 리스너로 등록
        getServer().getPluginManager().registerEvents(mgCommand, this);
        getServer().getPluginManager().registerEvents(worldBorderController, this); // 이벤트 리스너 등록
        getServer().getPluginManager().registerEvents(new BowActionListener(), this);
        WorldBorderController controller = new WorldBorderController(this);
        getServer().getPluginManager().registerEvents(new GameEventListener(controller), this);

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
        // 게임 종료 시 보스바 제거
        if (gameBossBar != null) {
            gameBossBar.removeBossBar();
        }

        // WorldBorderController에서 실행 중인 작업 정리
        if (worldBorderController != null) {
            worldBorderController.stopPhases();
        }
    }
}