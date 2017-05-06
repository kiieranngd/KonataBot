package br.net.brjdevs.steven.konata.games.engine;

import br.net.brjdevs.steven.konata.games.engine.events.GameEvent;

public interface GameListener {
    void onEvent(GameEvent event);
}
