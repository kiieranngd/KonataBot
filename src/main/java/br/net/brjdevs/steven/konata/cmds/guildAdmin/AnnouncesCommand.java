package br.net.brjdevs.steven.konata.cmds.guildAdmin;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.DataManager;
import br.net.brjdevs.steven.konata.core.data.guild.Announces;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;

public class AnnouncesCommand {

    @RegisterCommand
    public static ICommand greeting() {
        return new ICommand.Builder()
                .setCategory(Category.MODERATION)
                .setAliases("greeting")
                .setName("Greeting Command")
                .setDescription("Sets a greeting message!")
                .setUsageInstruction("greeting //returns the greeting message\n" +
                        "greeting set <greeting message>\n" +
                        "greeting reset //resets the greeting message")
                .setPrivateAvailable(false)
                .setRequiredPermission(Permission.MANAGE_SERVER)
                .setAction((event) -> {
                    Announces announces = DataManager.db().getAnnounces(event.getGuild());
                    String[] s = event.getArguments().split(" ", 2);
                    switch (s[0]) {
                        case "set":
                            if (s.length == 1) {
                                event.sendMessage(Emojis.X + " You have to tell me a message!").queue();
                                return;
                            }
                            announces.setGreeting(s[1]);
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Updated greeting message! The following message will be sent when an user join this guild:\n\n" + announces.getGreeting()).queue();
                            announces.saveAsync();
                            break;
                        case "reset":
                            announces.setGreeting(null);
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Reseted greeting message!").queue();
                            announces.saveAsync();
                            break;
                        default:
                            if (announces.getGreeting() == null) {
                                event.sendMessage("Oops, there's not greeting message set! Use `konata greeting set <message>` to set one!").queue();
                            } else {
                                TextChannel tc = event.getGuild().getTextChannelById(announces.getChannel());
                                event.sendMessage("The following message will be sent " + (tc != null ? "in " + tc.getAsMention() : "") + " when an user joins this guild:\n\n" + announces.getGreeting() + "\n\nIf you want to change it you can use `konata greeting set [new_message]` and if you'd like to remove it just use `konata greeting reset`.").queue();
                            }
                            break;
                    }
                })
                .build();
    }

    @RegisterCommand
    public static ICommand announceschannel() {
        return new ICommand.Builder()
                .setAliases("announceschannel", "annchannel")
                .setName("Announce Channel Command")
                .setDescription("Sets the channel the greetings and farewell will be sent.")
                .setUsageInstruction("announceschannel //returns the channel\n" +
                        "announceschannel set <channel_mention>\n" +
                        "announceschannel reset //resets the announce channel")
                .setRequiredPermission(Permission.MANAGE_SERVER)
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    Announces announces = DataManager.db().getAnnounces(event.getGuild());
                    String[] s = event.getArguments().split(" ", 2);
                    switch (s[0]) {
                        case "set":
                            TextChannel tc = event.getMessage().getMentionedChannels().isEmpty() ? ((TextChannel) event.getChannel()) : event.getMessage().getMentionedChannels().get(0);
                            announces.setChannel(tc.getId());
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Now the greetings and farewells will be sent in " + tc.getAsMention()).queue();
                            announces.saveAsync();
                            break;
                        case "reset":
                            announces.setChannel(null);
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Removed the greetings and farewells channel.").queue();
                            announces.saveAsync();
                            break;
                        default:
                            tc = event.getGuild().getTextChannelById(announces.getChannel());
                            if (tc == null) {
                                event.sendMessage("Oops, there's no announce channel set! Use `konata announceschannel set #channel` to set one!").queue();
                            } else {
                                event.sendMessage("The greetings and farewells will be sent in " + tc.getAsMention() + ". If you want to change that, use `konata announceschannel #channel`. " + Emojis.WINK).queue();
                            }
                            break;
                    }
                })
                .build();
    }

    @RegisterCommand
    public static ICommand farewell() {
        return new ICommand.Builder()
                .setCategory(Category.MODERATION)
                .setAliases("farewell")
                .setName("Farewell Command")
                .setDescription("Sets a farewell message!")
                .setUsageInstruction("farewell //returns the farewell message\n" +
                        "farewell set <farewell message>\n" +
                        "farewell reset //resets the farewell message")
                .setPrivateAvailable(false)
                .setRequiredPermission(Permission.MANAGE_SERVER)
                .setAction((event) -> {
                    Announces announces = DataManager.db().getAnnounces(event.getGuild());
                    String[] s = event.getArguments().split(" ", 2);
                    switch (s[0]) {
                        case "set":
                            if (s.length == 1) {
                                event.sendMessage(Emojis.X + " You have to tell me a message!").queue();
                                return;
                            }
                            announces.setFarewell(s[1]);
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Updated farewell message! The following message will be sent when an user leave this guild:\n\n" + announces.getFarewell()).queue();
                            announces.saveAsync();
                            break;
                        case "reset":
                            announces.setFarewell(null);
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Reseted farewell message!").queue();
                            announces.saveAsync();
                            break;
                        default:
                            if (announces.getFarewell() == null) {
                                event.sendMessage("Oops, there's not farewell message set! Use `konata farewell set <message>` to set one!").queue();
                            } else {
                                TextChannel tc = event.getGuild().getTextChannelById(announces.getChannel());
                                event.sendMessage("The following message will be sent " + (tc != null ? "in " + tc.getAsMention() : "") + " when an user leaves this guild:\n\n" + announces.getFarewell() + "\n\nIf you want to change it you can use `konata farewell set [new_message]` and if you'd like to remove it just use `konata farewell reset`.").queue();
                            }
                            break;
                    }
                })
                .build();
    }

}
