package ru.peef.spleef.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MapManager {
    private final List<Block> brokenBlocks = new ArrayList<>();
    private final GameManager gameManager;
    private final PlayerManager playerManager;
    @Setter
    private List<Location> spawns = new ArrayList<>();

    public MapManager(GameManager gameManager, PlayerManager playerManager) {
        this.gameManager = gameManager;
        this.playerManager = playerManager;
    }

    public void addBrokenBlock(Block block) {
        brokenBlocks.add(block);
    }

    public void restoreMap() {
        playerManager.getPlayers().forEach(gp -> {
            gp.getPlayer().teleport(gameManager.getSpawnLocation());
            gp.getPlayer().setGameMode(GameMode.SURVIVAL);
            gp.getPlayer().getInventory().clear();
            gp.setAlive(true);
        });
        getBrokenBlocks().forEach(block -> block.setType(Material.SNOW_BLOCK));

        gameManager.getState().setStatus(GameStatus.WAITING);
        gameManager.setStartTimestamp(0);

        gameManager.checkForStart();
    }
}
