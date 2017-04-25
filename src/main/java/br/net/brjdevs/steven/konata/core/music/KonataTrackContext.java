package br.net.brjdevs.steven.konata.core.music;

import br.net.brjdevs.steven.konata.Shard;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class KonataTrackContext {
    private AudioTrack track;
    private long dj, channel;
    private Shard shard;

    public KonataTrackContext(AudioTrack track, User dj, TextChannel channel, Shard shard) {
        this.track = track;
        this.dj = dj.getIdLong();
        this.channel = channel.getIdLong();
        this.shard = shard;
    }

    public KonataTrackContext makeClone() {
        track.makeClone();
        return this;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public Shard getShard() {
        return shard;
    }

    public User getDJ() {
        return getShard().getJDA().getUserById(dj);
    }

    public TextChannel getChannel() {
        return getShard().getJDA().getTextChannelById(channel);
    }
}
