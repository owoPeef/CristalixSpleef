package ru.peef.spleef.listeners;

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
import ru.peef.spleef.Spleef;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage("");
        Spleef.getPlayerManager().joinPlayer(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage("");
        Spleef.getPlayerManager().leavePlayer(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getY() <= 10
                && Spleef.getGameManager().getState().inProgress())
            Spleef.getPlayerManager().killPlayer(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (Spleef.getGameManager().getState().inProgress()
                && block.getType().equals(Material.SNOW_BLOCK)
                && event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.DIAMOND_SPADE)) {
            Spleef.getMapManager().addBrokenBlock(block);
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
