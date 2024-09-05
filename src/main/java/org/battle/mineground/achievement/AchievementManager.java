package org.battle.mineground.achievement;

import org.battle.mineground.WorldBorderController;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class AchievementManager implements Listener {

    private final Map<UUID, Integer> playerDamageDealt = new HashMap<>();
    private final Map<UUID, Integer> playerKillStreak = new HashMap<>();
    private final Map<UUID, Long> playerStartTime = new HashMap<>();
    private final Map<UUID, Integer> playerKillCount = new HashMap<>();
    private final Map<UUID, Integer> playerPlayCount = new HashMap<>();
    private final Map<UUID, Integer> playerWinCount = new HashMap<>();
    private final JavaPlugin plugin;
    private final WorldBorderController worldBorderController;

    public AchievementManager(JavaPlugin plugin, WorldBorderController worldBorderController) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.worldBorderController = worldBorderController;
    }

    // 게임 시작 시 호출되는 메소드
    public void startGame() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();
            playerDamageDealt.put(playerId, 0);
            playerKillStreak.put(playerId, 0);
            playerKillCount.put(playerId, 0);
            playerStartTime.put(playerId, System.currentTimeMillis());

        }
    }

    // 게임 종료 시 호출되는 메소드
    public void stopGame() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID playerId = player.getUniqueId();

            // 피해량과 관련된 업적 업데이트
            int damageDealt = playerDamageDealt.getOrDefault(playerId, 0);
            int mostDamageDealt = plugin.getConfig().getInt("achievements." + playerId + ".mostDamageDealt", 0);
            if (damageDealt > mostDamageDealt) {
                plugin.getConfig().set("achievements." + playerId + ".mostDamageDealt", damageDealt);
            }
            int totalDamageDealt = plugin.getConfig().getInt("achievements." + playerId + ".totalDamageDealt", 0);
            plugin.getConfig().set("achievements." + playerId + ".totalDamageDealt", totalDamageDealt + damageDealt);

            // 킬 관련 업적 업데이트
            int killCount = playerKillCount.getOrDefault(playerId, 0);
            int totalKillCount = plugin.getConfig().getInt("achievements." + playerId + ".killCount", 0);
            plugin.getConfig().set("achievements." + playerId + ".killCount", totalKillCount + killCount);

            int killStreak = playerKillStreak.getOrDefault(playerId, 0);
            int highestKillStreak = plugin.getConfig().getInt("achievements." + playerId + ".highestKillStreak", 0);
            if (killStreak > highestKillStreak) {
                plugin.getConfig().set("achievements." + playerId + ".highestKillStreak", killStreak);
            }

            // 생존 시간 업데이트
            if (player.getGameMode() == GameMode.SURVIVAL) {
                long startTime = playerStartTime.getOrDefault(playerId, 0L);
                if (startTime > 0) { // startTime이 0이 아닐 때만 생존 시간 계산
                    long survivalTime = System.currentTimeMillis() - startTime;
                    long longestSurvivalTime = plugin.getConfig().getLong("achievements." + playerId + ".longestSurvivalTime", 0);
                    if (survivalTime > longestSurvivalTime) {
                        plugin.getConfig().set("achievements." + playerId + ".longestSurvivalTime", survivalTime);
                    }
                }
            }


        }
        plugin.saveConfig();

        // 데이터 초기화
        playerDamageDealt.clear();
        playerKillStreak.clear();
        playerStartTime.clear();
        playerKillCount.clear();
        playerPlayCount.clear();
        playerWinCount.clear();
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player deceased = event.getEntity();
        UUID deceasedId = deceased.getUniqueId();

        if (deceased.getGameMode() == GameMode.SURVIVAL) {
            long startTime = playerStartTime.getOrDefault(deceasedId, 0L);
            if (startTime > 0) { // startTime이 0이 아닐 때만 생존 시간 계산
                long survivalTime = System.currentTimeMillis() - startTime;

                long longestSurvivalTime = plugin.getConfig().getLong("achievements." + deceasedId + ".longestSurvivalTime", 0);
                if (survivalTime > longestSurvivalTime) {
                    plugin.getConfig().set("achievements." + deceasedId + ".longestSurvivalTime", survivalTime);
                }

                playerStartTime.put(deceasedId, 0L); // 생존 시간 초기화
            }
        }

        Player killer = deceased.getKiller();
        if (killer != null) {
            UUID killerId = killer.getUniqueId();
            playerKillStreak.put(killerId, playerKillStreak.getOrDefault(killerId, 0) + 1);
            playerKillCount.put(killerId, playerKillCount.getOrDefault(killerId, 0) + 1); // 누적 킬 수를 맵에 추가
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (player.getGameMode() == GameMode.SURVIVAL) {
            long startTime = playerStartTime.getOrDefault(playerId, 0L);
            if (startTime > 0) { // startTime이 0이 아닐 때만 생존 시간 계산
                long survivalTime = System.currentTimeMillis() - startTime;

                long longestSurvivalTime = plugin.getConfig().getLong("achievements." + playerId + ".longestSurvivalTime", 0);
                if (survivalTime > longestSurvivalTime) {
                    plugin.getConfig().set("achievements." + playerId + ".longestSurvivalTime", survivalTime);
                }

                playerStartTime.put(playerId, 0L); // 생존 시간 초기화
            }
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

    public void increaseWinCount(Player player) {
        UUID playerId = player.getUniqueId();
        int currentWinCount = playerWinCount.getOrDefault(playerId, 0) + 1;
        playerWinCount.put(playerId, currentWinCount);

        // 즉시 config에 저장
        plugin.getConfig().set("achievements." + playerId + ".winCount", currentWinCount);
        plugin.saveConfig();
    }


    public void increasePlayCount(Player player) {
        UUID playerId = player.getUniqueId();
        int currentPlayCount = playerPlayCount.getOrDefault(playerId, 0) + 1;
        playerPlayCount.put(playerId, currentPlayCount);

        // 즉시 config에 저장
        plugin.getConfig().set("achievements." + playerId + ".playCount", currentPlayCount);
        plugin.saveConfig();
    }



    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player dealer = (Player) event.getDamager();
            int damage = (int) event.getDamage();

            UUID dealerId = dealer.getUniqueId();
            int currentDamage = playerDamageDealt.getOrDefault(dealerId, 0);
            playerDamageDealt.put(dealerId, currentDamage + damage);
        }
    }
}