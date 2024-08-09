package org.battle.mineground;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class WorldBorderController {

    private final JavaPlugin plugin;
    private final World world;
    private final FileConfiguration config;
    private final List<BukkitRunnable> tasks = new ArrayList<>(); // 실행 중인 작업을 추적

    public WorldBorderController(JavaPlugin plugin) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld("world"); // 월드 이름을 필요에 따라 변경하세요
        this.config = plugin.getConfig();
    }

    public void startPhases() {
        List<String> phaseKeys = config.getConfigurationSection("").getKeys(false).stream().toList();
        executePhase(phaseKeys, 0);  // 첫 번째 페이즈부터 시작
    }

    private void executePhase(List<String> phaseKeys, int index) {
        if (index >= phaseKeys.size()) {
            return;  // 모든 페이즈가 완료되면 종료
        }

        String phaseKey = phaseKeys.get(index);
        int breaktime = config.getInt(phaseKey + ".breaktime");
        int add = config.getInt(phaseKey + ".add");
        int shrinktime = config.getInt(phaseKey + ".shrinktime");
        double damage = config.getDouble(phaseKey + ".damage");

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                // 월드보더 크기 조절 명령어 실행
                String command = String.format("worldborder add %d %d", add, shrinktime);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                // 월드보더 데미지 설정 명령어 실행
                String damageCommand = String.format("worldborder damage amount %f", damage);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), damageCommand);

                // 다음 페이즈 실행
                executePhase(phaseKeys, index + 1);
            }
        };

        task.runTaskLater(plugin, breaktime * 20L); // breaktime 초 후 실행 (틱 단위로 변환)
        tasks.add(task);  // 실행된 작업을 리스트에 추가
    }

    public void stopPhases() {
        for (BukkitRunnable task : tasks) {
            task.cancel(); // 실행 중인 모든 작업 취소
        }
        tasks.clear();  // 작업 리스트 초기화
    }
}