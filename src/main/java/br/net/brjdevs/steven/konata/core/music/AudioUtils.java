package br.net.brjdevs.steven.konata.core.music;

import br.net.brjdevs.steven.konata.core.utils.Emojis;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

public class AudioUtils {

    private final static String BLOCK_INACTIVE = "\u25AC";
    private final static String BLOCK_ACTIVE = "\uD83D\uDD18";
    private static final int TOTAL_BLOCKS = 10;

    public static String getProgressBar(long percent, long duration) {
        int activeBlocks = (int) ((float) percent / duration * TOTAL_BLOCKS);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < TOTAL_BLOCKS; i++) builder.append(activeBlocks == i ? BLOCK_ACTIVE : BLOCK_INACTIVE);
        return builder.append(BLOCK_INACTIVE).toString();
    }

    public static String format(long length) {
        long hours = length / 3600000L % 24,
                minutes = length / 60000L % 60,
                seconds = length / 1000L % 60;
        return (hours == 0 ? "" : octal(hours) + ":")
                + (minutes == 0 ? "00" : octal(minutes)) + ":" + (seconds == 0 ? "00" : octal(seconds));
    }
    public static String octal(long num) {
        if (num > 9) return String.valueOf(num);
        return "0" + num;
    }

    public static boolean connect(TextChannel tc, Member member) {
        if (tc.getGuild().getAudioManager().isConnected() || tc.getGuild().getAudioManager().isAttemptingToConnect()) {
            while (!tc.getGuild().getAudioManager().isConnected()) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }
            }
            if (!tc.getGuild().getAudioManager().getConnectedChannel().equals(member.getVoiceState().getChannel())) {
                tc.sendMessage(Emojis.X + " You are not connected to the channel I am playing!").queue();
                return false;
            }
            return true;
        }
        if (!member.getVoiceState().inVoiceChannel()) {
            tc.sendMessage(Emojis.X + " You are not connected to a voice channel!").queue();
            return false;
        }
        VoiceChannel vc = member.getVoiceState().getChannel();
        if (!tc.getGuild().getSelfMember().hasPermission(vc, Permission.VOICE_CONNECT)) {
            tc.sendMessage(Emojis.CONFUSED + " I don't have permissions to join " + vc.getName() + "! (VOICE_CONNECT)").queue();
            return false;
        } else if (vc.getUserLimit() > 0 && vc.getMembers().size() > vc.getUserLimit() && !tc.getGuild().getSelfMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            tc.sendMessage(Emojis.CONFUSED + " I cannot join " + vc.getName() + " because it has reached the user limit!").queue();
            return false;
        }
        Message sent = tc.sendMessage("Connecting to `" + vc.getName() + "`...").complete();
        tc.getGuild().getAudioManager().openAudioConnection(vc);
        sent.editMessage(Emojis.CHECK_MARK + " Connected to " + vc.getName() + ".").queue();
        while (!tc.getGuild().getAudioManager().isConnected()) {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
            }
        }
        return true;
    }

}
