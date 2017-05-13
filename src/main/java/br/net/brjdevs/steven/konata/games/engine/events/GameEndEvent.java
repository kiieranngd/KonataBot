package br.net.brjdevs.steven.konata.games.engine.events;

import br.net.brjdevs.steven.konata.games.engine.AbstractGame;
import br.net.brjdevs.steven.konata.games.engine.GamePlayer;

import java.util.stream.Stream;

public class GameEndEvent extends GameEvent {
    public GameEndEvent(AbstractGame game, AbstractGame.GameEndReason endReason) {
        super(game);
        Stream.of(game.getPlayers()).forEach(GamePlayer::destroy);
        game.end(endReason);
    }
}
