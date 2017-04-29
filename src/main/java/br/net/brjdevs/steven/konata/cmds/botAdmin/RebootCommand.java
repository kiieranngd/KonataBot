package br.net.brjdevs.steven.konata.cmds.botAdmin;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.music.GuildMusicManager;
import br.net.brjdevs.steven.konata.core.music.KonataMusicManager;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import gnu.trove.map.hash.TLongObjectHashMap;

public class RebootCommand {

    @RegisterCommand
    public static ICommand reboot() {
        return new ICommand.Builder()
                .setAliases("reboot")
                .setName("Reboot Command")
                .setDescription("Restarts the bot or shards!")
                .setOwnerOnly(true)
                .setUsageInstruction("reboot all //restarts the bot\n" +
                        "reboot shard <shard_id> // reboots the shard")
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
                            event.sendMessage("If you were not so lazy you'd have finished this. But you didn't, you suck. If a shard died you are fucked.").queue();
                            break;
                            //if (args.length < 2 || !args[1].matches("[0-9]+")) {
                                //event.sendMessage(Emojis.SWEAT_SMILE + " You have to give me a shard to reboot!").queue();
                                //return;
                            //}
                            //int shard = Integer.parseInt(args[1]);
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

        musicManager.getRadioFeeder().getQueue().clear();
        musicManager.getRadioFeeder().getAudioPlayer().stopTrack();
    }
}
