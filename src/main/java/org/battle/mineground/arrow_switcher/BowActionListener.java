package org.battle.mineground.arrow_switcher;
import org.battle.mineground.MineGround;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BowActionListener implements Listener {


    private final MineGround plugin;

    public BowActionListener(MineGround plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material itemInHand = player.getInventory().getItemInMainHand().getType();

        // 플레이어가 Tipped Arrow, Splash Potion, 또는 Lingering Potion을 들고 있는지 확인
        if (itemInHand == Material.TIPPED_ARROW ||
                itemInHand == Material.SPLASH_POTION ||
                itemInHand == Material.LINGERING_POTION) {

            // 플레이어가 좌클릭(블록 공격 또는 공기 공격) 했는지 확인
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) {
                // /switcharrow 명령어 실행
                player.performCommand("switcharrow");
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

            if (itemInMainHand.getType() == Material.TIPPED_ARROW || itemInMainHand.getType() == Material.SPECTRAL_ARROW) {
                // 화살의 경우: 1틱 뒤에 체크
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                            switchNextArrow(player);
                        }
                    }
                }.runTaskLater(plugin, 1); // 1틱 지연 실행
            } else if (itemInMainHand.getType() == Material.SPLASH_POTION || itemInMainHand.getType() == Material.LINGERING_POTION) {
                // 포션의 경우: 1틱 뒤에 체크
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                            switchNextPotion(player);
                        }
                    }
                }.runTaskLater(plugin, 1); // 1틱 지연 실행
            }
        }
    }

    private void switchNextArrow(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        int mainHandSlot = player.getInventory().getHeldItemSlot();

        // 다음 화살을 찾아 교체
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && (item.getType() == Material.TIPPED_ARROW || item.getType() == Material.SPECTRAL_ARROW) && i != mainHandSlot) {
                player.getInventory().setItem(mainHandSlot, item);
                player.getInventory().clear(i);
                break;
            }
        }
    }

    private void switchNextPotion(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        int mainHandSlot = player.getInventory().getHeldItemSlot();

        // 다음 포션을 찾아 교체
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = inventory[i];
            if (item != null && (item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) && i != mainHandSlot) {
                player.getInventory().setItem(mainHandSlot, item);
                player.getInventory().clear(i);
                break;
            }
        }
    }
}
