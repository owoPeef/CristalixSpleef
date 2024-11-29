package ru.peef.spleef.game;

import lombok.Getter;
import lombok.Setter;

public class GameStateManager {
    @Getter
    @Setter
    private GameStatus status = GameStatus.WAITING;

    public boolean isStarting() { return status == GameStatus.STARTING; }
    public boolean inProgress() { return status == GameStatus.IN_PROGRESS; }
    public boolean isWaiting() { return status == GameStatus.WAITING; }
}
