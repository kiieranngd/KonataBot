package br.net.brjdevs.steven.konata.core.music;

import br.net.brjdevs.steven.konata.KonataBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.Guild;

public class GuildMusicManager {
    private AudioPlayer audioPlayer;
    private TrackScheduler trackScheduler;
    private long guildId;

    public GuildMusicManager(AudioPlayer audioPlayer, Guild guild) {
        this.audioPlayer = audioPlayer;
        this.trackScheduler = new TrackScheduler(audioPlayer, guild);
        this.audioPlayer.addListener(trackScheduler);
        this.guildId = guild.getIdLong();
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public Guild getGuild() {
        return KonataBot.getInstance().getShards()[KonataBot.getInstance().getShardId(guildId)].getJDA().getGuildById(guildId);
    }
}
