package org.battle.mineground.achievement;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AchievementManager implements Listener {

    private final JavaPlugin plugin;

    public AchievementManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    // 게임 시작 시 호출되는 메소드
    public void startGame() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.getConfig().set("players." + player.getUniqueId() + ".damageDealt", 0);
            plugin.getConfig().set("players." + player.getUniqueId() + ".killStreak", 0);  // 연속 킬 초기화
            plugin.getConfig().set("players." + player.getUniqueId() + ".startTime", System.currentTimeMillis());  // 생존 시간 초기화
        }
        plugin.saveConfig();
    }

    // 게임 종료 시 호출되는 메소드
    public void stopGame() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            int damageDealt = plugin.getConfig().getInt("players." + player.getUniqueId() + ".damageDealt", 0);
            int mostDamageDealt = plugin.getConfig().getInt("achievements." + player.getUniqueId() + ".mostDamageDealt", 0);
            if (damageDealt > mostDamageDealt) {
                plugin.getConfig().set("achievements." + player.getUniqueId() + ".mostDamageDealt", damageDealt);
            }

            int killStreak = plugin.getConfig().getInt("players." + player.getUniqueId() + ".killStreak", 0);
            int highestKillStreak = plugin.getConfig().getInt("achievements." + player.getUniqueId() + ".highestKillStreak", 0);
            if (killStreak > highestKillStreak) {
                plugin.getConfig().set("achievements." + player.getUniqueId() + ".highestKillStreak", killStreak);
            }

            long startTime = plugin.getConfig().getLong("players." + player.getUniqueId() + ".startTime");
            long survivalTime = System.currentTimeMillis() - startTime;
            long longestSurvivalTime = plugin.getConfig().getLong("achievements." + player.getUniqueId() + ".longestSurvivalTime", 0);
            if (survivalTime > longestSurvivalTime) {
                plugin.getConfig().set("achievements." + player.getUniqueId() + ".longestSurvivalTime", survivalTime);
            }

            // 생존 시간 초기화 (게임 종료 후 생존 시간 증가 방지)
            plugin.getConfig().set("players." + player.getUniqueId() + ".startTime", 0);
        }
        plugin.saveConfig();
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
    // 플레이어가 다른 플레이어를 처치할 때 호출되는 메소드
    public void onPlayerKill(Player killer) {
        int killStreak = plugin.getConfig().getInt("players." + killer.getUniqueId() + ".killStreak", 0);
        plugin.getConfig().set("players." + killer.getUniqueId() + ".killStreak", killStreak + 1);
        plugin.saveConfig();
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

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player dealer = (Player) event.getDamager();
            int damage = (int) event.getDamage();
            int currentDamage = plugin.getConfig().getInt("players." + dealer.getUniqueId() + ".damageDealt", 0);
            plugin.getConfig().set("players." + dealer.getUniqueId() + ".damageDealt", currentDamage + damage);
            plugin.saveConfig();
        }
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null && event.getEntity() instanceof Player) {
            Player killer = event.getEntity().getKiller();
            onPlayerKill(killer);
        }
    }

}