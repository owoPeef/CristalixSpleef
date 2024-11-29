package ru.peef.spleef.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.peef.spleef.Spleef;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {
    @Setter
    @Getter
    private List<GamePlayer> players = new ArrayList<>();
    private final GameManager gameManager;

    public PlayerManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void joinPlayer(Player player) {
        if (getPlayers().size() >= GameManager.getMaxPlayers()) {
            player.kickPlayer(ChatColor.RED + "Игроков достаточно!");
            return;
        }

        GamePlayer gamePlayer = new GamePlayer(player);
        gameManager.getDatabase().addPlayer(gamePlayer);

        player.getInventory().clear();
        player.teleport(gameManager.getSpawnLocation());

        getPlayers().add(gamePlayer);
        broadcastMessage(gamePlayer.getPlayer(), true);

        gameManager.checkForStart();
    }

    public void leavePlayer(Player player) {
        getPlayers().removeIf(gp -> gp.getPlayer().equals(player));
        broadcastMessage(player, false);

        if (gameManager.getState().isStarting()) {
            gameManager.getState().setStatus(GameStatus.WAITING);

            Bukkit.broadcastMessage(ChatColor.RED + "Отсчёт отменён: недостаточно игроков!");
        }
    }

    public void killPlayer(Player player) {
        GamePlayer gamePlayer = getPlayers()
                .stream().filter((search) -> search.getPlayer().getName().equals(player.getName()))
                .findFirst()
                .orElse(null);

        if (gamePlayer != null && gamePlayer.isAlive()) {
            Bukkit.broadcastMessage(String.format("&6%s &cпроиграл!", player.getName()).replace('&', ChatColor.COLOR_CHAR));

            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(new Location(gameManager.getWorld(), 0, 16, 0));

            gamePlayer.setAlive(false);
            gameManager.checkForEnd();
        }
    }

    private void broadcastMessage(Player player, boolean isJoin) {
        Bukkit.broadcastMessage(String.format(isJoin ? "&6%s &bзашёл в игру! &e[%s/%s]" : "&6%s &cвышел из игры! &e[%s/%s]",
                player.getName(),
                getPlayers().size(),
                GameManager.getMaxPlayers()
        ).replace('&', ChatColor.COLOR_CHAR));
    }
}
