package br.net.brjdevs.steven.konata.cmds.misc;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class UserInfoCommand {

    @RegisterCommand
    public static ICommand userinfo() {
        return new ICommand.Builder()
                .setAliases("userinfo", "user")
                .setName("User Info Command")
                .setDescription("Gives you info on the mentioned user")
                .setCategory(Category.MISCELLANEOUS)
                .setAction((event) -> {
                    User user = event.getMessage().getMentionedUsers().isEmpty() ? event.getAuthor() : event.getMessage().getMentionedUsers().get(0);
                    Member member = !event.isFromType(ChannelType.TEXT) ? null : event.getGuild().getMember(user);
                    OffsetDateTime creation = user.getCreationTime();
                    String createdAt = StringUtils.capitalize(creation.getDayOfWeek().toString().substring(0, 3)) + ", " + creation.getDayOfMonth() + " " + StringUtils.capitalize(creation.getMonth().toString().substring(0, 3)) + " " + creation.getYear() + " " + String.format("%02d", creation.getHour()) + ":" + String.format("%02d", creation.getMinute());
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle((!user.isBot() ? "\uD83D\uDC65" : Emojis.BOT_TAG) + " User information on " + StringUtils.toString(user), null);
                    if (member != null && member.getNickname() != null)
                        embed.addField("Nickname", member.getNickname(), true);
                    embed.addField("User ID", user.getId(), true);
                    if (member != null && member.getGame() != null)
                        embed.addField(member.getGame().getType() == Game.GameType.TWITCH ? "Streaming" : "Playing", member.getGame().getName(), true);
                    if (member != null) {
                        OffsetDateTime date = member.getJoinDate();
                        String joinDate = StringUtils.capitalize(date.getDayOfWeek().toString().substring(0, 3)) + ", " + date.getDayOfMonth() + " " + StringUtils.capitalize(date.getMonth().toString().substring(0, 3)) + " " + date.getYear() + " " + String.format("%02d", date.getHour()) + ":" + String.format("%02d", date.getMinute());
                        embed.addField("Member since", joinDate, true);
                    }
                    embed.addField("Account creation date", createdAt, true);
                    embed.addField("Mutual guilds", String.valueOf(Arrays.stream(KonataBot.getInstance().getShards()).mapToLong(shard -> shard.getJDA().getMutualGuilds(event.getAuthor()).size()).sum()), true);
                    if (member != null) {
                        embed.addField("Status", map(member.getOnlineStatus()) + " " + member.getOnlineStatus().name(), true);
                        embed.addField("Roles", String.valueOf(member.getRoles().size()), true);
                        List<Member> joins = new ArrayList<>(event.getGuild().getMembers());
                        joins.sort(Comparator.comparing(Member::getJoinDate));
                        int index = joins.indexOf(member);
                        int joinPos = -1;
                        index -= 3;
                        if(index < 0)
                            index = 0;
                        String str = "";
                        if (joins.get(index).equals(member)) {
                            str += "**" + joins.get(index).getUser().getName() + "**";
                            joinPos = index;
                        }
                        else
                            str += joins.get(index).getUser().getName();
                        for (int i = index + 1; i < index + 7; i++)
                        {
                            if(i >= joins.size())
                                break;
                            Member m = joins.get(i);
                            String name = m.getUser().getName();
                            if (m.equals(member)) {
                                name = "**" + name + "**";
                                joinPos = i + 1;
                            }
                            str += " > "+ name;
                        }
                        embed.addField("Join Position", String.valueOf(joinPos), true);
                        embed.addField("Join Order", str, false);
                    }
                    Color color = member != null && member.getColor() == null ? Color.decode("#FFA300") : member.getColor();
                    embed.setColor(color);
                    embed.setThumbnail(user.getEffectiveAvatarUrl());
                    embed.setFooter("Requested by " + StringUtils.toString(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl());
                    event.sendMessage(embed.build()).queue();
                })
                .build();
    }

    private static String map(OnlineStatus status) {
        switch (status) {
            case ONLINE:
                return Emojis.ONLINE;
            case DO_NOT_DISTURB:
                return Emojis.DND;
            case IDLE:
                return Emojis.AWAY;
            case INVISIBLE:
                return Emojis.INVISIBLE;
            case OFFLINE:
                return Emojis.OFFLINE;
            default:
                return "tf?";
        }
    }
}
