package br.net.brjdevs.steven.konata.listeners;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.events.EventListener;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;

import java.awt.*;

public class KonataListener extends EventListener<Event> {

    public KonataListener() {
        super(Event.class);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof GuildJoinEvent) {
            TextChannel tc = KonataBot.getInstance().getTextChannelById("249971874430320660");
            Guild guild = ((GuildJoinEvent) event).getGuild();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("Joined guild `" + guild.getName() + "`");
            embedBuilder.addField("ID", guild.getId(), true);
            embedBuilder.addField("Owner", StringUtils.toString(guild.getOwner().getUser()), true);
            embedBuilder.addField("Members", String.valueOf(guild.getMembers().size()), true);
            embedBuilder.setColor(Color.decode("#388BDF"));
            tc.sendMessage(embedBuilder.build()).queue();
        } else if (event instanceof GuildLeaveEvent) {
            TextChannel tc = KonataBot.getInstance().getTextChannelById("249971874430320660");
            Guild guild = ((GuildLeaveEvent) event).getGuild();
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("Left guild `" + guild.getName() + "`");
            embedBuilder.addField("ID", guild.getId(), true);
            embedBuilder.addField("Owner", StringUtils.toString(guild.getOwner().getUser()), true);
            embedBuilder.addField("Members", String.valueOf(guild.getMembers().size()), true);
            embedBuilder.setColor(Color.decode("#388BDF"));
            tc.sendMessage(embedBuilder.build()).queue();
        }
    }
}
