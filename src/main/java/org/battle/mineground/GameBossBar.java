package org.battle.mineground;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GameBossBar implements Listener {

    private final JavaPlugin plugin;
    private BossBar bossBar;
    private int totalPlayers;
    private int survivingPlayers;

    public GameBossBar(JavaPlugin plugin) {
        this.plugin = plugin;
        this.totalPlayers = Math.max(1, Bukkit.getOnlinePlayers().size());  // 플레이어 수가 0일 경우 1로 설정
        this.survivingPlayers = totalPlayers;

        // 보스바 생성 및 초기화
        bossBar = Bukkit.createBossBar("Survivors: " + survivingPlayers + "/" + totalPlayers, BarColor.GREEN, BarStyle.SOLID);

        // 모든 플레이어에게 보스바 표시
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        // 보스바 진행 상태 초기화
        updateBossBar();
    }

    // 플레이어 사망 이벤트 처리
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.setGameMode(GameMode.SPECTATOR); // 플레이어를 관전자 모드로 변경

        // 생존자 수 업데이트
        survivingPlayers--;
        updateBossBar();
    }

    // 보스바 업데이트 메서드
    private void updateBossBar() {
        int totalPlayers = Bukkit.getOnlinePlayers().size();
        double progress = (totalPlayers > 0) ? (double) survivingPlayers / totalPlayers : 0.0;

        // Progress 값이 0.0보다 작거나 1.0보다 크지 않도록 보장
        progress = Math.max(0.0, Math.min(1.0, progress));

        bossBar.setTitle("Survivors: " + survivingPlayers + "/" + totalPlayers);
        bossBar.setProgress(progress);

        // 보스바 색상 업데이트 (예: 생존자가 적을수록 색상 변경)
        if (progress > 0.5) {
            bossBar.setColor(BarColor.GREEN);
        } else if (progress > 0.2) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.RED);
        }
    }

    // 보스바 제거 메서드
    public void removeBossBar() {
        bossBar.removeAll(); // 모든 플레이어에서 보스바 제거
    }
}
