package br.net.brjdevs.steven.konata.cmds.misc;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
                    Member member = event.getMessage().getMentionedUsers().isEmpty() ? event.getMember() : event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
                    User user = member.getUser();
                    OffsetDateTime date = member.getJoinDate();
                    String joinDate = StringUtils.capitalize(date.getDayOfWeek().toString().substring(0, 3)) + ", " + date.getDayOfMonth() + " " + StringUtils.capitalize(date.getMonth().toString().substring(0, 3)) + " " + date.getYear() + " " + toOctalInteger(date.getHour()) + ":" + toOctalInteger(date.getMinute()) + ":" + toOctalInteger(date.getSecond());
                    OffsetDateTime creation = user.getCreationTime();
                    String createdAt = StringUtils.capitalize(creation.getDayOfWeek().toString().substring(0, 3)) + ", " + creation.getDayOfMonth() + " " + StringUtils.capitalize(creation.getMonth().toString().substring(0, 3)) + " " + creation.getYear() + " " + toOctalInteger(creation.getHour()) + ":" + toOctalInteger(creation.getMinute()) + ":" + toOctalInteger(creation.getSecond()) + " GMT";
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("\uD83D\uDC65 User information on " + StringUtils.toString(user), null);
                    embed.addField("ID", user.getId(), true);
                    if (member.getNickname() != null)
                        embed.addField("Nickname", member.getNickname(), true);
                    if (member.getGame() != null)
                        embed.addField(member.getGame().getType() == Game.GameType.TWITCH ? "Streaming" : "Playing", member.getGame().getName(), true);
                    embed.addField("Member since", joinDate, true);
                    embed.addField("Account Created At", createdAt, true);
                    embed.addField("Shared Guilds", String.valueOf(event.getJDA().getGuilds().stream().filter(guild -> guild.isMember(user)).count()), true);
                    embed.addField("Roles", String.valueOf(member.getRoles().size()), true);
                    embed.addField("Status", member.getOnlineStatus().toString(), true);
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
                    Color color = member.getColor() == null ? Color.decode("#FFA300") : member.getColor();
                    embed.setColor(color);
                    embed.addField("Join Order", str, false);
                    embed.setThumbnail(user.getEffectiveAvatarUrl());
                    embed.setFooter("Requested by " + StringUtils.toString(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl());
                    event.sendMessage(embed.build()).queue();
                })
                .build();
    }

    private static String toOctalInteger(int i) {
        return i > 9 ? String.valueOf(i) : "0" + String.valueOf(i);
    }
}
