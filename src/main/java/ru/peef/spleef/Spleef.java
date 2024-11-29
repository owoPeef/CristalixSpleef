package ru.peef.spleef;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import ru.peef.spleef.game.GameManager;
import ru.peef.spleef.game.MapManager;
import ru.peef.spleef.game.PlayerManager;
import ru.peef.spleef.listeners.EventListener;

@Getter
public class Spleef extends JavaPlugin {
    private Database database;
    private GameManager gameManager;
    private MapManager mapManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        gameManager = new GameManager();
        playerManager = new PlayerManager(gameManager);
        database = new Database(gameManager, playerManager);
        mapManager = new MapManager(gameManager, playerManager);

        gameManager.setPlayerManager(playerManager);
        gameManager.setMapManager(mapManager);
        gameManager.setDatabase(database);

        getServer().getPluginManager().registerEvents(new EventListener(gameManager, playerManager, mapManager), this);

        gameManager.setWorld(getServer().getWorlds().get(0));

        mapManager.getSpawns().add(new Location(gameManager.getWorld(), 0.5, 14, 5.5, 180, 0));
        mapManager.getSpawns().add(new Location(gameManager.getWorld(), 5.5, 14, 0.5, 90, 0));
        mapManager.getSpawns().add(new Location(gameManager.getWorld(), -5.5, 14, 0.5, -90, 0));
        mapManager.getSpawns().add(new Location(gameManager.getWorld(), 0.5, 14, -5.5, 0, 0));
    }

    @Override
    public void onDisable() {
        mapManager.restoreMap();
    }

    public static JavaPlugin getInstance() { return getProvidingPlugin(Spleef.class); }
}
