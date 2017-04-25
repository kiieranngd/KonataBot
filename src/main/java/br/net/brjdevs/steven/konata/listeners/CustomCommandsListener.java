package br.net.brjdevs.steven.konata.listeners;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.data.guild.CustomCommand;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.events.EventListener;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class CustomCommandsListener extends EventListener<GuildMessageReceivedEvent> {

    public CustomCommandsListener() {
        super(GuildMessageReceivedEvent.class);
    }
    @Override
    public void onEvent(GuildMessageReceivedEvent event) {
        GuildData data = KonataBot.getInstance().getDataManager().getGuild(event.getGuild());
        if (data.getCustomCommands().isEmpty())
            return;
        String prefix, msg = event.getMessage().getRawContent();
        if (msg.startsWith(prefix = KonataBot.getInstance().getConfig().defaultPrefix) || data.getCustomPrefix() != null && msg.startsWith(prefix = data.getCustomPrefix())) {
            String alias = msg.substring(prefix.length()).split(" ")[0];
            CustomCommand cmd = data.getCustomCommands().get(alias);
            if (cmd != null) {
                String answer = replace(cmd.getRandomAnswer(), event.getMember(), msg.substring(prefix.length() + alias.length()).trim());
                if (!answer.isEmpty())
                    event.getChannel().sendTyping().queue(sent -> event.getChannel().sendMessage(answer).queue());
            }
        }
    }

    private String replace(String s, Member member, String args) {
        return s.replace("%user%", member.getUser().getName()).replace("%mention%", member.getUser().getAsMention()).replace("%guild%", member.getGuild().getName()).replace("%id%", member.getUser().getName()).replace("%input%", args);
    }
}
