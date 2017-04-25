package br.net.brjdevs.steven.konata.core.commands;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.awt.*;
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

    public void invoke(CommandEvent event) {
        ICommand cmd = event.getCommand();
        if (!cmd.isPrivateAvailable() && !event.isFromType(ChannelType.TEXT)) {
            event.sendMessage("Sorry, but you can only use this commands in text channels! " + Emojis.WINK).queue();
            return;
        } else if (cmd.getRequiredPermission() != null && event.isFromType(ChannelType.TEXT) && !event.getMember().hasPermission(((TextChannel) event.getChannel()), cmd.getRequiredPermission()) && !KonataBot.getInstance().isOwner(event.getAuthor())) {
            event.sendMessage(Emojis.NO_GOOD + " Nope, I can't let you use this command because you are missing the " + Arrays.stream(cmd.getRequiredPermission()).map(Permission::toString).collect(Collectors.joining(", ")) + " permission" + (cmd.getRequiredPermission().length < 2 ? "" : "s") + ".").queue();
            return;
        } else if (cmd.isOwnerOnly() && !KonataBot.getInstance().isOwner(event.getAuthor())) {
            event.sendMessage(Emojis.NO_GOOD + " This command is owner only.").queue();
            return;
        }

        cmd.invoke(event);
    }

    public static MessageEmbed getHelp(ICommand cmd, JDA jda) {
        return new EmbedBuilder().setAuthor("Help for Custom Commands", null, jda.getSelfUser().getEffectiveAvatarUrl()).appendDescription("**You have to provide valid arguments to use this command, here's a list with the sub commands:**\n\n").appendDescription(cmd.getUsageInstruction()).setColor(Color.decode("#388BDF")).build();
    }
}
