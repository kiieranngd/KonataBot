package br.net.brjdevs.steven.konata.cmds.guildAdmin;

import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.permissions.Permissions;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PermissionsCommand {

    @RegisterCommand
    public static ICommand permissions() {
        return new ICommand.Builder()
                .setAliases("permissions", "perms")
                .setName("Permissions Command")
                .setDescription("Manages permissions in your guild")
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    String[] args = StringUtils.splitArgs(event.getArguments(), 2);
                    GuildData data = GuildData.of(event.getGuild());
                    switch (args[0]) {
                        case "set":
                        case "add":
                            if (!data.hasPermission(event.getMember(), Permissions.PERMSYS_GM)) {
                                event.sendMessage(Emojis.NO_GOOD + " You don't have the `PERMSYS_GM` permission!").queue();
                                return;
                            }
                            args = StringUtils.splitArgs(args[1], 3);
                            List<Member> members;
                            String msg;
                            switch (args[0]) {
                                case "everyone":
                                case "*":
                                    msg = "Updated everyone permissions!";
                                    members = event.getGuild().getMembers();
                                    args = new String[]{args[1].toUpperCase() + args[2].toUpperCase()};
                                    break;
                                case "role":
                                    if (args.length < 2) {
                                        event.sendMessage("You have to tell me a role name!").queue();
                                        return;
                                    }
                                    List<Role> matches = event.getGuild().getRolesByName(args[1], true);
                                    if (matches.isEmpty()) {
                                        event.sendMessage(Emojis.X + " No roles named `" + args[1] + "found! " +
                                                "Note: if the role name contains empty spaces, put it between quotation marks.").queue();
                                        return;
                                    }
                                    members = event.getGuild().getMembersWithRoles(matches.get(0));
                                    msg = "Updated members with role `" + matches.get(0).getName() + "` permissions!";
                                    args = new String[]{args.length < 3 ? "" : args[2].toUpperCase()};
                                    break;
                                default:
                                    String[] fargs = new String[]{String.join(" ", args).toUpperCase()};
                                    members = new ArrayList<>();
                                    event.getMessage().getMentionedUsers().forEach(user -> {
                                        Member member = event.getGuild().getMember(user);
                                        members.add(member);
                                        fargs[0] = fargs[0].replace(member.getAsMention(), "");
                                    });
                                    fargs[0] = fargs[0].trim();
                                    args = fargs;
                                    if (members.size() == 1)
                                        msg = "Updated " + StringUtils.toString(members.get(0).getUser()) + " permissions!";
                                    else
                                        msg = "Updated " + members.size() + " members permissions!";
                                    break;
                            }
                            long toBeSet = 0;
                            for (String perm : args[0].split(" ")) {
                                if (perm.isEmpty())
                                    continue;
                                long l = Permissions.perms.getOrDefault(perm, -1L);
                                if (l > -1) {
                                    toBeSet |= l;
                                } else {
                                    event.sendMessage(Emojis.X + " No such permission `" + perm + "`.").queue();
                                    return;
                                }
                            }
                            if (toBeSet == 0) {
                                event.sendMessage(Emojis.X + " You have to tell me permissions name! Here's a list of all the available permissions you can assign:\n```" + String.join(", ", Permissions.perms.keySet()) + "```").queue();
                                return;
                            }
                            long fToBeSet = toBeSet;
                            List<String> updated = new ArrayList<>(), failed = new ArrayList<>();
                            members.forEach(member -> {
                                if (data.setPermission(event.getMember(), member, fToBeSet, 0))
                                    updated.add(StringUtils.toString(member.getUser()));
                                else
                                    failed.add(StringUtils.toString(member.getUser()));
                            });
                            if (!updated.isEmpty())
                                event.sendMessage(Emojis.BALLOT_CHECK_MARK + " " + msg).queue();
                            else
                                event.sendMessage(Emojis.X + " You don't have enough permissions to do that!").queue();
                            data.saveAsync();
                            break;
                        case "unset":
                        case "remove":
                        case "take":
                            if (!data.hasPermission(event.getMember(), Permissions.PERMSYS_GM)) {
                                event.sendMessage(Emojis.NO_GOOD + " You don't have the `PERMSYS_GM` permission!").queue();
                                return;
                            }
                            args = StringUtils.splitArgs(args[1], 3);
                            switch (args[0]) {
                                case "everyone":
                                case "*":
                                    msg = "Updated everyone permissions!";
                                    members = event.getGuild().getMembers();
                                    args = new String[]{args[1].toUpperCase() + args[2].toUpperCase()};
                                    break;
                                case "role":
                                    if (args.length < 2) {
                                        event.sendMessage("You have to tell me a role name!").queue();
                                        return;
                                    }
                                    List<Role> matches = event.getGuild().getRolesByName(args[1], true);
                                    if (matches.isEmpty()) {
                                        event.sendMessage(Emojis.X + " No roles named `" + args[1] + "found! " +
                                                "Note: if the role name contains empty spaces, put it between quotation marks.").queue();
                                        return;
                                    }
                                    members = event.getGuild().getMembersWithRoles(matches.get(0));
                                    msg = "Updated members with role `" + matches.get(0).getName() + "` permissions!";
                                    args = new String[]{args.length < 3 ? "" : args[2].toUpperCase()};
                                    break;
                                default:
                                    String[] fargs = new String[]{String.join(" ", args).toUpperCase()};
                                    members = new ArrayList<>();
                                    event.getMessage().getMentionedUsers().forEach(user -> {
                                        Member member = event.getGuild().getMember(user);
                                        members.add(member);
                                        fargs[0] = fargs[0].replace(member.getAsMention(), "");
                                    });
                                    fargs[0] = fargs[0].trim();
                                    args = fargs;
                                    if (members.size() == 1)
                                        msg = "Updated " + StringUtils.toString(members.get(0).getUser()) + " permissions!";
                                    else
                                        msg = "Updated " + members.size() + " members permissions!";
                                    break;
                            }
                            long toBeUnset = 0;
                            for (String perm : args[0].split(" ")) {
                                if (perm.isEmpty())
                                    continue;
                                long l = Permissions.perms.getOrDefault(perm, -1L);
                                if (l > -1) {
                                    toBeUnset |= l;
                                } else {
                                    event.sendMessage(Emojis.X + " No such permission `" + perm + "`.").queue();
                                    return;
                                }
                            }
                            if (toBeUnset == 0) {
                                event.sendMessage(Emojis.X + " You have to tell me permissions name! Here's a list of all the available permissions you can assign:\n```" + String.join(", ", Permissions.perms.keySet()) + "```").queue();
                                return;
                            }
                            long fToBeUnset = toBeUnset;
                            updated = new ArrayList<>();
                            failed = new ArrayList<>();
                            members.forEach(member -> {
                                if (data.setPermission(event.getMember(), member, 0, fToBeUnset))
                                    updated.add(StringUtils.toString(member.getUser()));
                                else
                                    failed.add(StringUtils.toString(member.getUser()));
                            });
                            if (!updated.isEmpty())
                                event.sendMessage(Emojis.BALLOT_CHECK_MARK + " " +msg).queue();
                            else
                                event.sendMessage(Emojis.X + " You don't have enough permissions to do that!").queue();
                            data.saveAsync();
                            break;
                        case "get":
                            Member member = event.getMessage().getMentionedUsers().isEmpty() ? event.getMember() : event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
                            List<String> perms = Permissions.toCollection(data.getPermission(member));

                            EmbedBuilder embedBuilder = new EmbedBuilder();

                            embedBuilder.setThumbnail(member.getUser().getEffectiveAvatarUrl());
                            embedBuilder.setAuthor(StringUtils.toString(member.getUser()) + "'s permissions", null, member.getUser().getEffectiveAvatarUrl());
                            embedBuilder.setDescription(String.join(", ", perms) + "\n\nRaw: " + data.getPermission(member));

                            embedBuilder.setColor(Color.decode("#388BDF"));
                            event.sendMessage(embedBuilder.build()).queue();

                            break;
                    }
                })
                .build();
    }
}