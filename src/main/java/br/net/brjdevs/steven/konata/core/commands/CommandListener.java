package br.net.brjdevs.steven.konata.core.commands;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.events.EventListener;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class CommandListener extends EventListener<MessageReceivedEvent> {

    public CommandListener() {
        super(MessageReceivedEvent.class);
    }
    @Override
    public void onEvent(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isFake())
            return;
        String prefix, msg = event.getMessage().getRawContent();
        GuildData guild = KonataBot.getInstance().getDataManager().getGuild(event.getGuild());
        if (msg.startsWith(prefix = KonataBot.getInstance().getConfig().defaultPrefix) || guild.getCustomPrefix() != null && msg.startsWith(prefix = guild.getCustomPrefix())) {
            String fprefix = prefix, alias = msg.substring(fprefix.length()).split(" ")[0];
            ICommand cmd = KonataBot.getInstance().getCommandManager().getCommands().stream().filter(c -> Arrays.stream(c.getAliases()).anyMatch(x -> x.equals(alias))).findFirst().orElse(null);
            if (cmd == null)
                return;
            new Thread(() ->
                KonataBot.getInstance().getCommandManager().invoke(new CommandEvent(cmd, event, msg.substring(fprefix.length() + alias.length()).trim()))
            ).start();
        }
    }
}
