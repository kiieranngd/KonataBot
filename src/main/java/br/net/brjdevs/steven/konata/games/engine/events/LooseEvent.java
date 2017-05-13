package br.net.brjdevs.steven.konata.games.engine.events;

import br.net.brjdevs.steven.konata.games.engine.AbstractGame;
import br.net.brjdevs.steven.konata.games.engine.GamePlayer;

public class LooseEvent extends GameEndEvent {

    protected GamePlayer[] loosers;

    public LooseEvent(AbstractGame game, GamePlayer... loosers) {
        super(game, AbstractGame.GameEndReason.DEFEAT);
        this.loosers = loosers;
    }
}
