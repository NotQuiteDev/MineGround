package org.battle.mineground.chest;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.battle.mineground.MineGround;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Map;
import java.util.Random;

public class ChestSpawner {

    private final MineGround mineGround;

    public ChestSpawner(MineGround mineGround) {
        this.mineGround = mineGround;
    }

    // 모든 구역에 상자를 소환하는 메서드
    public void spawnChestsInAllRegions(World world) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

        if (regionManager == null) {
            mineGround.getLogger().warning("RegionManager를 찾을 수 없습니다.");
            return;
        }

        // 모든 구역에 대해 상자 소환
        for (Map.Entry<String, ProtectedRegion> entry : regionManager.getRegions().entrySet()) {
            spawnChestsInRegion(world, entry.getValue());
        }
    }

    // 특정 구역에 상자를 소환하는 메서드
    public void spawnChestsInRegion(World world, ProtectedRegion region) {
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        int regionVolume = (max.getBlockX() - min.getBlockX() + 1) *
                (max.getBlockY() - min.getBlockY() + 1) *
                (max.getBlockZ() - min.getBlockZ() + 1);

        // 구역 이름에 따른 확률을 가져옴
        double probabilityPerBlock = getProbabilityFromConfig(region.getId());

        // 소환할 상자 개수 계산
        int chestCount = (int) (regionVolume * probabilityPerBlock);
        Random random = new Random();

        for (int i = 0; i < chestCount; i++) {
            // 구역 내 랜덤 좌표 생성
            int x = random.nextInt(max.getBlockX() - min.getBlockX() + 1) + min.getBlockX();
            int z = random.nextInt(max.getBlockZ() - min.getBlockZ() + 1) + min.getBlockZ();
            int y = random.nextInt(max.getBlockY() - min.getBlockY() + 1) + min.getBlockY();

            Location randomLocation = new Location(world, x, y, z);

            // 아래로 탐색하여 단단한 블럭 찾기 (중간 위치에서부터 아래로 탐색)
            Block block = findSolidBlockBelow(randomLocation);

            if (block != null) {
                Location chestLocation = block.getLocation().add(0, 1, 0);
                spawnChestAtLocation(chestLocation);
            }
        }
    }

    // 랜덤으로 선택한 위치에서 아래로 탐색하여 단단한 블럭을 찾는 메서드
    private Block findSolidBlockBelow(Location location) {
        World world = location.getWorld();
        int y = location.getBlockY();

        while (y > 0) {  // Y좌표를 0까지 탐색
            Block block = world.getBlockAt(location.getBlockX(), y, location.getBlockZ());
            if (block.getType().isSolid()) {
                return block;  // 단단한 블럭을 찾으면 반환
            }
            y--;
        }

        return null;  // 단단한 블럭을 찾지 못하면 null 반환
    }

    private void spawnChestAtLocation(Location location) {
        World world = location.getWorld();
        Block block = world.getBlockAt(location);

        Block above1 = world.getBlockAt(location.clone().add(0, 1, 0));
        Block above2 = world.getBlockAt(location.clone().add(0, 2, 0));

        if (block.getType() == Material.AIR && above1.getType() == Material.AIR && above2.getType() == Material.AIR) {
            block.setType(Material.CHEST);
            // 아이템 추가 로직 추가 가능
        }
    }

    private double getProbabilityFromConfig(String regionName) {
        // 구역 이름에서 언더스코어(_)를 기준으로 앞부분 추출
        String baseRegionName = regionName.contains("_") ? regionName.split("_")[0] : regionName;

        // config.yml에서 앞부분 구역 이름에 해당하는 확률을 읽어옴
        return mineGround.getConfig().getDouble("chestProbability." + baseRegionName, 0.0);
    }
}