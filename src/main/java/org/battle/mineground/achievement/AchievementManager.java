package org.battle.mineground.achievement;

import org.battle.mineground.WorldBorderController;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AchievementManager implements Listener {

    private final JavaPlugin plugin;
    private final WorldBorderController worldBorderController;  // final로 설정해 명확히 전달 받도록 함

    public AchievementManager(JavaPlugin plugin, WorldBorderController worldBorderController) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.worldBorderController = worldBorderController;  // 여기서 실제로 전달된 인스턴스로 초기화
    }

    // 게임 시작 시 호출되는 메소드
    public void startGame() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            plugin.getConfig().set("players." + player.getUniqueId() + ".damageDealt", 0);
            plugin.getConfig().set("players." + player.getUniqueId() + ".killStreak", 0);  // 연속 킬 초기화
            plugin.getConfig().set("players." + player.getUniqueId() + ".startTime", System.currentTimeMillis());  // 생존 시간 초기화
            plugin.getConfig().set("players." + player.getUniqueId() + ".isAlive", true);  // 생존 상태 초기화
        }
        plugin.saveConfig();
    }

    // 게임 종료 시 호출되는 메소드
    public void stopGame() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // 현재 게임의 피해량을 전체 피해량에 추가하여 저장
            int damageDealt = plugin.getConfig().getInt("players." + player.getUniqueId() + ".damageDealt", 0);
            int mostDamageDealt = plugin.getConfig().getInt("achievements." + player.getUniqueId() + ".mostDamageDealt", 0);

            if (damageDealt > mostDamageDealt) {
                plugin.getConfig().set("achievements." + player.getUniqueId() + ".mostDamageDealt", damageDealt);
            }

            int totalDamageDealt = plugin.getConfig().getInt("achievements." + player.getUniqueId() + ".totalDamageDealt", 0);
            plugin.getConfig().set("achievements." + player.getUniqueId() + ".totalDamageDealt", totalDamageDealt + damageDealt);

            int killStreak = plugin.getConfig().getInt("players." + player.getUniqueId() + ".killStreak", 0);
            int highestKillStreak = plugin.getConfig().getInt("achievements." + player.getUniqueId() + ".highestKillStreak", 0);

            if (killStreak > highestKillStreak) {
                plugin.getConfig().set("achievements." + player.getUniqueId() + ".highestKillStreak", killStreak);
            }

            // 플레이어가 게임 중에 살아 있었다면, 생존 시간을 계산
            if (plugin.getConfig().getBoolean("players." + player.getUniqueId() + ".isAlive", false)) {
                long startTime = plugin.getConfig().getLong("players." + player.getUniqueId() + ".startTime");
                long survivalTime = System.currentTimeMillis() - startTime;
                long longestSurvivalTime = plugin.getConfig().getLong("achievements." + player.getUniqueId() + ".longestSurvivalTime", 0);

                if (survivalTime > longestSurvivalTime) {
                    plugin.getConfig().set("achievements." + player.getUniqueId() + ".longestSurvivalTime", survivalTime);
                }
            }

            // 게임 종료 후 생존 시간 초기화 및 생존 상태 업데이트
            plugin.getConfig().set("players." + player.getUniqueId() + ".startTime", 0);
            plugin.getConfig().set("players." + player.getUniqueId() + ".isAlive", false);
            plugin.getConfig().set("players." + player.getUniqueId() + ".damageDealt", 0);
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
// 누적 킬수 증가 리스너 (누군가를 죽였을 때)
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player deceased = event.getEntity();

        // 게임이 진행 중인지 확인
        if (!worldBorderController.isGameRunning()) {
            return;
        }

        // 사망한 플레이어의 생존 시간 계산 및 저장
        long startTime = plugin.getConfig().getLong("players." + deceased.getUniqueId() + ".startTime");
        long survivalTime = System.currentTimeMillis() - startTime;
        long longestSurvivalTime = plugin.getConfig().getLong("achievements." + deceased.getUniqueId() + ".longestSurvivalTime", 0);

        if (survivalTime > longestSurvivalTime) {
            plugin.getConfig().set("achievements." + deceased.getUniqueId() + ".longestSurvivalTime", survivalTime);
        }

        // 생존 시간 초기화 (죽은 후 다시 생존 시간 기록 방지)
        plugin.getConfig().set("players." + deceased.getUniqueId() + ".startTime", 0);

        if (!(deceased.getKiller() instanceof Player)) {
            return;
        }

        // 이 코드를 추가하여 이벤트 핸들러가 즉시 실행되지 않고, 지연되어 실행되도록 함
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player killer = deceased.getKiller();
            if (killer == null) {
                return;
            }

            FileConfiguration config = plugin.getConfig();

            // 킬 카운트 증가
            String killCountPath = "achievements." + killer.getUniqueId() + ".killCount";
            int currentKillCount = config.getInt(killCountPath, 0);
            config.set(killCountPath, currentKillCount + 1);
            Bukkit.getLogger().info("PlayerKillEvent: " + killer.getName() + "의 킬 카운트가 " + (currentKillCount + 1) + "로 증가했습니다.");

            // 킬 스트릭 증가
            String killStreakPath = "players." + killer.getUniqueId() + ".killStreak";
            int currentKillStreak = config.getInt(killStreakPath, 0);
            config.set(killStreakPath, currentKillStreak + 1);
            Bukkit.getLogger().info("PlayerKillEvent: " + killer.getName() + "의 킬 스트릭이 " + (currentKillStreak + 1) + "로 증가했습니다.");

            // 설정 파일 저장
            plugin.saveConfig();
            Bukkit.getLogger().info("PlayerKillEvent: 설정 파일이 저장되었습니다.");
        }, 1L);  // 1틱 지연 후 실행
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
//ToDO : 나중에 문제가 생긴다면, 그때그때 저장하는 방식이 아닌 맵에 저장했다가 게임 시작과 끝에 저장하는 방식으로 바꾸기

}