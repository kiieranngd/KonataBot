package br.net.brjdevs.steven.konata.cmds.botAdmin;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.Shard;
import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.music.GuildMusicManager;
import br.net.brjdevs.steven.konata.core.music.KonataMusicManager;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.Utils;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class RebootCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger("Shard Reboot");

    @RegisterCommand
    public static ICommand reboot() {
        return new ICommand.Builder()
                .setAliases("reboot")
                .setName("Reboot Command")
                .setDescription("Restarts the bot or shards!")
                .setOwnerOnly(true)
                .setUsageInstruction("reboot all //restarts the bot\n" +
                        "reboot shard <shard_id> // reboots the shard")
                .setCategory(Category.BOT_ADMIN)
                .setAction((event) -> {
                    String[] args = event.getArguments().split(" ", 2);
                    switch (args[0]) {
                        case "all":
                            boolean shutdown = args.length > 1 && args[1].equals("-shutdown");
                            event.sendMessage(shutdown ? "Shutting down..." : "Restarting...").queue();
                            try {
                                prepareShutdown(shutdown);
                            } catch (Exception e) {
                                event.sendMessage(Emojis.COLD_SWEAT + " Something went wrong while preparing shutdown!").queue();
                            }
                            System.exit(shutdown ? 1 : 0);
                            break;
                        case "shard":
                            if (args.length < 2 || !args[1].matches("[0-9]+")) {
                                event.sendMessage(Emojis.SWEAT_SMILE + " You have to give me a shard to reboot!").queue();
                                return;
                            }
                            int shard = Integer.parseInt(args[1]);
                            boolean isCurrentShard = KonataBot.getInstance().getShard(event.getJDA()).getId() == shard;
                            try {
                                event.sendMessage("Restarting shard [" + shard + " / " + KonataBot.getInstance().getShards().length + "]").complete();
                                restart(KonataBot.getInstance().getShards()[shard]);
                                if (!isCurrentShard) {
                                    event.sendMessage("Restarted shard " + shard + " successfully.").queue();
                                }
                            } catch (Exception e) {
                                LOGGER.error("Failed to restart shard " + shard + ".", e);
                                if (!isCurrentShard) {
                                    event.sendMessage(Emojis.X + " Something failed when restarting the shard! `" + e.getMessage() + "`\n```" + Utils.getStackTrace(e) + "```").queue();
                                }
                            }
                    }
                })
                .build();
    }

    private static void prepareShutdown(boolean shutdown) {
        KonataMusicManager musicManager = KonataBot.getInstance().getMusicManager();
        for (long l : new TLongObjectHashMap<>(musicManager.getMusicManagers()).keys()) {
            try {
                GuildMusicManager manager = musicManager.getMusicManagers().get(l);
                musicManager.getMusicManagers().remove(l);
                if (manager.getTrackScheduler().getCurrentTrack() != null && manager.getTrackScheduler().getCurrentTrack().getChannel() != null) {
                    manager.getTrackScheduler().getCurrentTrack().getChannel().sendMessage(shutdown ? "Sorry to bother you but I'll shutdown for an unknown period of time, you can keep updated about that joining my support guild." : "I'm going to restart. Be back in a few minutes stronger and better!").complete();
                    manager.getTrackScheduler().stop();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void restart(Shard shard) throws LoginException, InterruptedException {
        TLongObjectMap<Pair<Long, GuildMusicManager>> musicManagers = new TLongObjectHashMap<>();
        new TLongObjectHashMap<>(KonataBot.getInstance().getMusicManager().getMusicManagers()).valueCollection()
                .forEach(manager -> {
                    if (manager.getGuild() != null
                            && manager.getGuild().getAudioManager().isConnected()
                            && manager.getTrackScheduler().getCurrentTrack() != null) {
                        musicManagers.put(manager.getGuild().getIdLong(), Pair.of(manager.getGuild().getAudioManager().getConnectedChannel().getIdLong(), manager));
                        manager.getAudioPlayer().setPaused(true);
                    }
                    if (manager.getGuild() != null)
                        manager.getGuild().getAudioManager().closeAudioConnection();
                    KonataBot.getInstance().getMusicManager().getMusicManagers().remove(manager.guildId);
                });
        musicManagers.valueCollection().stream().map(Pair::getValue).forEach(manager -> {
            TextChannel tc = manager.getTrackScheduler().getCurrentTrack().getChannel();
            if (tc != null && tc.canTalk())
                tc.sendMessage("I'll restart this shard, just give me a second to boot up and we will ").complete();
        });
        shard.getJDA().shutdown(true);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }
        shard.restartJDA();

        for (long l : musicManagers.keys()) {
            Guild guild = KonataBot.getInstance().getGuildById(l);
            if (guild == null)
                continue;
            VoiceChannel vc = guild.getVoiceChannelById(musicManagers.get(l).getLeft());
            if (vc == null)
                continue;
            guild.getAudioManager().openAudioConnection(vc);
            GuildMusicManager musicManager = musicManagers.get(l).getRight();
            KonataBot.getInstance().getMusicManager().getMusicManagers().put(l, musicManager);
            musicManager.getAudioPlayer().setPaused(false);
        }
    }
}
