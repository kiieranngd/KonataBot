package br.net.brjdevs.steven.konata.cmds.music;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.music.*;
import br.net.brjdevs.steven.konata.core.permissions.Permissions;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import gnu.trove.list.TLongList;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MusicCommands {
    @RegisterCommand
    public static ICommand play() {
        return new ICommand.Builder()
                .setAliases("play", "p")
                .setName("Play Command")
                .setDescription("Play songs.")
                .setUsageInstruction("play <search_term> //searches on youtube and play\n" +
                        "play <url> //plays the url\n")
                .setPrivateAvailable(false)
                .setCategory(Category.MUSIC)
                .setAction((event) -> {
                    if (event.getArguments().isEmpty()) {
                        event.sendMessage("You have to tell me a song to play!").queue();
                        return;
                    } else if (event.getGuild().getAudioManager().isConnected() && !event.getGuild().getAudioManager().getConnectedChannel().equals(event.getMember().getVoiceState().getChannel())) {
                        event.sendMessage("You are not connected to the channel I am playing!").queue();
                        return;

                    }
                    AudioLoader.loadAndPlay(event.getAuthor(), ((TextChannel) event.getChannel()), event.getArguments(), false);
                })
                .build();
    }

    @RegisterCommand
    public static ICommand forceplay() {
        return new ICommand.Builder()
                .setAliases("forceplay", "fp")
                .setName("Force Play Command")
                .setDescription("Selects the first song from a query and plays it.")
                .setUsageInstruction("forceplay <search_term> //searches on youtube and play\n" +
                        "forceplay <url> //plays the url\n")
                .setCategory(Category.MUSIC)
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    if (event.getArguments().isEmpty()) {
                        event.sendMessage("You have to tell me a song to play!").queue();
                        return;
                    } else if (event.getGuild().getAudioManager().isConnected() && !event.getGuild().getAudioManager().getConnectedChannel().equals(event.getMember().getVoiceState().getChannel())) {
                        event.sendMessage("You are not connected to the channel I am playing!").queue();
                        return;

                    }
                    AudioLoader.loadAndPlay(event.getAuthor(), ((TextChannel) event.getChannel()), event.getArguments(), true);
                })
                .build();
    }

    @RegisterCommand
    public static ICommand skip() {
        return new ICommand.Builder()
                .setAliases("skip", "s")
                .setName("Skip Command")
                .setDescription("Skips the current song!")
                .setUsageInstruction("skip //skips the current track")
                .setCategory(Category.MUSIC)
                .setPrivateAvailable(false)
                .setAction((event) -> {
                   if (KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getAudioPlayer().getPlayingTrack() == null) {
                        event.sendMessage("I'm not playing anything!").queue();
                        return;
                    } else if (event.getGuild().getAudioManager().isConnected() && !event.getGuild().getAudioManager().getConnectedChannel().equals(event.getMember().getVoiceState().getChannel())) {
                        event.sendMessage("You are not connected to the channel I am playing!").queue();
                        return;

                    }
                    TrackScheduler scheduler = KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getTrackScheduler();
                    TLongList voteSkips = scheduler.getVoteSkips();
                    int requiredVotes = scheduler.getRequiredVotes();
                    if (voteSkips.contains(event.getAuthor().getIdLong())) {
                        voteSkips.remove(event.getAuthor().getIdLong());
                        event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Well, you already voted to skip this song so I removed your vote. " + (requiredVotes - voteSkips.size()) + " more votes are necessary to skip!").queue();
                    } else {
                        voteSkips.add(event.getAuthor().getIdLong());
                        if (voteSkips.size() >= requiredVotes || isDJ(event.getMember()) || scheduler.getCurrentTrack() != null && scheduler.getCurrentTrack().getDJ() != null && scheduler.getCurrentTrack().getDJ().equals(event.getAuthor())) {
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Reached required amount of votes, skipping...").queue();
                            scheduler.skip();
                            return;
                        }
                        event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Your vote to skip this song has been submitted. " + (requiredVotes - voteSkips.size()) + " more votes are required to skip.").queue();
                    }
                })
                .build();
    }


    @RegisterCommand
    public static ICommand forceskip() {
        return new ICommand.Builder()
                .setAliases("forceskip", "fs")
                .setName("Force Skip Command")
                .setDescription("Skips the current song without votes!")
                .setUsageInstruction("fs //skips the current track")
                .setCategory(Category.MUSIC)
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    if (!isDJ(event.getMember())) {
                        event.sendMessage(Emojis.NO_GOOD + " You don't have the `DJ_OVERRIDE` permission!!").queue();
                        return;
                    } else if (KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getAudioPlayer().getPlayingTrack() == null) {
                        event.sendMessage("I'm not playing anything!").queue();
                        return;
                    }
                    TrackScheduler scheduler = KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getTrackScheduler();
                    event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Skipping...").queue();
                    scheduler.skip();
                })
                .build();
    }

    @RegisterCommand
    public static ICommand queue() {
        return new ICommand.Builder()
                .setAliases("queue", "q")
                .setName("Queue Command")
                .setDescription("Lists all the tracks in the queue.")
                .setUsageInstruction("queue <page> //lists the queue page\n" +
                        "queue remove <track_index> //removes a track from the queue")
                .setCategory(Category.MUSIC)
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    String[] args = event.getArguments().split(" ");
                    TrackScheduler scheduler = KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getTrackScheduler();
                    switch (args[0]) {
                        case "remove":
                        case "rmtrack":
                            if (args.length == 1) {
                                event.sendMessage(Emojis.X + " You have to tell me the song queue position!").queue();
                                return;
                            } else if (!args[1].matches("[0-9]+")) {
                                event.sendMessage(Emojis.X + " Uhh, `" + args[1] + "` is not a valid queue position.").queue();
                                return;
                            }
                            int i = Integer.parseInt(args[1]);
                            if (i > scheduler.getQueue().size()) {
                                event.sendMessage(Emojis.X + " The last song is at position `" + scheduler.getQueue().size() + "`.").queue();
                                return;
                            }
                            KonataTrackContext trackContext = new ArrayList<>(scheduler.getQueue()).get(i - 1);
                            if (!event.getAuthor().equals(trackContext.getDJ()) && !isDJ(event.getMember())) {
                                event.sendMessage(Emojis.NO_GOOD + " You cannot remove this track because you're not its DJ nor have a DJ role.").queue();
                                return;
                            }
                            scheduler.getQueue().remove(trackContext);
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Removed `" + trackContext.getTrack().getInfo().title + "` from queue!").queue();
                            break;
                        default:

                            int maxPages = (scheduler.getQueue().size() / 10) + (scheduler.getQueue().size() % 10 == 0 ? 0 : 1);
                            int page = 1;
                            if (args[0].matches("[0-9]+")) {
                                page = Integer.parseInt(args[0]);
                                if (page > maxPages) {
                                    event.sendMessage("Woah, the last page is " + maxPages + " " + Emojis.SWEAT_SMILE).queue();
                                    return;
                                }
                            }
                            int max = page * 10, min = max - 10;

                            List<KonataTrackContext> tracks = new ArrayList<>(scheduler.getQueue());
                            KonataTrackContext currentTrack = scheduler.getCurrentTrack();
                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setAuthor("Queue for guild " + event.getGuild().getName() + " - Page " + page + "/" + maxPages, null, event.getGuild().getIconUrl());
                            eb.setDescription((currentTrack != null ? "__**Now playing:**__ " + currentTrack.getTrack().getInfo().title + " (`" + AudioUtils.format(currentTrack.getTrack().getDuration()) + "` / `" + AudioUtils.format(currentTrack.getTrack().getDuration()) + "`) " + (currentTrack.getDJ() != null ? " DJ: " + StringUtils.toString(currentTrack.getDJ()) : "") + "\n\n" : "") + (scheduler.getRepeatMode() != null ? "\uD83D\uDD01" : "\u25b6") + " " + AudioUtils.getProgressBar(currentTrack.getTrack().getPosition(), currentTrack.getTrack().getDuration()) + tracks.stream().filter(track -> {
                                int index = tracks.indexOf(track);
                                return index < max && index >= min;
                            }).map(track -> "**#" + (tracks.indexOf(track) + 1) + "** (" + StringUtils.escapeFormatting(track.getTrack().getInfo().title) + ")[" + track.getTrack().getInfo().uri + "] (`" + AudioUtils.format(track.getTrack().getDuration()) + "`) " + (track.getDJ() != null ? " DJ: " + StringUtils.toString(track.getDJ()): "")).collect(Collectors.joining("\n")));
                            eb.setFooter("Total queue size: " + tracks.size() + " songs (Total estimated time: " + AudioUtils.format(tracks.stream().mapToLong(track -> track.getTrack().getDuration()).sum()) + ")", null);
                            eb.setColor(Color.decode("#388BDF"));
                            event.sendMessage(eb.build()).queue();
                            break;
                    }
                })
                .build();
    }

    @RegisterCommand
    public static ICommand stop() {
        return new ICommand.Builder()
                .setAliases("stop")
                .setName("Stop Command")
                .setDescription("Stops the current track and clear the queue.")
                .setCategory(Category.MUSIC)
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    if (!isDJ(event.getMember())) {
                        event.sendMessage(Emojis.NO_GOOD + " You don't have the `DJ_OVERRIDE` permission!!").queue();
                        return;
                    } else if (KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getAudioPlayer().getPlayingTrack() == null) {
                        event.sendMessage("I'm not playing anything!").queue();
                        return;
                    }
                    int removedSongs = KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getTrackScheduler().stop();
                    event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Stopped the current track and removed " + removedSongs + " songs from queue.").queue();

                })
                .build();
    }

    @RegisterCommand
    public static ICommand restart() {
        return new ICommand.Builder()
                .setAliases("restart")
                .setName("Restart Command")
                .setDescription("Restarts the current track if playing, otherwise the restarts the previous track.")
                .setCategory(Category.MUSIC)
                .setAction((event) -> {
                    if (!isDJ(event.getMember())) {
                        event.sendMessage(Emojis.NO_GOOD + " You don't have the `DJ_OVERRIDE` permission!!").queue();
                        return;
                    } else if (!KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getTrackScheduler().restart(event.getMember())) {
                        event.sendMessage("I'm haven't played anything so I can't restart!").queue();
                        return;
                    }
                    event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Restarting track...").queue();

                })
                .build();
    }

    @RegisterCommand
    public static ICommand pause() {
        return new ICommand.Builder()
                .setAliases("pause")
                .setCategory(Category.MUSIC)
                .setName("Pause Command")
                .setDescription("Pauses the audio player.")
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    if (!isDJ(event.getMember())) {
                        event.sendMessage(Emojis.NO_GOOD + " You don't have the `DJ_OVERRIDE` permission!!").queue();
                        return;
                    }
                    AudioPlayer player = KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getAudioPlayer();
                    player.setPaused(!player.isPaused());
                    event.sendMessage(Emojis.BALLOT_CHECK_MARK + (player.isPaused() ? " The player is now paused. Use `konata pause` again to unpause." : "The player is no longer paused.")).queue();
                })
                .build();
    }

    @RegisterCommand
    public static ICommand nowplaying() {
        return new ICommand.Builder()
                .setCategory(Category.MUSIC)
                .setAliases("nowplaying", "np", "n")
                .setName("Now playing Command")
                .setDescription("Gives you information on the current song.")
                .setAction((event) -> {

                    TrackScheduler scheduler = KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getTrackScheduler();
                    if (scheduler.getCurrentTrack() == null) {
                        event.sendMessage("I'm not playing anything!").queue();
                        return;
                    }
                    KonataTrackContext track = scheduler.getCurrentTrack(); EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setColor(Color.decode("#388BDF"));
                    embedBuilder.setTitle("\uD83C\uDFA7 Now playing in " + event.getGuild().getAudioManager().getConnectedChannel().getName(), track.getTrack().getInfo().uri);
                    embedBuilder.addField("Title", track.getTrack().getInfo().title + "  (`" + AudioUtils.format(track.getTrack().getPosition()) + "` / `" + AudioUtils.format(track.getTrack().getDuration()) + "`)\n"+ (scheduler.getRepeatMode() != null ? "\uD83D\uDD01" : "\u25b6") + " " + AudioUtils.getProgressBar(track.getTrack().getPosition(), track.getTrack().getDuration()), false);
                    embedBuilder.addField("Author", track.getTrack().getInfo().author, true);
                    if (track.getChannel() != null)
                        embedBuilder.addField("Requested channel", track.getChannel().getAsMention(), true);
                    embedBuilder.addField("DJ", StringUtils.toString(track.getDJ()), true);
                    if (!scheduler.getQueue().isEmpty()) {
                        track = scheduler.getQueue().peek();
                        embedBuilder.setFooter("Next up: " + track.getTrack().getInfo().title + " (" + AudioUtils.format(track.getTrack().getDuration()) + ")", null);
                    }
                    event.sendMessage(embedBuilder.build()).queue();

                })
                .build();
    }

    @RegisterCommand
    public static ICommand repeat() {
        return new ICommand.Builder()
                .setCategory(Category.MUSIC)
                .setAliases("repeat")
                .setName("Repeat Command")
                .setDescription("Toggles the repeat mode.")
                .setUsageInstruction("repeat queue //toggles the queue repeat mode\n" +
                        "repeat song //toggles the song repeat mode\n" +
                        "repeat //toggles the song repeat mode aswell (default)")
                .setPrivateAvailable(false)
                .setAction((event) -> {
                   if (!isDJ(event.getMember())) {
                        event.sendMessage(Emojis.NO_GOOD + " You don't have the `DJ_OVERRIDE` permission!!").queue();
                        return;
                    }
                    TrackScheduler scheduler = KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getTrackScheduler();
                    switch (event.getArguments()) {
                        case "queue":
                            if (TrackScheduler.RepeatMode.QUEUE.equals(scheduler.getRepeatMode())) {
                                scheduler.setRepeatMode(null);
                                event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Toggled the repeat queue mode off!").queue();
                            } else {
                                scheduler.setRepeatMode(TrackScheduler.RepeatMode.QUEUE);
                                event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Toggled the repeat queue mode on!").queue();
                            }
                            break;
                        default:
                            if (scheduler.getRepeatMode() != null) {
                                scheduler.setRepeatMode(null);
                                event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Toggled the repeat mode off!").queue();
                            } else {
                                scheduler.setRepeatMode(TrackScheduler.RepeatMode.SONG);
                                event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Toggled the repeat song mode on!").queue();
                            }
                            break;
                    }
                })
                .build();
    }

    @RegisterCommand
    public static ICommand shuffle() {
        return new ICommand.Builder()
                .setAliases("shuffle")
                .setName("Shuffle Command")
                .setDescription("Shuffles the queue")
                .setCategory(Category.MUSIC)
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    if (!isDJ(event.getMember())) {
                        event.sendMessage(Emojis.NO_GOOD + " You don't have the `DJ_OVERRIDE` permission!!").queue();
                        return;
                    }
                    KonataBot.getInstance().getMusicManager().getMusicManager(event.getGuild()).getTrackScheduler().shuffle();
                    event.sendMessage(Emojis.OK_HAND + " Shuffled queue!").queue();
                })
                .build();
    }

    public static boolean isDJ(Member member) {
        return GuildData.of(member.getGuild()).hasPermission(member, Permissions.DJ_OVERRIDE);
    }
}
