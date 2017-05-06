package br.net.brjdevs.steven.konata.games.engine.events;

import br.net.brjdevs.steven.konata.games.engine.AbstractGame;

public class TieEvent extends GameEvent {

    public TieEvent(AbstractGame game) {
        super(game);
        game.end(AbstractGame.GameEndReason.TIE);
    }

}
