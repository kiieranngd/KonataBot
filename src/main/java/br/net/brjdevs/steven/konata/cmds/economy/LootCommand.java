package br.net.brjdevs.steven.konata.cmds.economy;

import br.com.brjdevs.java.utils.RateLimiter;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.economy.Looting;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.ProfileUtils;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Random;

public class LootCommand {

    private static RateLimiter RATE_LIMITER = new RateLimiter(1, 30000);

    @RegisterCommand
    public static ICommand loot() {
        return new ICommand.Builder()
                .setAliases("loot")
                .setName("Loot Command")
                .setDescription("Loots the coins on the ground!")
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    if (!RATE_LIMITER.process(event.getAuthor().getId())) {
                        event.sendMessage("Woah, slow down a little there buddy! Don't be greedy!").queue();
                        return;
                    }
                    Looting looting = Looting.of(((TextChannel) event.getChannel()));
                    long gains = looting.collect();
                    if (gains <= 0) {
                        event.sendMessage("Nothing to loot here!").queue();
                        return;
                    }
                    if (ProfileUtils.addCoins(ProfileData.of(event.getAuthor()), gains)) {
                        event.sendMessage("You walk a little and find " + gains + " coins!").queue();
                    } else {
                        looting.drop(gains);
                        event.sendMessage(Emojis.X + " You have too much coins, go spend some first!").queue();
                    }

                })
                .build();
    }
}
