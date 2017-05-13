package br.net.brjdevs.steven.konata.core.commands;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandManager {
    private List<ICommand> commands;

    public CommandManager() {
        commands = new ArrayList<>(new Reflections("br.net.brjdevs.steven.konata", new MethodAnnotationsScanner())
                .getMethodsAnnotatedWith(RegisterCommand.class).stream()
                .map(method -> {
                    if (method.getReturnType() != ICommand.class)
                        return null;
                    try {
                        return (ICommand) method.invoke(null);
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public List<ICommand> getCommands() {
        return commands;
    }

    public List<ICommand> getCommands(Category category) {
        return commands.stream().filter(command -> category.equals(command.getCategory())).collect(Collectors.toList());
    }

    public void invoke(CommandEvent event) {
        ICommand cmd = event.getCommand();
        if (!cmd.isPrivateAvailable() && !event.isFromType(ChannelType.TEXT)) {
            event.sendMessage("Sorry, but you can only use this commands in text channels! " + Emojis.WINK).queue();
            return;
        } else if (cmd.isOwnerOnly() && !KonataBot.getInstance().isOwner(event.getAuthor())) {
            event.sendMessage(Emojis.NO_GOOD + " This command is owner only.").queue();
            return;
        }

        try {
            cmd.invoke(event);
        } catch (ArrayIndexOutOfBoundsException exception) {
            event.sendMessage(getHelp(cmd, event.getMember())).queue();
        }
    }

    public static MessageEmbed getHelp(ICommand cmd, Member member) {
        User user = member.getUser();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.decode("#388BDF"));
        embedBuilder.setTitle("\u2139 Help for " + cmd.getName(), null);
        embedBuilder.setFooter("Requested by " + StringUtils.toString(user), user.getEffectiveAvatarUrl());
        embedBuilder.setTimestamp(Instant.now());
        if (cmd.isOwnerOnly() && !KonataBot.getInstance().isOwner(user)) {
            embedBuilder.setDescription("This command is owner only, you shouldn't be looking at this.");
            return embedBuilder.build();
        }
        embedBuilder.appendDescription("**" + cmd.getCategory().getEmoji() + " | " + cmd.getCategory().getName() + "**\n\n");
        if (cmd.getDescription() != null) {
            embedBuilder.appendDescription("\uD83D\uDCAC " + cmd.getDescription() + "\n\n");
        }
        if (cmd.getUsageInstruction() != null) {
            embedBuilder.appendDescription("\uD83D\uDD16 **Usage instruction:**\n```" + Arrays.stream(cmd.getUsageInstruction().split("\n")).map(s -> " - " + s).collect(Collectors.joining("\n")) + "```");
        } else
            embedBuilder.appendDescription("No usage instructions for this command.");
        if (cmd.getAliases().length > 1)
            embedBuilder.appendDescription("\n\n**Aliases:** " + String.join(", ", cmd.getAliases()));
        embedBuilder.setThumbnail("http://www.iconsfind.com/wp-content/uploads/2015/10/20151012_561baa2799b27.png");
        return embedBuilder.build();
    }
}
