package br.net.brjdevs.steven.konata.core.music.radio;

import br.net.brjdevs.steven.konata.KonataBot;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class Subscriber implements AudioSendHandler {
    private DequeBuffer<AudioFrame> buffer = new DequeBuffer<>(() -> {
        if (getActiveVoiceChannel() != null
                && !KonataBot.getInstance().getMusicManager().getRadioFeeder().isPlaying()
                && !getActiveVoiceChannel().getMembers().isEmpty()) {
            disconnect();
        }
        if (getActiveVoiceChannel() == null || getActiveVoiceChannel().getMembers().isEmpty()) {
            KonataBot.getInstance().getMusicManager().getRadioFeeder().getSubscribers().remove(this);
        }
    });
    private AudioFrame lastFrame;
    private long activeVoiceChannel;
    private int shardId;

    public Subscriber(VoiceChannel voiceChannel) {
        this.activeVoiceChannel = voiceChannel.getIdLong();
        this.shardId = KonataBot.getInstance().getShardId(voiceChannel.getJDA());
    }

    public VoiceChannel getActiveVoiceChannel() {
        return KonataBot.getInstance().getShards()[shardId].getJDA().getVoiceChannelById(activeVoiceChannel);
    }

    public void connect() {
        VoiceChannel voiceChannel = getActiveVoiceChannel();
        voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
        if (voiceChannel.getGuild().getAudioManager().getSendingHandler() == null || !(voiceChannel.getGuild().getAudioManager().getSendingHandler() instanceof Subscriber))
            voiceChannel.getGuild().getAudioManager().setSendingHandler(this);
    }

    public void disconnect() {
        getActiveVoiceChannel().getGuild().getAudioManager().closeAudioConnection();
    }

    public boolean isConnected() {
        return getActiveVoiceChannel().getGuild().getAudioManager().isConnected();
    }

    public void setActiveVoiceChannel(VoiceChannel activeVoiceChannel) {
        this.activeVoiceChannel = activeVoiceChannel.getIdLong();
    }
    void feed(AudioFrame frame) {
        buffer.add(frame);
    }

    public DequeBuffer<AudioFrame> getBuffer() {
        return buffer;
    }
    @Override
    public boolean canProvide() {
        if (lastFrame == null)
            lastFrame = buffer.poll();

        return lastFrame != null;
    }
    @Override
    public byte[] provide20MsAudio() {
        if (lastFrame == null) {
            lastFrame = buffer.poll();
        }

        byte[] data = lastFrame != null ? lastFrame.data : null;
        lastFrame = null;
        return data;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
