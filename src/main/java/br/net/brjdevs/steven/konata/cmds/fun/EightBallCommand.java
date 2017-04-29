package br.net.brjdevs.steven.konata.cmds.fun;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;

import java.util.Random;

public class EightBallCommand {

    private static final Random r = new Random();
    public static final String[] ANSWERS = {
            "It is certain",
            "It is decidedly so",
            "Without a doubt",
            "Yes, definitely",
            "You may rely on it",
            "As I see it, yes",
            "Most likely",
            "Outlook good",
            "Yes",
            "Signs point to yes",
            "Reply hazy try again",
            "Ask again later",
            "Better not tell you now",
            "Cannot predict now",
            "Concentrate and ask again",
            "Don't count on it",
            "My reply is no",
            "My sources say no",
            "Outlook not so good",
            "Very doubtful"
    };

    public static String getAnswer(String answer) {
        return ANSWERS[answer.codePoints().sum() % ANSWERS.length];
    }

    @RegisterCommand
    public static ICommand eightball() {
        return new ICommand.Builder()
                .setAliases("eightball", "8ball", "8")
                .setName("8Ball Command")
                .setDescription("Ask the magic 8Ball a question!")
                .setCategory(Category.FUN)
                .setAction((event) -> {
                    String question = event.getArguments();
                    if (question.isEmpty()) {
                        event.sendMessage("\uD83C\uDFB1 *`Ask the 8Ball a question!`*").queue();
                        return;
                    }

                    event.sendMessage("\uD83C\uDFB1 *`" + getAnswer(question) + "`*").queue();
                })
                .build();
    }
}
