package ru.peef.spleef.listeners;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.peef.spleef.game.GameManager;
import ru.peef.spleef.game.MapManager;
import ru.peef.spleef.game.PlayerManager;

@Getter
public class EventListener implements Listener {
    private final GameManager gameManager;
    private final MapManager mapManager;
    private final PlayerManager playerManager;

    public EventListener(GameManager gameManager, PlayerManager playerManager, MapManager mapManager) {
        this.gameManager = gameManager;
        this.playerManager = playerManager;
        this.mapManager = mapManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage("");
        getPlayerManager().joinPlayer(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage("");
        getPlayerManager().leavePlayer(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getY() <= 10
                && getGameManager().getState().inProgress())
            getPlayerManager().killPlayer(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (getGameManager().getState().inProgress()
                && block.getType().equals(Material.SNOW_BLOCK)
                && event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_SPADE)) {
            getMapManager().addBrokenBlock(block);
            event.setDropItems(false);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler public void onEntityDamage(EntityDamageEvent event) { event.setCancelled(true); }
    @EventHandler public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) { event.setCancelled(true); }
    @EventHandler public void onPlayerDropItem(PlayerDropItemEvent event) { event.setCancelled(true); }
    @EventHandler public void onBlockPlace(BlockPlaceEvent event) { event.setCancelled(true); }
}
