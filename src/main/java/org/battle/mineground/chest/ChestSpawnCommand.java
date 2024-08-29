package org.battle.mineground.chest;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.battle.mineground.chest.ChestSpawner;
import org.bukkit.Bukkit;
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
        // OP 여부 확인 (모든 CommandSender가 명령어를 사용할 수 있지만 OP 권한은 필요)
        if (!sender.isOp()) {
            sender.sendMessage("이 명령어를 사용할 권한이 없습니다.");
            return true;
        }

        // 월드를 가져올 때 Sender가 플레이어인 경우에만 월드를 가져옴
        World world;
        if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else {
            world = Bukkit.getWorlds().get(0);  // 만약 플레이어가 아니면 첫 번째 월드를 기본값으로 설정
        }

        // 인자가 없으면 모든 구역에 상자를 소환
        if (args.length == 0) {
            chestSpawner.spawnChestsInAllRegions(world);
            sender.sendMessage("모든 구역에 상자가 소환되었습니다.");
        } else {
            // 특정 구역에 상자 소환
            String regionName = args[0];

            // RegionContainer를 통해 RegionManager 가져오기
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

            if (regionManager == null) {
                sender.sendMessage("RegionManager를 찾을 수 없습니다.");
                return true;
            }

            // regionName에 해당하는 구역 가져오기
            ProtectedRegion region = regionManager.getRegion(regionName);

            if (region == null) {
                sender.sendMessage("구역 " + regionName + "을 찾을 수 없습니다.");
                return true;
            }

            // 구역에 상자 소환
            chestSpawner.spawnChestsInRegion(world, region);
            sender.sendMessage(regionName + " 구역에 상자가 소환되었습니다.");
        }

        return true;
    }
}