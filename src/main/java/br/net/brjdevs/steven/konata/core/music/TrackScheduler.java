package br.net.brjdevs.steven.konata.core.music;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    private final BlockingQueue<KonataTrackContext> queue;
    private RepeatMode repeatMode;
    private KonataTrackContext currentTrack;
    private KonataTrackContext previousTrack;
    private final AudioPlayer audioPlayer;
    private long lastMessageId, guildId;
    private TLongList voteSkips;

    public TrackScheduler(AudioPlayer audioPlayer, Guild guild) {
        this.queue = new LinkedBlockingQueue<>();
        this.repeatMode = null;
        this.currentTrack = null;
        this.previousTrack = null;
        this.audioPlayer = audioPlayer;
        this.lastMessageId = 0;
        this.guildId = guild.getIdLong();
        this.voteSkips = new TLongArrayList();
    }

    public int getRequiredVotes() {
        int listeners = (int) getGuild().getAudioManager().getConnectedChannel().getMembers().stream()
                .filter(m -> !m.getUser().isBot()).count();
        return (int) Math.ceil(listeners * .55);
    }
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public BlockingQueue<KonataTrackContext> getQueue() {
        return queue;
    }

    public void shuffle() {
        List<KonataTrackContext> tracks = new ArrayList<>();
        queue.drainTo(tracks);
        Collections.shuffle(tracks);
        queue.addAll(tracks);
        tracks.clear();
    }

    public KonataTrackContext getCurrentTrack() {
        return currentTrack;
    }

    public KonataTrackContext getPreviousTrack() {
        return previousTrack;
    }

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
    }

    public Guild getGuild() {
        return KonataBot.getInstance().getShards()[KonataBot.getInstance().getShardId(guildId)].getJDA().getGuildById(guildId);
    }

    public void setLastMessage(Message message) {
        this.lastMessageId = message.getIdLong();
    }

    public void startNext(boolean isSkipped) {
        if (repeatMode == RepeatMode.SONG && !isSkipped && currentTrack != null) {
            audioPlayer.startTrack(currentTrack.getTrack().makeClone(), false);
        } else {
            if (currentTrack != null)
                previousTrack = currentTrack;
            currentTrack = queue.poll();
            audioPlayer.startTrack(currentTrack == null ? null : currentTrack.getTrack(), false);
            if (repeatMode == RepeatMode.QUEUE && previousTrack != null)
                queue.offer(previousTrack.makeClone());
        }
        if (currentTrack == null)
            onQueueEnd();
    }

    public void offer(KonataTrackContext trackContext) {
        this.queue.offer(trackContext);
        if (audioPlayer.getPlayingTrack() == null)
            startNext(true);
    }

    public TLongList getVoteSkips() {
        return voteSkips;
    }

    public void skip() {
        startNext(true);
    }

    public int stop() {
        int removedSongs = queue.size();
        queue.clear();
        audioPlayer.stopTrack();
        return removedSongs;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        TextChannel channel = currentTrack.getChannel();
        if (channel != null && channel.canTalk()) {
            VoiceChannel vc = getGuild().getAudioManager().isAttemptingToConnect() ? getGuild().getAudioManager().getQueuedAudioConnection() : getGuild().getAudioManager().getConnectedChannel();
            if (vc == null) {
                channel.sendMessage("Oh no, this is embarrassing " + Emojis.SWEAT_SMILE + ". I lost connection with the voice channel so I stopped the audio player to avoid bugs.").queue();
                stop();
                return;
            }
            channel.sendMessage("\uD83D\uDD0A Now playing in **" + vc.getName() + "** `" + track.getInfo().title + "` (`" + AudioUtils.format(track.getDuration()) + "`) added by " + StringUtils.toString(currentTrack.getDJ())).queue(this::setLastMessage);
        }
    }

    private void onQueueEnd() {
        getGuild().getAudioManager().closeAudioConnection();
        TextChannel tc = previousTrack.getChannel();
        if (tc != null && tc.canTalk())
            tc.sendMessage("Queue has ended, disconnecting from voice channel...").queue();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        voteSkips.clear();
        if (currentTrack != null) {
            TextChannel channel = currentTrack.getChannel();
            if (channel != null && channel.canTalk())
                channel.deleteMessageById(lastMessageId).queue();
        }
        if (endReason.mayStartNext) {
            startNext(false);
            if (currentTrack == null) {
                onQueueEnd();
            }
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        TextChannel channel = currentTrack.getChannel();
        if (channel != null && channel.canTalk()) {
            String msg = ":fearful: Failed to play " + track.getInfo().title + ": `" + exception.getMessage() + "`";
            channel.getMessageById(lastMessageId).queue(message -> message.editMessage(msg).queue(), throwable -> channel.sendMessage(msg).queue());
        }
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        TextChannel channel = currentTrack.getChannel();
        if (channel != null && channel.canTalk()) {
            String msg = "Track got stuck, skipping...";
            channel.getMessageById(lastMessageId).queue(message -> message.editMessage(msg).queue(), throwable -> channel.sendMessage(msg).queue());
        }
    }

    public enum RepeatMode {
        SONG, QUEUE
    }
}
