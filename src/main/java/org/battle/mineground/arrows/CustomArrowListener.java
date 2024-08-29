package org.battle.mineground.arrows;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomArrowListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> lastShotTime = new HashMap<>();
    private final long SHOOT_COOLDOWN = 1000; // 1초 (1000밀리초) 간격

    public CustomArrowListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        Block clickedBlock = event.getClickedBlock();

        // 상호작용할 수 있는 블록들에 대한 예외 처리
        if (clickedBlock != null && isInteractiveBlock(clickedBlock.getType())) {
            return; // 상자 등 상호작용할 수 있는 블록을 우클릭한 경우 화살 발사하지 않음
        }

        if (itemInHand.getType() == Material.TIPPED_ARROW &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

            // 마지막 발사 시점 확인
            long currentTime = System.currentTimeMillis();
            long lastShot = lastShotTime.getOrDefault(player.getUniqueId(), 0L);

            // 발사 간격이 충분히 지났는지 확인
            if (currentTime - lastShot < SHOOT_COOLDOWN) {
                return; // 쿨다운이 아직 끝나지 않았으면 발사하지 않음
            }

            // 발사 로직
            Arrow arrow = player.launchProjectile(Arrow.class);
            PotionMeta potionMeta = (PotionMeta) itemInHand.getItemMeta();

            // Tipped Arrow의 포션 효과 적용
            if (potionMeta != null) {
                potionMeta.getCustomEffects().forEach(effect -> arrow.addCustomEffect(effect, true));
            }

            arrow.setVelocity(player.getLocation().getDirection().multiply(2));
            player.sendMessage("화살이 발사되었습니다!");

            // 손에 있는 아이템 개수 줄이기
            itemInHand.setAmount(itemInHand.getAmount() - 1);

            // 아이템 개수가 0이 되면 인벤토리에서 제거
            if (itemInHand.getAmount() <= 0) {
                player.getInventory().remove(itemInHand);
            }

            // 발사 시간 기록
            lastShotTime.put(player.getUniqueId(), currentTime);
        }
    }

    // 상호작용할 수 있는 블록인지 확인하는 메서드
    private boolean isInteractiveBlock(Material material) {
        switch (material) {
            case CHEST:
            case TRAPPED_CHEST:
            case CRAFTING_TABLE:
            case FURNACE:
            case ANVIL:
            case STONECUTTER:
            case LOOM:
            case ENCHANTING_TABLE:
            case ENDER_CHEST:
            case BARREL:
            case BLAST_FURNACE:
            case SMOKER:
            case DISPENSER:
            case DROPPER:
            case HOPPER:
            case SHULKER_BOX:
            case BREWING_STAND:
                return true;
            default:
                return false;
        }
    }
}