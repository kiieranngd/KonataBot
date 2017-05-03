package br.net.brjdevs.steven.konata.core.economy.events;

import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.utils.ProfileUtils;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public class ExperienceFoundEvent extends Event {

    public ExperienceFoundEvent(ProfileData data, TextChannel textChannel, Member member) {
        super(data, textChannel, member);
        int exp = r.nextInt(35);
        textChannel.sendMessage(member.getAsMention() + " - You find a small shiny orb and approaches it. When you're really close, it flies right into your chest! You feel a little stronger. *+" + exp + " experience*").queue();
        ProfileUtils.addExperience(data, exp);
        data.saveAsync();
    }
}
