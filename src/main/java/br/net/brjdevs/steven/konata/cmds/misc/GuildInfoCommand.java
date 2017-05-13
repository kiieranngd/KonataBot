package br.net.brjdevs.steven.konata.cmds.misc;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class GuildInfoCommand {

    @RegisterCommand
    public static ICommand guild() {
        return new ICommand.Builder()
                .setAliases("guild", "guildinfo")
                .setCategory(Category.MISCELLANEOUS)
                .setName("Guild Info Command")
                .setDescription("Gives you info about the given guild id.")
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    if (!event.getGuild().getSelfMember().hasPermission(((TextChannel) event.getChannel()), Permission.MESSAGE_EMBED_LINKS)) {
                        event.sendMessage("I need to have MESSAGE_EMBED_LINKS permission to send this message!").queue();
                        return;
                    }
                    Guild guild = event.getGuild();
                    Member guildOwner = guild.getOwner();
                    OffsetDateTime creation = guild.getCreationTime();
                    String creationDate = StringUtils.capitalize(creation.getDayOfWeek().toString().substring(0, 3)) + ", " + creation.getDayOfMonth() + " " + StringUtils.capitalize(creation.getMonth().toString().substring(0, 3)) + " " + creation.getYear() + " " + String.format("%02d", creation.getHour()) + ":" + String.format("%02d", creation.getMinute());
                    boolean hasEmotes = !guild.getEmotes().isEmpty();
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setThumbnail(guild.getIconUrl());
                    embedBuilder.setFooter("Requested by " + StringUtils.toString(event.getAuthor()),
                            event.getAuthor().getEffectiveAvatarUrl());
                    embedBuilder.setColor(Color.decode("#388BDF"));
                    embedBuilder.setTitle("Guild information on " + guild.getName(), null);
                    embedBuilder.addField("Owner", StringUtils.toString(guildOwner.getUser()), true);
                    embedBuilder.addField("ID", guild.getId(), true);
                    embedBuilder.addField("Region", guild.getRegion().toString(), true);
                    embedBuilder.addField("Created at", creationDate, true);
                    List<Member> online = guild.getMembers().stream().filter(m -> m.getOnlineStatus() == OnlineStatus.ONLINE).collect(Collectors.toList());
                    embedBuilder.addField("Text channels", String.valueOf(guild.getTextChannels().size()), true);
                    embedBuilder.addField("Members", String.valueOf(guild.getMembers().size()) + " (Online: " + online.size() + ")", true);
                    embedBuilder.addField("Voice channels", String.valueOf(guild.getVoiceChannels().size()), true);
                    embedBuilder.addField("Emotes count", String.valueOf(guild.getEmotes().size()), true);
                    embedBuilder.addField("Role count", String.valueOf(guild.getRoles().size()), true);
                    List<Role> roles = guild.getRoles().stream()
                            .filter(role -> !role.getName().equals("@everyone")).collect(Collectors.toList());
                    String strRoles = roles.stream().map(Role::getName).collect(Collectors.joining(", "));
                    if (strRoles.length() <= MessageEmbed.VALUE_MAX_LENGTH)
                        embedBuilder.addField("Roles", strRoles, false);
                    if (hasEmotes) {
                        List<Emote> emotes = guild.getEmotes();
                        String strEmotes = emotes.stream().map(Emote::getAsMention).collect(Collectors.joining(", "));
                        if (strEmotes.length() <= MessageEmbed.VALUE_MAX_LENGTH)
                            embedBuilder.addField("Emotes", strEmotes, true);
                    }
                    event.sendMessage(embedBuilder.build()).queue();
                })
                .build();
    }
}
