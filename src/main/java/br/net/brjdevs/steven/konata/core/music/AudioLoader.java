package br.net.brjdevs.steven.konata.core.music;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.interactivechoice.InteractiveChoice;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioLoader implements AudioLoadResultHandler {

    public static final long MAX_SONG_LENGTH = 10800000, MAX_PLAYLIST_LENGTH = 54000000;

    public static void loadAndPlay(User user, TextChannel tc, String search, boolean force) {
        KonataBot.getInstance().getMusicManager().getPlayerManager().loadItem(search, new AudioLoader(user, tc, search, force));
    }

    private AudioLoader(User user, TextChannel channel, String search, boolean force) {
        this.user = user;
        this.channel = channel;
        this.search = search;
        this.force = force;
    }

    private boolean force;
    private TextChannel channel;
    private String search;
    private User user;

    @Override
    public void trackLoaded(AudioTrack track) {
        GuildData data = GuildData.of(channel.getGuild());
        if (track.getDuration() > data.getMaxSongLength()) {
            channel.sendMessage("You can't add songs longer than " + AudioUtils.format(data.getMaxSongLength()) + " " + Emojis.SWEAT_SMILE).queue();
            return;
        }
        Member member = channel.getGuild().getMember(user);
        if (AudioUtils.connect(channel, member)) {
            TrackScheduler scheduler = KonataBot.getInstance().getMusicManager().getMusicManager(channel.getGuild()).getTrackScheduler();
            scheduler.offer(new KonataTrackContext(track, user, channel, KonataBot.getInstance().getShard(user.getJDA())));
            channel.sendMessage(user.getAsMention() + " has added `" + track.getInfo().title + "` to the queue! (`" + AudioUtils.format(track.getDuration()) + "`) [`" + scheduler.getQueue().size() + "`]").queue();
        }
    }
    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlist.isSearchResult()) {
            if (force) {
                trackLoaded(playlist.getTracks().get(0));
                return;
            }
            AudioTrack[] options = playlist.getTracks().stream().limit(3).toArray(AudioTrack[]::new);
            String[] responses = new String[options.length];
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(StringUtils.toString(user)).append("] Results found by `").append((search.startsWith("ytsearch:") ? search.substring(9) : search).trim()).append("`:\n");
            for (int i = 0; i < options.length; i++) {
                responses[i] = String.valueOf(i + 1);
                sb.append("**#").append(i + 1).append("** ").append(options[i].getInfo().title).append(" (`").append(AudioUtils.format(options[i].getDuration())).append("`)").append("\n");
            }
            sb.append("Type ").append(StringUtils.replaceLast(String.join(", ", responses), ", ", " or ")).append(" to pick a song!");
            channel.sendMessage(sb.toString()).queue(message ->
                new InteractiveChoice.Builder()
                        .setUser(user)
                        .setChannel(channel)
                        .expireIn(30000)
                        .setAcceptedResponses(responses)
                        .onInvalidResonse((interactiveChoice, s) ->
                                message.editMessage("Well, `" + s + "` doesn't look like a valid option " + Emojis.SWEAT_SMILE).queue())
                        .onValidResonse((interactiveChoice, s) -> {
                            message.delete().queue();
                            trackLoaded(options[Integer.parseInt(s) - 1]);
                        })
                        .onTimeout((interactiveChoice) -> message.editMessage("\u23f1 You didn't reply in 30 seconds, query canceled!").queue())
                        .build()
            );
            return;
        }
        long length = playlist.getTracks().stream().mapToLong(AudioTrack::getDuration).sum();
        if (length > MAX_PLAYLIST_LENGTH) {
            channel.sendMessage("You cannot add playlists longer than 15 hours " + Emojis.SWEAT_SMILE).queue();
            return;
        }
        if (AudioUtils.connect(channel, channel.getGuild().getMember(user))) {
            TrackScheduler scheduler = KonataBot.getInstance().getMusicManager().getMusicManager(channel.getGuild()).getTrackScheduler();
            playlist.getTracks().forEach(audioTrack -> scheduler.offer(new KonataTrackContext(audioTrack, user, channel, KonataBot.getInstance().getShard(user.getJDA()))));
        }

    }
    @Override
    public void noMatches() {
        if (!search.startsWith("ytsearch:")) {
            loadAndPlay(user, channel, "ytsearch:" + search, force);
            return;
        }
        channel.sendMessage(Emojis.X + " I didn't find anything matching the criteria `" + (search.startsWith("ytsearch:") ? search.substring(9) : search).trim() + "`.").queue();
    }
    @Override
    public void loadFailed(FriendlyException exception) {
        channel.sendMessage("Well, this is embarrassing. I failed to load the song: `" + exception.getMessage() + "` " + Emojis.SWEAT_SMILE).queue();
    }
}