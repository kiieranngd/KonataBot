package br.net.brjdevs.steven.konata.core.economy.events;

import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Random;

public abstract class Event {
    public static final Random r = new Random();
    protected final ProfileData data;
    protected final TextChannel textChannel;
    protected final Member member;

    public Event(ProfileData data, TextChannel textChannel, Member member) {
        this.data = data;
        this.textChannel = textChannel;
        this.member = member;
    }

    public ProfileData getProfile() {
        return data;
    }

    public TextChannel getTextChannel() {
        return textChannel;
    }

    public Member getMember() {
        return member;
    }
}
