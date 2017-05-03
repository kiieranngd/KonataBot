package br.net.brjdevs.steven.konata.core.music;

import br.net.brjdevs.steven.konata.core.utils.TLongMapUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.entities.Guild;

public class KonataMusicManager {

    private TLongObjectMap<GuildMusicManager> musicManagers;
    private AudioPlayerManager playerManager;

    public KonataMusicManager() {
        this.musicManagers = new TLongObjectHashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    public TLongObjectMap<GuildMusicManager> getMusicManagers() {
        return musicManagers;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        AudioPlayer audioPlayer = playerManager.createPlayer();
        if (guild.getAudioManager().getSendingHandler() == null || !(guild.getAudioManager().getSendingHandler() instanceof PlayerSendHandler))
            guild.getAudioManager().setSendingHandler(new PlayerSendHandler(audioPlayer));
        return TLongMapUtils.computeIfAbsent(musicManagers, guild.getIdLong(), (id) -> new GuildMusicManager(audioPlayer, guild));
    }
}
