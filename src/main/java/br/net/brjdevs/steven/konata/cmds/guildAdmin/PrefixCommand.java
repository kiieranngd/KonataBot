package br.net.brjdevs.steven.konata.cmds.guildAdmin;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import net.dv8tion.jda.core.Permission;

public class PrefixCommand {

    @RegisterCommand
    public static ICommand prefix() {
        return new ICommand.Builder()
                .setCategory(Category.MODERATION)
                .setAliases("prefix")
                .setName("Prefix Command")
                .setDescription("Changes the prefix if you need!")
                .setUsageInstruction("prefix <prefix> //changes the custom prefix\n" +
                        "prefix //returns the custom prefix\n" +
                        "prefix reset //resets the custom prefix\n\n*Requires MESSAGE_MANAGE permission.*")
                .setRequiredPermission(Permission.MESSAGE_MANAGE)
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    GuildData data = KonataBot.getInstance().getDataManager().getGuild(event.getGuild());
                    if (event.getArguments().isEmpty()) {
                        if (data.getCustomPrefix() == null)
                            event.sendMessage("This guild doesn't have a custom prefix, you can set one by using `konata prefix [new prefix]`, just keep in mind that I'll keep responding to both `konata ` and the new prefix " + Emojis.WINK).queue();
                        else
                            event.sendMessage("This guild's custom prefix is `" + data.getCustomPrefix() + "`. If you want to remove the custom prefix just use `konata prefix reset` " + Emojis.WINK).queue();
                        return;
                    } else if (event.getArguments().equals("reset")) {
                        String prefix = data.getCustomPrefix();
                        data.setCustomPrefix(null);
                        event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Reseted the custom prefix for this guild, this means I'll no longer respond to `" + prefix + "`.").queue();
                        data.save();
                        return;
                    } else if (event.getArguments().equals(data.getCustomPrefix()) || event.getArguments().equals(KonataBot.getInstance().getConfig().defaultPrefix)) {
                        event.sendMessage("I already respond to that prefix! " + Emojis.SWEAT_SMILE).queue();
                        return;
                    }
                    data.setCustomPrefix(event.getArguments());
                    event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Set the custom prefix to `" + event.getArguments() + "`, this means you can start commands with `" + event.getArguments() + "` instead of `konata `. *e.g.: " + event.getArguments() + "help* ").queue();
                    data.saveAsync();
                })
                .build();
    }
}
