package br.net.brjdevs.steven.konata.cmds.misc;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.DataManager;
import br.net.brjdevs.steven.konata.core.poll.Poll;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.stream.Stream;

public class PollCommand {

    //@RegisterCommand
    public static ICommand poll() {
        return new ICommand.Builder()
                .setAliases("poll")
                .setName("Poll Command")
                .setCategory(Category.MISCELLANEOUS)
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    String[] args = StringUtils.splitArgs(event.getArguments(), 2);
                    Poll poll = DataManager.db().getPoll(((TextChannel) event.getChannel()));
                    switch (args[0]) {
                        case "create":
                            if (poll != null) {
                                event.sendMessage(Emojis.X + " Um, there's another poll running in this channel and I'm afraid you cannot create another one " + Emojis.SWEAT_SMILE).queue();
                                return;
                            }
                            int index = args[1].indexOf(";");
                            if (index < 0) {
                                event.sendMessage("You have to supply options splitted by a `;`!").queue();
                                return;
                            }
                            String name = args[1].substring(0, index).trim();
                            String[] options = Stream.of(args[1].substring(name.length() + 1).split(";"))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty()).distinct().toArray(String[]::new);
                            if (options.length < 2) {
                                event.sendMessage(Emojis.X + " Erm, you want to create a poll with one option? ~~oppressor~~").queue();
                                return;
                            }
                            poll = new Poll(event.getAuthor(), ((TextChannel) event.getChannel()), name, options);
                            event.sendMessage(poll.toEmbed()).queue();
                            break;
                    }
                })
                .build();
    }
}
