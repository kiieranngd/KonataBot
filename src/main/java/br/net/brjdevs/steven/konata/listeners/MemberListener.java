package br.net.brjdevs.steven.konata.listeners;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.data.guild.Announces;
import br.net.brjdevs.steven.konata.core.events.EventListener;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;

public class MemberListener extends EventListener<GenericGuildMemberEvent> {

    public MemberListener() {
        super(GenericGuildMemberEvent.class);
    }
    @Override
    public void onEvent(GenericGuildMemberEvent event) {
        if (event instanceof GuildMemberJoinEvent) {
            Announces announces = KonataBot.getInstance().getDataManager().getAnnounces(event.getGuild());
            TextChannel textChannel;
            if (announces.getGreeting() == null || (textChannel = event.getGuild().getTextChannelById(announces.getChannel())) == null || !textChannel.canTalk())
                return;
            textChannel.sendMessage(replace(announces.getGreeting(), event.getMember(), event.getGuild())).queue();
        } else if (event instanceof GuildMemberLeaveEvent) {
            Announces announces = KonataBot.getInstance().getDataManager().getAnnounces(event.getGuild());
            TextChannel textChannel;
            if (announces.getFarewell() == null || (textChannel = event.getGuild().getTextChannelById(announces.getChannel())) == null || !textChannel.canTalk())
                return;
            textChannel.sendMessage(replace(announces.getFarewell(), event.getMember(), event.getGuild())).queue();
        }
    }

    public static String replace(String s, Member member, Guild guild) {
        return s.replace("%user%", member.getUser().getName()).replace("%mention%", member.getUser().getAsMention()).replace("%guild%", guild.getName()).replace("%id%", member.getUser().getId());
    }
}
