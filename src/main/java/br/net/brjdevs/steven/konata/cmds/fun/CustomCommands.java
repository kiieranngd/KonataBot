package br.net.brjdevs.steven.konata.cmds.fun;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.CommandManager;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.DataManager;
import br.net.brjdevs.steven.konata.core.data.guild.CustomCommand;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.permissions.Permissions;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomCommands {

    @RegisterCommand
    public static ICommand customcmds() {
        return new ICommand.Builder()
                .setCategory(Category.FUN)
                .setAliases("custom", "cmds", "customcommands")
                .setName("Custom Commands")
                .setDescription("Manages custom commands!")
                .setUsageInstruction(
                        "cmds list <page>\n" +
                                "cmds create [command_name] [command answer]\n" +
                                "\\*cmds delete [command_name]\n" +
                                "\\*cmds addanswer [command_name] [answer]\n" +
                                "\\*cmds rmanswer [command_name] [answer_index]\n" +
                                "\n" +
                                "\\* *Requires you to be the command creator or have MANAGE_SERVER.*"
                )
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    String[] args = event.getArguments().split(" ", 3);

                    switch (args[0]) {
                        case "list":
                            Map<String, CustomCommand> cmds = DataManager.db().getCustomCommands(event.getGuild()).stream().collect(Collectors.toMap(CustomCommand::getName, cmd -> cmd));
                            List<CustomCommand> commands = cmds.values().stream().sorted(Comparator.comparingLong(CustomCommand::getCreationDate)).collect(Collectors.toList());
                            if (commands.isEmpty()) {
                                event.sendMessage("\u2139 This guild doesn't have any custom commands.").queue();
                                return;
                            }
                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setColor(Color.decode("#388BDF"));
                            int maxPages = (cmds.size() / 10) + (cmds.size() % 10 == 0 ? 0 : 1);
                            int page = 1;
                            if (args.length > 1 && args[1].matches("[0-9]+")) {
                                page = Integer.parseInt(args[1]);
                                if (page > maxPages) {
                                    event.sendMessage("Woah, the last page is " + maxPages + " " + Emojis.SWEAT_SMILE).queue();
                                    return;
                                }
                            }
                            eb.setAuthor("Custom commands list for " + event.getGuild().getName() + " - Page " + page + "/" + maxPages, null, event.getGuild().getIconUrl());
                            int max = page * 10, min = max - 10;
                            eb.setDescription(commands.stream().filter(cmd -> {
                                int index = commands.indexOf(cmd);
                                return index < max && index >= min;
                            }).map(cmd -> "**#" + (commands.indexOf(cmd) + 1) + "** " + cmd.getName() + " - Created by `" + StringUtils.toString(event.getJDA().getUserById(cmd.getCreatorId())) + "`").collect(Collectors.joining("\n")));
                            eb.setFooter("Total custom commands: " + commands.size(), null);
                            event.sendMessage(eb.build()).queue();
                            break;
                        case "create":
                        case "new":
                            if (DataManager.db().getCustomCommand(event.getGuild(), args[1]) != null) {
                                event.sendMessage("Oh well, this guild already has a command with this name. If you want, you can add more answers to it using `konata cmds addanswer " + args[1] + " " + args[2] + "` " + Emojis.WINK).queue();
                                return;
                            }
                            if (args[1].isEmpty() || args[2].isEmpty()) {
                                event.sendMessage(CommandManager.getHelp(event.getCommand(), event.getMember())).queue();
                                return;
                            }
                            new CustomCommand(event.getGuild(), event.getMember(), args[1], args[2]).saveAsync();
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Created custom command " + args[1] + ", you can add more answers to it if you'd like using `konata cmds addanswer " + args[1] + " [answer]` " + Emojis.WINK).queue();
                            break;
                        case "delete":
                        case "del":
                        case "remove":
                            CustomCommand cmd = DataManager.db().getCustomCommand(event.getGuild(), args[1]);
                            if (cmd == null) {
                                event.sendMessage("Oops, this guild doesn't have a custom command with that name! " + Emojis.SWEAT_SMILE).queue();
                                return;
                            }
                            if (!cmd.getCreatorId().equals(event.getAuthor().getId()) && !GuildData.of(event.getGuild()).hasPermission(event.getMember(), Permissions.CUSTOM_CMDS_MANAGE)) {
                                event.sendMessage(Emojis.NO_GOOD + " Nope, you cannot delete this command because you are not its owner and you don't have `CUSTOM_CMDS_MANAGE` permission.").queue();
                                return;
                            }
                            cmd.deleteAsync();
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Deleted custom command `" + args[1] + "`.").queue();
                            break;
                        case "addanswer":
                            cmd = DataManager.db().getCustomCommand(event.getGuild(), args[1]);
                            if (cmd == null) {
                                event.sendMessage("Oops, this guild doesn't have a custom command with that name! " + Emojis.SWEAT_SMILE).queue();
                                return;
                            }
                            if (!cmd.getCreatorId().equals(event.getAuthor().getId()) && !GuildData.of(event.getGuild()).hasPermission(event.getMember(), Permissions.CUSTOM_CMDS_MANAGE)) {
                                event.sendMessage(Emojis.NO_GOOD + " Nope, you cannot delete this command because you are not its owner and you don't have `CUSTOM_CMDS_MANAGE` permission.").queue();
                                return;
                            } else if (cmd.getAnswers().contains(args[2])) {
                                event.sendMessage("Oh well, this command already has this answer! " + Emojis.SWEAT_SMILE).queue();
                                return;
                            }
                            cmd.getAnswers().add(args[2]);
                            cmd.saveAsync();
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Added another answer to this custom command!").queue();
                            break;
                        case "rmanswer":
                            if (!args[2].matches("[0-9]+")) {
                                event.sendMessage(Emojis.X + " This is not a valid number.").queue();
                                return;
                            }
                            cmd = DataManager.db().getCustomCommand(event.getGuild(), args[1]);
                            if (cmd == null) {
                                event.sendMessage("Oops, this guild doesn't have a custom command with that name! " + Emojis.SWEAT_SMILE).queue();
                                return;
                            }
                            int i = Integer.parseInt(args[2]);
                            if (i > cmd.getAnswers().size()) {
                                event.sendMessage("Nope, the last answer index is " + cmd.getAnswers().size() + "! " + Emojis.SWEAT_SMILE).queue();
                                return;
                            }
                            if (!cmd.getCreatorId().equals(event.getAuthor().getId()) && !GuildData.of(event.getGuild()).hasPermission(event.getMember(), Permissions.CUSTOM_CMDS_MANAGE)) {
                                event.sendMessage(Emojis.NO_GOOD + " Nope, you cannot delete this command because you are not its owner and you don't have `CUSTOM_CMDS_MANAGE` permission.").queue();
                                return;
                            }
                            cmd.getAnswers().remove(i - 1);
                            if (!cmd.getAnswers().isEmpty())
                                cmd.saveAsync();
                            else
                                cmd.deleteAsync();
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Removed answer from custom command `" + args[1] + "`.").queue();
                            break;
                        default:
                            event.sendMessage(CommandManager.getHelp(event.getCommand(), event.getMember())).queue();
                            break;
                    }
                })
                .build();
    }
}
