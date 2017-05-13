package br.net.brjdevs.steven.konata.cmds.fun;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.games.engine.GamePlayer;
import br.net.brjdevs.steven.konata.games.engine.events.StopEvent;
import br.net.brjdevs.steven.konata.games.hangman.HangMan;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;

public class GamesCommand {

    @RegisterCommand
    public static ICommand game() {
        return new ICommand.Builder()
                .setAliases("game", "games")
                .setCategory(Category.FUN)
                .setName("Game Command")
                .setDescription("Let's play some games!")
                .setUsageInstruction("game [start/play] [game name] //starts a game session\n" +
                        "game [end/stop] //gives up the current session\n" +
                        "game list //lists you all the available games")
                .setAction((event) -> {
                    String[] args = event.getArguments().split(" ", 2);
                    GamePlayer player = GamePlayer.of(event.getAuthor());
                    switch (args[0]) {
                        case "play":
                        case "start":
                            if (player != null) {
                                event.sendMessage(Emojis.X + " You have a " + player.getGame().toGame().getName() + " game running in another channel!").queue();
                                return;
                            }
                            if (args.length < 2)
                                args = new String[] {args[0], ""};
                            switch (args[1]) {
                                case "hangman":
                                case "hang man":
                                    HangMan hm = new HangMan(event.getChannel(), event.getAuthor());
                                    EmbedBuilder embedBuilder = new EmbedBuilder();
                                    embedBuilder.setTitle("Hang man", null);
                                    embedBuilder.setColor(Color.decode("#388BDF"));
                                    embedBuilder.setDescription("You are now playing hang man!\n\n**Information:**\n" +
                                            "     - The word has " + hm.getWord().length() + " letters.\n" +
                                            "     - Current Guesses: `" + hm.getGuesses() + "`");
                                    event.sendMessage(embedBuilder.build()).queue();
                                    break;
                                default:
                                    event.sendMessage((args[1].isEmpty() ? "What game do you want to play? Here's a list with all the available games:" : "That doesn't look like a valid game name. Here's a list with all the available games:")
                                            + "\n1. hangman\n~wow such big list~\nMore games will be added in the future, keep in mind that I am still in development! " + Emojis.SMILE).queue();
                                    break;
                            }
                            break;
                        case "end":
                        case "stop":
                            if (player == null) {
                                event.sendMessage(Emojis.X + " You are not playing anything!").queue();
                                return;
                            }
                            player.getGame().toGame().stop();

                            break;
                        default:
                            event.sendMessage("Here's a list with all the available games:"
                                    + "\n1. hangman\n~wow such big list~\nMore games will be added in the future, keep in mind that I am still in development! " + Emojis.SMILE).queue();
                            break;
                    }
                })
                .build();
    }
}
