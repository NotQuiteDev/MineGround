package org.battle.mineground.chest;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.battle.mineground.chest.ChestSpawner;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.WorldGuard;

public class ChestSpawnCommand implements CommandExecutor {

    private final ChestSpawner chestSpawner;

    public ChestSpawnCommand(ChestSpawner chestSpawner) {
        this.chestSpawner = chestSpawner;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 명령어를 실행한 사람이 플레이어인지 확인
        if (!(sender instanceof Player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        // OP 여부 확인
        if (!player.isOp()) {
            player.sendMessage("이 명령어를 사용할 권한이 없습니다.");
            return true;
        }

        // 인자가 없으면 모든 구역에 상자를 소환
        if (args.length == 0) {
            chestSpawner.spawnChestsInAllRegions(world);
            player.sendMessage("모든 구역에 상자가 소환되었습니다.");
        } else {
            // 특정 구역에 상자 소환
            String regionName = args[0];

            // RegionContainer를 통해 RegionManager 가져오기
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

            if (regionManager == null) {
                player.sendMessage("RegionManager를 찾을 수 없습니다.");
                return true;
            }

            // regionName에 해당하는 구역 가져오기
            ProtectedRegion region = regionManager.getRegion(regionName);

            if (region == null) {
                player.sendMessage("구역 " + regionName + "을 찾을 수 없습니다.");
                return true;
            }

            // 구역에 상자 소환
            chestSpawner.spawnChestsInRegion(world, region);
            player.sendMessage(regionName + " 구역에 상자가 소환되었습니다.");
        }

        return true;
    }
}