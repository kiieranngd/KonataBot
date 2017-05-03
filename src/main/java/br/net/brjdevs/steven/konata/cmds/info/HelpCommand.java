package br.net.brjdevs.steven.konata.cmds.info;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.CommandManager;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HelpCommand {

    @RegisterCommand
    public static ICommand help() {
        return new ICommand.Builder()
                .setCategory(Category.INFORMATIVE)
                .setAliases("help")
                .setName("Help Command")
                .setDescription("Gives you a full list from commands you can use.")
                .setAction((event) -> {
                    CommandManager manager = KonataBot.getInstance().getCommandManager();
                    if (event.getArguments().isEmpty()) {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setAuthor("Konata help", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                        embedBuilder.setDescription(Stream.of(Category.values()).map(category -> {
                            List<ICommand> c = manager.getCommands(category);
                            return c.isEmpty() ? "" : category.getEmoji() + " **| " + category.getName() + "** - " + c.stream().map(command -> "`" + command.getAliases()[0] + "`").collect(Collectors.joining(", "));
                        }).collect(Collectors.joining("\n")) + "\n\n**To db help on a command use `konata help [command alias]`**\n*e.g.: konata help pokedex*");
                        embedBuilder.setColor(Color.decode("#388BDF"));

                        event.sendMessage(embedBuilder.build()).queue();
                    } else {
                        ICommand command = manager.getCommands().stream().filter(c -> Arrays.stream(c.getAliases()).anyMatch(x -> x.equals(event.getArguments()))).findFirst().orElse(null);
                        if (command == null) {
                            event.sendMessage(Emojis.SWEAT_SMILE + " I didn't find any commands matching that criteria! Make sure you didn't include prefixes!").queue();
                            return;
                        }
                        event.sendMessage(CommandManager.getHelp(command, event.getJDA())).queue();
                    }
                })
                .build();
    }
}
