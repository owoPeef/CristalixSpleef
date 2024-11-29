package ru.peef.spleef.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import ru.peef.spleef.Database;
import ru.peef.spleef.Spleef;

@Setter
public class GameManager {
    @Getter private static int maxPlayers = 2;

    @Getter
    private long startTimestamp;

    @Getter
    private World world;

    @Getter
    private GameStateManager state = new GameStateManager();

    @Getter
    @Setter
    private PlayerManager playerManager;

    @Getter
    @Setter
    private Database database;

    @Getter
    @Setter
    private MapManager mapManager;

    public void checkForStart() {
        if (getPlayerManager().getPlayers().size() >= getMaxPlayers()) {
            state.setStatus(GameStatus.STARTING);
            BukkitScheduler scheduler = Bukkit.getScheduler();

            Bukkit.broadcastMessage(ChatColor.AQUA + "Игра начнется через " + ChatColor.YELLOW + "5 сек.");
            scheduler.runTaskLater(Spleef.getInstance(), this::startGame, 5 * 20L);
        }
    }

    private void startGame() {
        if (state.isStarting()) {
            state.setStatus(GameStatus.IN_PROGRESS);
            startTimestamp = System.currentTimeMillis();

            for (int i = 0; i < getPlayerManager().getPlayers().size(); i++) {
                GamePlayer gamePlayer = getPlayerManager().getPlayers().get(i);
                Player player = gamePlayer.getPlayer();

                player.sendTitle(ChatColor.YELLOW + (ChatColor.BOLD + "ИГРА НАЧАЛАСЬ"), ChatColor.AQUA + "Удачи", 5, 40, 5);
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();

                ItemStack shovel = new ItemStack(Material.DIAMOND_SPADE);
                ItemMeta meta = shovel.getItemMeta();

                if (meta != null) {
                    meta.setUnbreakable(true);
                    meta.addEnchant(Enchantment.DIG_SPEED, 5, true);

                    shovel.setItemMeta(meta);
                }

                player.getInventory().setItem(0, shovel);
                player.getInventory().setHeldItemSlot(0);
                player.teleport(getMapManager().getSpawns().get(i));
            }
        }
    }

    public void checkForEnd() {
        if (state.inProgress()) {
            int aliveCount = (int) getPlayerManager().getPlayers().stream()
                    .filter(GamePlayer::isAlive).count();

            if (aliveCount <= 1) {
                endGame();
            }
        }
    }

    private void endGame() {
        state.setStatus(GameStatus.END);

        getDatabase().recordGame();

        getPlayerManager().getPlayers().stream()
                .filter(GamePlayer::isAlive)
                .findFirst()
                .ifPresent(winner -> {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "==================\n" + ChatColor.YELLOW + (ChatColor.BOLD + "ИГРА ОКОНЧЕНА!\n") + ChatColor.AQUA + "Победитель: " + ChatColor.GOLD + winner.getPlayer().getName());
                    getDatabase().addWins(winner, 1);
                });

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(Spleef.getInstance(), getMapManager()::restoreMap, 2 * 20L);
    }

    public Location getSpawnLocation() {
        return new Location(getWorld(), 0.5, 14, 0.5);
    }
}
