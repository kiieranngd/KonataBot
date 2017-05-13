package br.net.brjdevs.steven.konata.cmds.fun;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;

public class LoveCommand {

    @RegisterCommand
    public static ICommand love() {
        return new ICommand.Builder()
                .setAliases("love")
                .setName("Love Command")
                .setDescription("Calculates the love between two names.")
                .setCategory(Category.FUN)
                .setAction((event) -> {
                    if (event.getArguments().isEmpty()) {
                        event.sendMessage(Emojis.X + " You have to tell me names to calculate love!").queue();
                        return;
                    }
                    String[] args = StringUtils.splitArgs(event.getArguments(), 2);
                    String firstName = args[0];
                    String secondName = args.length < 2 ? event.getAuthor().getName() : args[1];
                    if (firstName.equals(secondName)) {
                        event.sendMessage("Oh well, that's narcissistic.").queue();
                        return;
                    }
                    for (User user : event.getMessage().getMentionedUsers()) {
                        firstName = firstName.replaceAll("<@!?" + user.getId() + ">", user.getName());
                        secondName = secondName.replaceAll("<@!?" + user.getId() + ">", user.getName());
                    }
                    if (firstName.isEmpty() || secondName.isEmpty()) {
                        event.sendMessage(Emojis.COLD_SWEAT + " I think that I need 2 names to calculate love percentage.").queue();
                        return;
                    }
                    int percentage = (firstName.codePoints().sum() + secondName.codePoints().sum()) % 101;
                    String message = message(percentage);
                    event.sendMessage(new EmbedBuilder().setDescription("\u2763 **LOVE CALCULATOR** \u2763\n\uD83D\uDC97 *`" + firstName + "`*\n\uD83D\uDC97 *`" + secondName + "`*\n**" + percentage + "%** `" + StringUtils.getProgressBar(percentage, 100) + "` " + message).setColor(Color.decode("#388BDF")).build()).queue();
                })
                .build();
    }
    
    public static String message(int percentage) {
        if (percentage <= 20)
            return "Better luck next time.";
        else if (percentage <= 40)
            return "Not so bad.";
        else if (percentage == 69)
            return "( ͡° ͜ʖ ͡°)";
        else if (percentage <= 60)
            return "Pretty great!";
        else if (percentage <= 80)
            return "A lovely ship! <:blobaww:306819724367495169>";
        else
            return "Perfect! \u2764";
    }
}
