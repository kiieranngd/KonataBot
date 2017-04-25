package br.net.brjdevs.steven.konata.core.music.radio;

import br.net.brjdevs.steven.konata.KonataBot;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public class RadioTrack {
    private AudioTrack track;
    private long guildId, djId;

    public RadioTrack(AudioTrack track, Guild guild, User dj) {
        this.track = track;
        this.guildId = guild.getIdLong();
        this.djId = dj.getIdLong();
    }

    public AudioTrack getTrack() {
        return track;
    }

    public Guild getGuild() {
        return KonataBot.getInstance().getShards()[KonataBot.getInstance().getShardId(guildId)].getJDA().getGuildById(guildId);
    }

    public User getDJ() {
        return KonataBot.getInstance().getShards()[KonataBot.getInstance().getShardId(guildId)].getJDA().getUserById(djId);
    }
}
