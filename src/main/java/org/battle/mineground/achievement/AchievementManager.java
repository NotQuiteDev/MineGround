package org.battle.mineground.achievement;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AchievementManager implements Listener {

    private final JavaPlugin plugin;

    public AchievementManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // 플레이 횟수 증가 메소드
    public void increasePlayCount(Player player) {
        FileConfiguration config = plugin.getConfig();
        String path = "achievements." + player.getUniqueId() + ".playCount";
        int currentCount = config.getInt(path, 0);
        config.set(path, currentCount + 1);
        plugin.saveConfig();
    }

    // 우승 횟수 증가 메소드
    public void increaseWinCount(Player player) {
        FileConfiguration config = plugin.getConfig();
        String path = "achievements." + player.getUniqueId() + ".winCount";
        int currentCount = config.getInt(path, 0);
        config.set(path, currentCount + 1);
        plugin.saveConfig();
    }

    // 누적 킬수 증가 리스너 (누군가를 죽였을 때)
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player killer = event.getEntity().getKiller();
            FileConfiguration config = plugin.getConfig();
            String path = "achievements." + killer.getUniqueId() + ".killCount";
            int currentCount = config.getInt(path, 0);
            config.set(path, currentCount + 1);
            plugin.saveConfig();
        }
    }




    // 플레이어의 업적을 가져오는 메소드
    public String getPlayerAchievements(Player player) {
        FileConfiguration config = plugin.getConfig();
        return "Play Count: " + config.getInt("achievements." + player.getUniqueId() + ".playCount", 0) + "\n" +
                "Win Count: " + config.getInt("achievements." + player.getUniqueId() + ".winCount", 0) + "\n" +
                "Kill Count: " + config.getInt("achievements." + player.getUniqueId() + ".killCount", 0) + "\n" +
                "Participation Count: " + config.getInt("achievements." + player.getUniqueId() + ".participationCount", 0) + "\n" +
                "First Place Count: " + config.getInt("achievements." + player.getUniqueId() + ".firstPlaceCount", 0);
    }
}