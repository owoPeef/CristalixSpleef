package ru.peef.spleef.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import ru.peef.spleef.Spleef;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    @Getter private static int maxPlayers = 2;

    @Setter
    @Getter
    private static long startTimestamp;

    @Setter
    @Getter
    private static List<GamePlayer> players = new ArrayList<>();

    @Setter
    @Getter
    private static List<Location> spawns = new ArrayList<>();

    @Setter
    @Getter
    private static List<Block> brokenBlocks = new ArrayList<>();

    @Setter
    @Getter
    private static World world;

    @Setter
    @Getter
    private static GameStatus status = GameStatus.WAITING;

    public static void join(Player player) {
        if (GameManager.getPlayers().size() < GameManager.getMaxPlayers() && !getStatus().equals(GameStatus.IN_PROGRESS)) {
            GamePlayer gamePlayer = new GamePlayer(player);
            Spleef.getDatabase().addPlayer(gamePlayer);

            player.getInventory().clear();
            player.teleport(getSpawnLocation());

            players.add(gamePlayer);
            Bukkit.broadcastMessage(String.format("&6%s &bзашёл в игру! &e[%s/%s]",
                    gamePlayer.getPlayer().getName(),
                    gamePlayer.getPlayer().getWorld().getPlayers().size()+1,
                    getMaxPlayers()
            ).replace('&', ChatColor.COLOR_CHAR));

            checkForStart();
        } else if (getStatus().equals(GameStatus.IN_PROGRESS)) {
            player.kickPlayer(ChatColor.RED + "Игра уже идёт!");
        } else {
            player.kickPlayer(ChatColor.RED + "Игроков достаточно!");
        }
    }

    public static void leave(Player player) {
        if (GameManager.getPlayers().size() >= GameManager.getMaxPlayers()) {
            players.removeIf(gp -> gp.getPlayer().getName().equals(player.getName()));

            Bukkit.broadcastMessage(String.format("&6%s &cвышел из игры! &e[%s/%s]",
                    player.getName(),
                    player.getWorld().getPlayers().size()-1,
                    getMaxPlayers()
            ).replace('&', ChatColor.COLOR_CHAR));

            if (GameManager.getStatus().equals(GameStatus.STARTING)) {
                GameManager.setStatus(GameStatus.WAITING);

                Bukkit.broadcastMessage("&cОтчёт был отменён из-за недостатка игроков!".replace('&', ChatColor.COLOR_CHAR));
            }
        }
    }

    public static void killPlayer(Player player) {
        GamePlayer gamePlayer = getPlayers()
                .stream().filter((search) -> search.getPlayer().getName().equals(player.getName()))
                .findFirst()
                .orElse(null);

        if (gamePlayer != null && gamePlayer.isAlive()) {
            Bukkit.broadcastMessage(String.format("&6%s &cпроиграл!", player.getName()).replace('&', ChatColor.COLOR_CHAR));

            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(new Location(getWorld(), 0, 16, 0));

            gamePlayer.setAlive(false);
            checkForEnd();
        }
    }

    private static void checkForStart() {
        if (getPlayers().size() >= getMaxPlayers()) {
            setStatus(GameStatus.STARTING);
            BukkitScheduler scheduler = Bukkit.getScheduler();

            Bukkit.broadcastMessage(ChatColor.AQUA + "Игра начнется через " + ChatColor.YELLOW + "5 сек.");
            scheduler.runTaskLater(Spleef.getInstance(), GameManager::startGame, 5 * 20L);
        }
    }

    private static void startGame() {
        if (getStatus().equals(GameStatus.STARTING)) {
            setStatus(GameStatus.IN_PROGRESS);
            startTimestamp = System.currentTimeMillis();

            for (int i = 0; i < getPlayers().size(); i++) {
                GamePlayer gamePlayer = getPlayers().get(i);
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
                player.teleport(getSpawns().get(i));
            }
        }
    }

    private static void checkForEnd() {
        if (getStatus().equals(GameStatus.IN_PROGRESS)) {
            int aliveCount = (int) getPlayers().stream()
                    .filter(GamePlayer::isAlive).count();

            if (aliveCount <= 1) {
                endGame();
            }
        }
    }

    private static void endGame() {
        setStatus(GameStatus.END);

        Spleef.getDatabase().recordGame();

        players.stream()
                .filter(GamePlayer::isAlive)
                .findFirst()
                .ifPresent(winner -> {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "==================\n" + ChatColor.YELLOW + (ChatColor.BOLD + "ИГРА ОКОНЧЕНА!\n") + ChatColor.AQUA + "Победитель: " + ChatColor.GOLD + winner.getPlayer().getName());
                    Spleef.getDatabase().addWins(winner, 1);
                });

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(Spleef.getInstance(), GameManager::restoreMap, 2 * 20L);
    }

    public static void restoreMap() {
        players.forEach(gp -> {
            gp.getPlayer().teleport(getSpawnLocation());
            gp.getPlayer().setGameMode(GameMode.SURVIVAL);
            gp.getPlayer().getInventory().clear();
            gp.setAlive(true);
        });
        GameManager.getBrokenBlocks().forEach(block -> block.setType(Material.SNOW_BLOCK));

        setStatus(GameStatus.WAITING);
        startTimestamp = 0;

        checkForStart();
    }

    public static Location getSpawnLocation() {
        return new Location(getWorld(), 0.5, 14, 0.5);
    }
}
