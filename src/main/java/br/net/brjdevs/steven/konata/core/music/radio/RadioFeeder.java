package br.net.brjdevs.steven.konata.core.music.radio;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.music.AudioUtils;
import br.net.brjdevs.steven.konata.core.music.PlayerSendHandler;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.IOUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RadioFeeder extends AudioEventAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger("Radio");

    public static final List<String> FILTERED_WORDS;

    static {
        FILTERED_WORDS = new ArrayList<>(Arrays.asList("rape", "orgasm", "sex", "porn", "dick", "pussy", "vagina", "estupro", "orgasmo", "sexo", "porno", "pau", "pinto"));
    }

    private List<Subscriber> subscribers;
    private BlockingQueue<RadioTrack> queue = new LinkedBlockingQueue<>();
    private RadioTrack currentTrack;
    private AudioPlayer audioPlayer;

    public RadioFeeder(AudioPlayerManager playerManager) {
        this.subscribers = new ArrayList<>();
        this.audioPlayer = playerManager.createPlayer();
        audioPlayer.addListener(this);


        Thread t = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    tick();
                    Thread.sleep(16);
                } catch (Exception e) {
                    LOGGER.error("An exception occurred while streaming radio", e);
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }


    public boolean isPlaying() {
        return currentTrack != null;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    public boolean startNextTrack(boolean noInterrupt) {
        currentTrack = queue.peek();
        boolean b = audioPlayer.startTrack(currentTrack == null ? null : currentTrack.getTrack(), noInterrupt);
        if (b)
            queue.poll();
        return b;
    }

    public void unsubscribe(Guild guild) {
        Subscriber subscriber = ((Subscriber) guild.getAudioManager().getSendingHandler());
        subscribers.remove(subscriber);
        guild.getAudioManager().setSendingHandler(null);
    }

    public void subscribe(Guild guild, VoiceChannel voiceChannel) {
        Subscriber subscriber = new Subscriber(voiceChannel);
        guild.getAudioManager().setSendingHandler(subscriber);
        subscribers.add(subscriber);
    }

    public boolean isSubscribed(Guild guild) {
        return guild.getAudioManager().getSendingHandler() instanceof Subscriber && subscribers.contains(guild.getAudioManager().getSendingHandler());
    }

    public BlockingQueue<RadioTrack> getQueue() {
        return queue;
    }


    public int getAverageBufferSize() {
        if (subscribers.isEmpty())
            return -1;
        return subscribers.stream().mapToInt(s -> s.getBuffer().size()).sum() / subscribers.size();
    }

    private void tick() {
        if (getAverageBufferSize() > 150)
            return;

        AudioFrame frame = audioPlayer.provide();

        if (subscribers.isEmpty())
            return;

        if (frame != null) {
            List<Subscriber> toUnsubscribe = new ArrayList<>();
            subscribers.forEach(subscriber -> {
                if (subscriber.getBuffer().size() > 250) {
                    toUnsubscribe.add(subscriber);
                } else
                    subscriber.feed(frame);
            });
            toUnsubscribe.forEach(subscribers::remove);
        }
    }

    public void loadAndPlay(TextChannel textChannel, User user, String search) {
        KonataBot.getInstance().getMusicManager().getPlayerManager().loadItem(search, new RadioTrackLoader(textChannel, user, search));
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            startNextTrack(false);
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        List<Subscriber> toUnregister = new ArrayList<>();

        subscribers.stream().filter(subscriber -> {
            try {
                return !subscriber.isConnected() && !subscriber.getActiveVoiceChannel().getMembers().isEmpty();
            } catch (Exception e) {
                toUnregister.add(subscriber);
                return false;
            }
        }).forEach(Subscriber::connect);

        subscribers.removeAll(toUnregister);
    }

    public RadioTrack getCurrentTrack() {
        return currentTrack;
    }
    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        LOGGER.info("Got onTrackException at track `" + track.getInfo().title + "`");
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        LOGGER.info("Track `" + track.getInfo().title + "` got stuck.");
    }


    public static class RadioTrackLoader implements AudioLoadResultHandler {

        private TextChannel textChannel;
        private User dj;
        private String search;

        public RadioTrackLoader(TextChannel textChannel, User dj, String search) {
            this.textChannel = textChannel;
            this.dj = dj;
            this.search = search;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            if (FILTERED_WORDS.stream().anyMatch(s -> track.getInfo().title.toLowerCase().contains(s.toLowerCase()))) {
                textChannel.sendMessage(Emojis.NO_GOOD + " This song's title contains a filtered word.").queue();
                return;
            }
            if (track.getDuration() > 480000) {
                textChannel.sendMessage("You cannot request songs longer than 8 minutes!").queue();
                return;
            }
            RadioFeeder feeder = KonataBot.getInstance().getMusicManager().getRadioFeeder();
            feeder.getQueue().offer(new RadioTrack(track, textChannel.getGuild(), dj));
            if (!feeder.startNextTrack(true)) {
                textChannel.sendMessage(dj.getAsMention() + " has added `" + track.getInfo().title + "` to the queue! (`" + AudioUtils.format(track.getDuration()) + "`) [`" + feeder.getQueue().size() + "`]").queue();
            }
        }
        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (playlist.isSearchResult())
                trackLoaded(playlist.getTracks().get(0));
            else
                textChannel.sendMessage(Emojis.RAISED_HAND + " You cannot request playlists in radio mode!").queue();
        }
        @Override
        public void noMatches() {
            if (!search.startsWith("ytsearch:")) {
                RadioFeeder feeder = KonataBot.getInstance().getMusicManager().getRadioFeeder();
                feeder.loadAndPlay(textChannel, dj, "ytsearch:" + search);
                return;
            }
            textChannel.sendMessage("Nothing found matching that criteria.").queue();
        }
        @Override
        public void loadFailed(FriendlyException exception) {
            textChannel.sendMessage(Emojis.SWEAT_SMILE + " Something failed when trying to load track! `" + exception.getMessage() + "`").queue();
        }
    }
}
