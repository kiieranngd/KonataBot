package br.net.brjdevs.steven.konata.games.engine.events;

import br.net.brjdevs.steven.konata.games.engine.AbstractGame;
import br.net.brjdevs.steven.konata.games.engine.GamePlayer;

public class WinEvent extends GameEndEvent {

    protected GamePlayer[] winners;

    public WinEvent(AbstractGame game, GamePlayer... winners) {
        super(game, AbstractGame.GameEndReason.VICTORY);
        this.winners = winners;
    }
}
