package br.net.brjdevs.steven.konata.games.hangman;

import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.ProfileUtils;
import br.net.brjdevs.steven.konata.core.utils.TimeParser;
import br.net.brjdevs.steven.konata.games.engine.GameListener;
import br.net.brjdevs.steven.konata.games.engine.GamePlayer;
import br.net.brjdevs.steven.konata.games.engine.events.GameEvent;
import br.net.brjdevs.steven.konata.games.engine.events.LooseEvent;
import br.net.brjdevs.steven.konata.games.engine.events.StopEvent;
import br.net.brjdevs.steven.konata.games.engine.events.WinEvent;
import br.net.brjdevs.steven.konata.games.hangman.events.AlreadyGuessedEvent;
import br.net.brjdevs.steven.konata.games.hangman.events.GuessEvent;
import br.net.brjdevs.steven.konata.games.hangman.events.InvalidGuessEvent;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class HangManListener implements GameListener {
    @Override
    public void onEvent(GameEvent event) {
        HangMan hm = (HangMan) event.getGame();
        if (event instanceof WinEvent) {
            hm.getChannel().sendMessage(Emojis.PARTY_POPPER + " Yay, you won! Congratulations! *+15 experience +10 coins*").queue();
            Stream.of(hm.getPlayers()).map(GamePlayer::getProfile).forEach(profile -> {
                ProfileUtils.addExperience(profile, 15);
                ProfileUtils.addCoins(profile, 10);
                profile.saveAsync();
            });
        } else if (event instanceof LooseEvent) {
            hm.getChannel().sendMessage(Emojis.CONFUSED + " You lost but you did your best! The word was `" + hm.getWord() + "`. *-5 experience*").queue();
            Stream.of(hm.getPlayers()).map(GamePlayer::getProfile).forEach(profile -> {
                ProfileUtils.takeExperience(profile, 5);
                profile.saveAsync();
            });
        } else if (event instanceof StopEvent) {
            hm.getChannel().sendMessage("Aww, why did you give up? You were doing so well! *-5 experience*").queue();;
            Stream.of(hm.getPlayers()).map(GamePlayer::getProfile).forEach(profile -> {
                ProfileUtils.takeExperience(profile, 5);
                profile.saveAsync();
            });
        } else if (event instanceof GuessEvent) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Hang man", null);
            embedBuilder.setColor(Color.decode("#388BDF"));
            List<String> mistakes = new ArrayList<>();
            for (char c : hm.getMistakes().toArray()) {
                mistakes.add(String.valueOf(c));
            }
            embedBuilder.setDescription("Nice, the letter `" + ((GuessEvent) event).getGuess() + "` is in the word! Keep up the good job!\n\n**Statistics:**\n" +
                    "     - You've been playing this game for " + TimeParser.toTextString(System.currentTimeMillis() - hm.getStartTime()) + "\n" +
                    "     - You've guessed " + Arrays.stream(hm.getGuesses().split("")).filter(s -> !s.equals(" ") && !s.equals("_")).count() + " letters so far.\n" +
                    "     - You've missed " + hm.getMistakes().size() + " letters so far. Mistakes: " + String.join(", ", mistakes) + "\n" +
                    "     - Current Guesses: " + hm.getGuesses());
            hm.getChannel().sendMessage(embedBuilder.build()).queue();
        } else if (event instanceof InvalidGuessEvent) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Hang man", null);
            embedBuilder.setColor(Color.decode("#388BDF"));
            List<String> mistakes = new ArrayList<>();
            for (char c : hm.getMistakes().toArray()) {
                mistakes.add(String.valueOf(c));
            }
            embedBuilder.setDescription("Oops, the letter `" + ((InvalidGuessEvent) event).getGuess() + "` is not in the word!\n\n**Statistics:**\n" +
                    "     - You've been playing this game for " + TimeParser.toTextString(System.currentTimeMillis() - hm.getStartTime()) + "\n" +
                    "     - You've guessed " + Arrays.stream(hm.getGuesses().split("")).filter(s -> !s.equals(" ") && !s.equals("_")).count() + " letters so far.\n" +
                    "     - You've missed " + hm.getMistakes().size() + " letters so far. Mistakes: " + String.join(", ", mistakes) + "\n" +
                    "     - Current Guesses: " + hm.getGuesses());
            hm.getChannel().sendMessage(embedBuilder.build()).queue();
        } else if (event instanceof AlreadyGuessedEvent) {
            hm.getChannel().sendMessage(Emojis.X + " You already guessed that character!").queue();
        }
    }
}
