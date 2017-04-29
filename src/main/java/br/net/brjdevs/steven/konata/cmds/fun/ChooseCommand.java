package br.net.brjdevs.steven.konata.cmds.fun;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;

import java.util.Random;

public class ChooseCommand {
    private static final Random r = new Random();

    private static final String[] QUOTES = {
            "I'd stay with",
            "The best option is",
            "For sure",
            "I'd choose",
            "I pick"
    };

    @RegisterCommand
    public static ICommand choose() {
        return new ICommand.Builder()
                .setAliases("choose")
                .setName("Choose Command")
                .setDescription("Chooses between the given optoions.")
                .setCategory(Category.FUN)
                .setAction((event) -> {
                    String rawOptions = event.getArguments();
                    String[] options;
                    if (rawOptions.contains(" or "))
                        options = rawOptions.split(" or ");
                    else
                        options = rawOptions.split("\\s+");
                    if (options.length == 1) {
                        event.sendMessage("You need to give me at least 2 options to choose between!").queue();
                        return;
                    }
                    String choice = options[r.nextInt(options.length - 1)];
                    event.sendMessage(getRandomQuote() + " " + choice).queue();
                })
                .build();
    }

    private static String getRandomQuote() {
        return QUOTES[r.nextInt(QUOTES.length)];
    }
}
