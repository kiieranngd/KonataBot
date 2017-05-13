package br.net.brjdevs.steven.konata.games.engine.events;

import br.net.brjdevs.steven.konata.games.engine.AbstractGame;

public class StopEvent extends GameEndEvent {
    public StopEvent(AbstractGame game) {
        super(game, AbstractGame.GameEndReason.STOP);
    }
}
