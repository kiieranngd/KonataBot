package br.net.brjdevs.steven.konata.games.engine.events;

import br.net.brjdevs.steven.konata.games.engine.AbstractGame;

public abstract class GameEvent {
    protected AbstractGame game;

    public GameEvent(AbstractGame game) {
        this.game = game;
    }

    public AbstractGame getGame() {
        return game;
    }
}
