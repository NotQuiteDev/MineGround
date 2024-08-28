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
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
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

            // 상자에 아이템 추가
            addItemsToChest(location);
        }
    }

    private void addItemsToChest(Location location) {
        BlockState state = location.getBlock().getState();

        if (state instanceof Chest) {
            Chest chest = (Chest) state;
            List<String> chosenCategories = chooseCategories();

            for (String category : chosenCategories) {
                ItemStack item = chooseItemFromCategory(category);
                if (item != null) {
                    chest.getInventory().addItem(item);
                }
            }
        } else {
            mineGround.getLogger().warning("상자가 아닌 블록에서 인벤토리를 시도했습니다.");
        }
    }

    // 여러 카테고리 선택 가능하게 수정된 메서드
    private List<String> chooseCategories() {
        FileConfiguration config = mineGround.getConfig();
        Map<String, Object> categories = config.getConfigurationSection("chest_contents.categories").getValues(false);
        List<String> selectedCategories = new ArrayList<>();

        Random random = new Random();

        // 각 카테고리별로 확률을 계산해 선택
        for (Map.Entry<String, Object> entry : categories.entrySet()) {
            double probability = (double) entry.getValue();
            if (random.nextDouble() <= probability) {
                selectedCategories.add(entry.getKey());
            }
        }

        // 최소 한 개의 카테고리 보장
        if (selectedCategories.isEmpty()) {
            List<String> categoryList = new ArrayList<>(categories.keySet());
            selectedCategories.add(categoryList.get(random.nextInt(categoryList.size())));
        }

        return selectedCategories;
    }

    // 선택된 카테고리에서 아이템 선택 (카테고리 내에서 하나의 아이템만 선택)
    private ItemStack chooseItemFromCategory(String category) {
        FileConfiguration config = mineGround.getConfig();
        ConfigurationSection categorySection = config.getConfigurationSection("chest_contents." + category);

        if (categorySection != null) {
            double totalProbability = 0;
            for (String key : categorySection.getKeys(false)) {
                totalProbability += categorySection.getConfigurationSection(key).getDouble("probability");
            }

            double randomValue = new Random().nextDouble() * totalProbability;

            for (String key : categorySection.getKeys(false)) {
                randomValue -= categorySection.getConfigurationSection(key).getDouble("probability");
                if (randomValue <= 0) {
                    // 아이템 데이터를 getItemStack으로 불러옴
                    return categorySection.getItemStack(key + ".item");
                }
            }
        }

        return null;
    }

    // 구역 이름에 따른 확률을 설정 파일에서 가져오는 메서드
    private double getProbabilityFromConfig(String regionName) {
        // 구역 이름에서 언더스코어(_)를 기준으로 앞부분 추출
        String baseRegionName = regionName.contains("_") ? regionName.split("_")[0] : regionName;

        // config.yml에서 앞부분 구역 이름에 해당하는 확률을 읽어옴
        return mineGround.getConfig().getDouble("chestProbability." + baseRegionName, 0.0);
    }
}