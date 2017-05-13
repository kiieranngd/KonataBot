package br.net.brjdevs.steven.konata.games.hangman;

import br.net.brjdevs.steven.konata.core.data.FileDataManager;
import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.games.engine.AbstractGame;
import br.net.brjdevs.steven.konata.games.engine.GamePlayer;
import br.net.brjdevs.steven.konata.games.engine.events.LooseEvent;
import br.net.brjdevs.steven.konata.games.engine.events.WinEvent;
import br.net.brjdevs.steven.konata.games.hangman.events.AlreadyGuessedEvent;
import br.net.brjdevs.steven.konata.games.hangman.events.GuessEvent;
import br.net.brjdevs.steven.konata.games.hangman.events.InvalidGuessEvent;
import gnu.trove.list.TCharList;
import gnu.trove.list.array.TCharArrayList;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HangMan extends AbstractGame {

    private static final FileDataManager HANGMAN_WORDS = new FileDataManager("hangmanwords.txt");
    private static final Random r = new Random(System.currentTimeMillis());

    private String word, guesses;
    private TCharList mistakes;

    public HangMan(MessageChannel channel, User user) {
        super(5, channel, user);
    }

    public int getMaxMistakes() {
        return getPlayers().length * 5;
    }
    public void guess(GamePlayer player, String guess) {
        if (!word.toLowerCase().contains(guess.toLowerCase())) {
            mistakes.add(guess.charAt(0));
            if (isGameOver()) {
                fireEvent(new LooseEvent(this));
                return;
            }
            fireEvent(new InvalidGuessEvent(this, guess, player));
        } else if (guesses.toLowerCase().contains(guess.toLowerCase()) || mistakes.contains(guess.toLowerCase().charAt(0)) || mistakes.contains(guess.toUpperCase().charAt(0))) {
            fireEvent(new AlreadyGuessedEvent(this, guess, player));
        } else {
            final char c = guess.charAt(0);
            final char[] chars = guesses.toCharArray();
            int index = -1;
            while((index = index > -1 ? word.indexOf(guess, index + 1) : word.toLowerCase().indexOf(guess.toLowerCase())) > -1) {
                chars[index] = c;
            }
            this.guesses = String.valueOf(chars);
            if (isGameOver()) {
                fireEvent(new WinEvent(this));
                return;
            }
            fireEvent(new GuessEvent(this, guess, player));
        }
    }
    public String getWord() {
        return word;
    }
    public TCharList getMistakes() {
        return mistakes;
    }
    public String getGuesses() {
        return guesses;
    }
    @Override
    public void setup() {
        this.word = HANGMAN_WORDS.get().get(r.nextInt(HANGMAN_WORDS.get().size()));
        this.guesses = word.replaceAll("([^ ])", "_");
        this.mistakes = new TCharArrayList();
        addListener(new HangManListener());
    }
    @Override
    public String getName() {
        return "HangMan";
    }
    @Override
    public boolean isPrivateAvailable() {
        return true;
    }
    @Override
    public boolean isGameOver() {
        return guesses.equalsIgnoreCase(word) || mistakes.size() > getMaxMistakes();
    }
    @Override
    public boolean isTurn(GamePlayer player) {
        return true;
    }
    @Override
    public void end(GameEndReason endReason) {

    }
    @Override
    public void call(MessageReceivedEvent event) {
        GamePlayer player = Arrays.stream(getPlayers()).filter(p -> p.getId() == event.getAuthor().getIdLong()).findFirst().orElse(null);
        if (player == null)
            return;
        String msg = event.getMessage().getRawContent();
        if (msg.charAt(0) == '^' && msg.length() > 1)
            guess(player, msg.substring(1, 2));
    }
}
