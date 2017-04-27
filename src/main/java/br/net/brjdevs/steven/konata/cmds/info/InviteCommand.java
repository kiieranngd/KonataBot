package br.net.brjdevs.steven.konata.cmds.info;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import net.dv8tion.jda.core.EmbedBuilder;

public class InviteCommand {
    public static ICommand invite() {
        return new ICommand.Builder()
                .setAliases("invite", "inviteme")
                .setName("Invite Command")
                .setDescription("Sends you my OAuth invite url!")
                .setCategory(Category.INFORMATIVE)
                .setAction((event) -> {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setAuthor("Konata's invite url", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    embedBuilder.setDescription("You can add me to your guild by [clicking here](" + event.getJDA().asBot().getInviteUrl() + ")\nIf your server doesn't show up on that list, either you are not logged in discord in your browser or you don't have MANAGE_SERVER permission.");
                    event.sendMessage(embedBuilder.build()).queue();
                })
                .build();
    }
}
