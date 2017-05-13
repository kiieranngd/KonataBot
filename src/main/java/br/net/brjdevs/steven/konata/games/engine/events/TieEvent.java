package br.net.brjdevs.steven.konata.games.engine.events;

import br.net.brjdevs.steven.konata.games.engine.AbstractGame;

public class TieEvent extends GameEndEvent {

    public TieEvent(AbstractGame game) {
        super(game, AbstractGame.GameEndReason.TIE);
    }

}
