package br.net.brjdevs.steven.konata.games.engine.events;

import br.net.brjdevs.steven.konata.games.engine.AbstractGame;

public class StopEvent extends GameEvent {
    public StopEvent(AbstractGame game) {
        super(game);
        game.end(AbstractGame.GameEndReason.STOP);
    }
}
