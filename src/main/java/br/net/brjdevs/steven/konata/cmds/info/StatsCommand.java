package br.net.brjdevs.steven.konata.cmds.info;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.SessionMonitor;
import br.net.brjdevs.steven.konata.Shard;
import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatsCommand {

    @RegisterCommand
    public static ICommand stats() {
        return new ICommand.Builder()
                .setAliases("stats", "status")
                .setName("Status Command")
                .setDescription("View the bot, shard and other status!")
                .setUsageInstruction("stats [bot/shard/guilds/cmds]")
                .setAction((event) -> {
                    String[] args = StringUtils.splitArgs(event.getArguments(), 2);
                    if (args.length == 0)
                        args = new String[]{""};
                    switch (args[0]) {
                        case "":
                        case "bot":
                            List<Guild> guilds = KonataBot.getInstance().getGuilds();
                            List<User> users = KonataBot.getInstance().getUsers();
                            List<TextChannel> textChannels = KonataBot.getInstance().getTextChannels();
                            List<VoiceChannel> voiceChannels = KonataBot.getInstance().getVoiceChannels();

                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setAuthor("Konata " + KonataBot.VERSION + " status", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                            embedBuilder.setDescription("I've been awake for " + SessionMonitor.getUptime());
                            embedBuilder.addField(":homes: Guilds", String.valueOf(guilds.size()), true);
                            embedBuilder.addField(":busts_in_silhouette: Users", String.valueOf(users.size()), true);
                            embedBuilder.addField(":pencil: Text channels", String.valueOf(textChannels.size()), true);
                            embedBuilder.addField(":mega: Voice channels", String.valueOf(voiceChannels.size()), true);
                            embedBuilder.addField(":small_blue_diamond: Shards (C/T) ", KonataBot.getInstance().getConnectedShards().length + "/" + KonataBot.getInstance().getShards().length, true);
                            embedBuilder.addField("<:jda:230988580904763393> JDA version",JDAInfo.VERSION, true);
                            embedBuilder.addField(":globe_with_meridians: Audio connections", String.valueOf(guilds.stream().filter(guild -> guild.getAudioManager().isConnected()).count()), true);
                            embedBuilder.addField(":notes: Total queue size", String.valueOf(KonataBot.getInstance().getMusicManager().getMusicManagers().valueCollection().stream().mapToLong(musicManager -> musicManager.getTrackScheduler().getQueue().size()).sum()), true);
                            embedBuilder.addField("\uD83D\uDCFB Radio subscribers", String.valueOf(KonataBot.getInstance().getMusicManager().getRadioFeeder().getSubscribers().size()), true);
                            embedBuilder.setColor(Color.decode("#388BDF"));

                            event.sendMessage(embedBuilder.build()).queue();
                            break;
                        case "shard":
                        case "shards":

                            if (args.length < 2) {
                                int connectedShards = KonataBot.getInstance().getConnectedShards().length;
                                event.sendMessage("```diff\n" +
                                        (Stream.of(KonataBot.getInstance().getShards())).map(shard -> {
                                            JDA.Status status = shard.getJDA().getStatus();
                                            return (status == JDA.Status.CONNECTED ? "+" : "-") + " Shard [ " + shard.getId() + " / "  + shard.getShardTotal() + " ] " + status + " - Last event: " + StringUtils.parseTime(System.currentTimeMillis() - KonataBot.getInstance().getLastEvents().get(shard.getId())) + " - Guilds: " + shard.getJDA().getGuilds().size();
                                        }).collect(Collectors.joining("\n")) +
                                        "\n\n" + (connectedShards != KonataBot.getInstance().getShards().length ? "- " +(KonataBot.getInstance().getShards().length - connectedShards) + " shards are not connected." : "+ All shards are connected.") + "```"
                                ).queue();
                                break;
                            } else if (!args[1].matches("0-9")) {
                                event.sendMessage(Emojis.NO_GOOD + " That's not a valid shard id.").queue();
                                break;
                            } else {
                                int i = Integer.parseInt(args[1]);
                                Shard shard = KonataBot.getInstance().getShards()[i];
                                event.sendMessage("```prolog\n Shard [ " + shard.getId() + " / " + shard.getShardTotal() + " ]\n" +
                                        "       Last event: " + StringUtils.parseTime(System.currentTimeMillis() - KonataBot.getInstance().getLastEvents().get(shard.getId())) +
                                        "       Guilds: " + shard.getJDA().getGuilds().size() +
                                        "       Users: " + shard.getJDA().getUsers().size() +
                                        "       Text Channels: " + shard.getJDA().getTextChannels().size() +
                                        "       Voice Channels: " + shard.getJDA().getVoiceChannels().size() +
                                        "       Voice Connections: " + shard.getJDA().getGuilds().stream().filter(guild -> guild.getAudioManager().isConnected()).count() +
                                        "```"
                                ).queue();
                            }
                            break;

                    }
                })
                .build();
    }

    private static final Random r = new Random();

    @RegisterCommand
    public static ICommand ping() {
        return new ICommand.Builder()
                .setCategory(Category.INFORMATIVE)
                .setName("Ping Command")
                .setDescription("Pong.")
                .setAction((event) -> {
                    long start = System.currentTimeMillis();
                    event.getChannel().sendTyping().queue(sent -> {
                        long ping = System.currentTimeMillis() - start;
                        char c = new char[] {'a', 'e', 'i', 'o', 'u'}[r.nextInt(5)];
                        event.getChannel().sendMessage("\uD83C\uDFD3 P" + c + "ng: `" + ping + "ms`\n\uD83D\uDC93 Heartbeat: `" + event.getJDA().getPing() + "ms`").queue();
                    });
                })
                .build();
    }
}
