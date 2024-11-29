package ru.peef.spleef;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import ru.peef.spleef.game.GameManager;
import ru.peef.spleef.listeners.EventListener;

@Getter
public class Spleef extends JavaPlugin {
    @Getter
    private static Database database;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        GameManager.setWorld(getServer().getWorlds().get(0));

        GameManager.getSpawns().add(new Location(GameManager.getWorld(), 0.5, 14, 5.5, 180, 0));
        GameManager.getSpawns().add(new Location(GameManager.getWorld(), 5.5, 14, 0.5, 90, 0));
        GameManager.getSpawns().add(new Location(GameManager.getWorld(), -5.5, 14, 0.5, -90, 0));
        GameManager.getSpawns().add(new Location(GameManager.getWorld(), 0.5, 14, -5.5, 0, 0));

        database = new Database();
    }

    @Override
    public void onDisable() {
        GameManager.restoreMap();
    }

    public static JavaPlugin getInstance() { return getProvidingPlugin(Spleef.class); }
}
