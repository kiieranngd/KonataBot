package br.net.brjdevs.steven.konata.core.commands;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.economy.Looting;
import br.net.brjdevs.steven.konata.core.events.EventListener;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Random;

public class CommandListener extends EventListener<MessageReceivedEvent> {

    private static final Random r = new Random();
    public static final Logger LOGGER = LoggerFactory.getLogger("CommandListener");

    public CommandListener() {
        super(MessageReceivedEvent.class);
    }
    @Override
    public void onEvent(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isFake() || event.getGuild() != null && !event.getTextChannel().canTalk())
            return;
        String prefix, msg = event.getMessage().getRawContent().toLowerCase();
        GuildData guild = event.getGuild() == null ? null : GuildData.of(event.getGuild());
        if (msg.startsWith(prefix = KonataBot.getInstance().getConfig().defaultPrefix) || guild != null && guild.getCustomPrefix() != null && msg.startsWith(prefix = guild.getCustomPrefix())) {
            String fprefix = prefix, alias = msg.substring(fprefix.length()).split(" ")[0];
            ICommand cmd = KonataBot.getInstance().getCommandManager().getCommands().stream().filter(c -> Arrays.stream(c.getAliases()).anyMatch(x -> x.equals(alias))).findFirst().orElse(null);
            if (cmd == null)
                return;
            else if (event.getGuild() != null && !event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
                event.getChannel().sendTyping().queue(sent ->
                        event.getChannel().sendMessage(Emojis.X + " I need the permission MESSAGE_EMBED_LINKS to execute commands!").queue());
                return;
            }
            new Thread(() -> {
                CommandEvent e = new CommandEvent(cmd, event, event.getMessage().getRawContent().substring(fprefix.length() + alias.length()).trim());
                try {
                    KonataBot.getInstance().getCommandManager().invoke(e);
                    if (event.isFromType(ChannelType.TEXT))
                        Looting.of(event.getTextChannel()).drop(r.nextInt(15), 6);
                } catch (Exception ex) {
                    LOGGER.error("An unexpected error has occurred while executing command `" + cmd.getName() + "`", ex);
                    e.sendMessage(Emojis.COLD_SWEAT + " An unexpected `" + ex.getClass().getSimpleName() + "` has occurred while executing this command.\n" + (ex.getMessage() != null ? "`" + ex.getMessage() + "`\n\n" : "") + "You don't need to report this, my owner has been notified about it.").queue();
                }
            }).start();
        }
    }
}
