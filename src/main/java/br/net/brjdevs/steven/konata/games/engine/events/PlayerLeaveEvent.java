package br.net.brjdevs.steven.konata.games.engine.events;

import br.net.brjdevs.steven.konata.games.engine.AbstractGame;
import br.net.brjdevs.steven.konata.games.engine.GamePlayer;

public class PlayerLeaveEvent extends GameEvent {

    protected GamePlayer player;

    public PlayerLeaveEvent(AbstractGame game, GamePlayer player) {
        super(game);
        if (game.getPlayers().length < 2)
            throw new IllegalArgumentException("Cannot process PlayerLeaveEvent in a SinglePlayer session!");
        this.player = player;
    }
}
