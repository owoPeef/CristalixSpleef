package ru.peef.spleef.game;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@Data
@NoArgsConstructor
public class GamePlayer {
    private Player player;
    private boolean alive = true;

    public GamePlayer(Player player) {
        this.player = player;
    }
}