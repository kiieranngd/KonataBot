package br.net.brjdevs.steven.konata.games.hangman.events;

import br.net.brjdevs.steven.konata.games.engine.GamePlayer;
import br.net.brjdevs.steven.konata.games.engine.events.GameEvent;
import br.net.brjdevs.steven.konata.games.hangman.HangMan;

public class GuessEvent extends GameEvent {

    protected String guess;
    protected GamePlayer player;

    public GuessEvent(HangMan game, String guess, GamePlayer player) {
        super(game);
        this.guess = guess;
        this.player = player;
    }

    public String getGuess() {
        return guess;
    }

    public GamePlayer getPlayer() {
        return player;
    }
}
