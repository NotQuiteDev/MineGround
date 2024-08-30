package org.battle.mineground;

import org.battle.mineground.achievement.AchievementExpansion;
import org.battle.mineground.achievement.AchievementManager;
import org.battle.mineground.arrow_switcher.ArrowSwitcherCommand;
import org.battle.mineground.arrow_switcher.BowActionListener;
import org.battle.mineground.arrows.*;
import org.battle.mineground.chest.ChestSpawnCommand;
import org.battle.mineground.chest.ChestSpawner;
import org.battle.mineground.chest.ItemRegister;
import org.battle.mineground.elytra.ElytraCommand;
import org.battle.mineground.elytra.ElytraListener;
import org.battle.mineground.enchant.EnchantCombiner;
import org.battle.mineground.enchant.EnchantInventoryListener;
import org.battle.mineground.pointer.PointerCommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;


public class MineGround extends JavaPlugin {

    private WorldBorderController worldBorderController;
    private EnchantCombiner enchantCombiner;
    private boolean isReloading = false;  // 리로드 여부를 추적하는 플래그

    @Override
    public void onEnable() {
        saveDefaultConfig();  // 기본 설정 파일 저장
        registerListenersAndCommands();  // 리스너 및 명령어 등록
    }

    @Override
    public void onDisable() {
        saveConfig();
        if (worldBorderController != null) {
            worldBorderController.stopPhases();  // 월드 보더 작업 정리
        }

        // 모든 스케줄러 작업 취소
        Bukkit.getScheduler().cancelTasks(this);
    }

    // 매번 config에서 explosion-radius 값을 가져오는 메서드 추가
    public double getExplosionRadius() {
        return getConfig().getDouble("explosion-radius", 2.0);  // config.yml에서 explosion-radius 값 가져오기
    }

    private void registerListenersAndCommands() {
        // WorldBorderController 초기화 및 리스너 등록
        worldBorderController = new WorldBorderController(this);

        worldBorderController.startSpectatorParticleTask();
        ChestSpawner chestSpawner = new ChestSpawner(this);

        // 인챈트 성공 확률을 설정에서 읽어옴
        int enchantSuccessRate = getConfig().getInt("enchant-success-rate", 70);  // 기본값은 70%
        enchantCombiner = new EnchantCombiner(enchantSuccessRate);

        getServer().getPluginManager().registerEvents(new HorseTameListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosionDamageListener(this), this);


        getServer().getPluginManager().registerEvents(new ElytraListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantInventoryListener(enchantCombiner), this);

        // MGCommand 클래스의 인스턴스를 생성하고 명령어로 등록
        MGCommand mgCommand = new MGCommand(this, worldBorderController);
        getCommand("switcharrow").setExecutor(new ArrowSwitcherCommand());
        getCommand("giveelytra").setExecutor(new ElytraCommand(this));
        getCommand("mg").setExecutor(mgCommand);
        getCommand("showpointer").setExecutor(new PointerCommandExecutor(this));
        getCommand("spawnchests").setExecutor(new ChestSpawnCommand(chestSpawner));
        // MGCommand를 이벤트 리스너로 등록
        getServer().getPluginManager().registerEvents(mgCommand, this);
        getServer().getPluginManager().registerEvents(worldBorderController, this); // 이벤트 리스너 등록
        getServer().getPluginManager().registerEvents(new BowActionListener(), this);
        getServer().getPluginManager().registerEvents(new CustomArrowListener(this), this);

        // AchievementManager 인스턴스를 생성하고 이벤트 리스너로 등록
        AchievementManager achievementManager = new AchievementManager(this);
        getServer().getPluginManager().registerEvents(achievementManager, this);

        ItemRegister itemRegister = new ItemRegister(this);
        getCommand("registeritem").setExecutor(itemRegister);
        getCommand("spawnitem").setExecutor(itemRegister);
        getCommand("spawnitem").setTabCompleter(itemRegister);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new AchievementExpansion(this).register();
        }

        // 추가 리스너 등록
        getServer().getPluginManager().registerEvents(new GameEventListener(worldBorderController), this);
        getServer().getPluginManager().registerEvents(new HasteArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new WaterBreathing2ArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new WaterBreathing3ArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new WaterBreathing4ArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new WaterBreathing5ArrowListener(this), this);
    }

    public void reloadPlugin() {
        isReloading = true;  // 리로드 중임을 표시

        // 기존 리스너 및 스케줄러 작업 취소
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);


        // 설정 다시 로드
        reloadConfig();

        // 리스너 및 명령어 재등록 (보스바는 생성하지 않음)
        registerListenersAndCommands();

        isReloading = false;  // 리로드 완료
    }
}
