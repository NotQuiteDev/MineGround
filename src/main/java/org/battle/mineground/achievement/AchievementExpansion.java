package org.battle.mineground.achievement;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AchievementExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;

    public AchievementExpansion(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true; // This is required for the placeholder to persist through reloads
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "achievement"; // This is the identifier for your placeholders, e.g., %achievement_playcount%
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        FileConfiguration config = plugin.getConfig();
        String uuidPath = "achievements." + player.getUniqueId().toString();

        // 플레이 횟수
        if (identifier.equalsIgnoreCase("playcount")) {
            return String.valueOf(config.getInt(uuidPath + ".playCount", 0));
        }

        // 플레이 횟수 순위
        if (identifier.equalsIgnoreCase("playcount_rank")) {
            return String.valueOf(getRank("playCount", player));
        }

        // 우승 횟수
        if (identifier.equalsIgnoreCase("wincount")) {
            return String.valueOf(config.getInt(uuidPath + ".winCount", 0));
        }

        // 우승 횟수 순위
        if (identifier.equalsIgnoreCase("wincount_rank")) {
            return String.valueOf(getRank("winCount", player));
        }

        // 누적 킬수
        if (identifier.equalsIgnoreCase("killcount")) {
            return String.valueOf(config.getInt(uuidPath + ".killCount", 0));
        }

        // 누적 킬수 순위
        if (identifier.equalsIgnoreCase("killcount_rank")) {
            return String.valueOf(getRank("killCount", player));
        }

        // 상위 10명의 플레이 횟수
        if (identifier.equalsIgnoreCase("top10_playcount")) {
            return getTop10List("playCount");
        }

        // 상위 10명의 우승 횟수
        if (identifier.equalsIgnoreCase("top10_wincount")) {
            return getTop10List("winCount");
        }

        // 상위 10명의 누적 킬수
        if (identifier.equalsIgnoreCase("top10_killcount")) {
            return getTop10List("killCount");
        }
        if (identifier.equalsIgnoreCase("top1_playcount")) {
            return getTop1Player("playCount");
        }

        if (identifier.equalsIgnoreCase("top1_wincount")) {
            return getTop1Player("winCount");
        }

        if (identifier.equalsIgnoreCase("top1_killcount")) {
            return getTop1Player("killCount");
        }

        return null; // Placeholder not found
    }

    // 특정 통계 항목에 대한 플레이어의 순위를 계산하는 메소드
    private int getRank(String stat, OfflinePlayer player) {
        Map<String, Integer> playerStats = new HashMap<>();
        for (String uuid : plugin.getConfig().getConfigurationSection("achievements").getKeys(false)) {
            int count = plugin.getConfig().getInt("achievements." + uuid + "." + stat, 0);
            playerStats.put(uuid, count);
        }

        // 플레이어 통계 값을 기준으로 내림차순으로 정렬
        List<Map.Entry<String, Integer>> sortedStats = playerStats.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        // 플레이어의 순위 반환
        for (int i = 0; i < sortedStats.size(); i++) {
            if (sortedStats.get(i).getKey().equals(player.getUniqueId().toString())) {
                return i + 1; // 1부터 시작하는 순위 반환
            }
        }
        return -1; // 플레이어가 목록에 없을 경우
    }
    // 특정 통계 항목에서 1위 플레이어를 반환하는 메소드
    private String getTop1Player(String stat) {
        Map<String, Integer> playerStats = new HashMap<>();
        for (String uuid : plugin.getConfig().getConfigurationSection("achievements").getKeys(false)) {
            int count = plugin.getConfig().getInt("achievements." + uuid + "." + stat, 0);
            playerStats.put(uuid, count);
        }

        // 통계를 내림차순으로 정렬하고 1위 플레이어를 가져옴
        return playerStats.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(1) // 1위만 가져옴
                .map(entry -> {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                    return p.getName(); // 플레이어의 이름을 반환
                })
                .findFirst()
                .orElse("없음"); // 플레이어가 없을 경우
    }

    // 상위 10명의 통계 목록을 반환하는 메소드
    private String getTop10List(String stat) {
        Map<String, Integer> playerStats = new HashMap<>();
        for (String uuid : plugin.getConfig().getConfigurationSection("achievements").getKeys(false)) {
            int count = plugin.getConfig().getInt("achievements." + uuid + "." + stat, 0);
            playerStats.put(uuid, count);
        }

        // 통계를 내림차순으로 정렬하고 상위 10명을 추출
        return playerStats.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(entry.getKey()));
                    return "§7" + p.getName() + ": §a" + entry.getValue();
                })
                .collect(Collectors.joining("\n"));
    }
}