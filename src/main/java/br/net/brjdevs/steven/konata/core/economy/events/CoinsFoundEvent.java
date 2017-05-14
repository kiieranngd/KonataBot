package br.net.brjdevs.steven.konata.core.economy.events;

import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.interactivechoice.InteractiveChoice;
import br.net.brjdevs.steven.konata.core.utils.ProfileUtils;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Random;

public class CoinsFoundEvent extends Event {

    private static final Random r = new Random();

    public CoinsFoundEvent(ProfileData data, TextChannel textChannel, Member member) {
        super(data, textChannel, member);
        int coins = (r.nextInt(200 - 50)) + 50;
        textChannel.sendMessage(member.getAsMention() + " - You found " + coins + " coins dropped on the ground! You have three options:\n" +
                "1. `pick` - You get the money and nothing else happens.\n" +
                "2. `find owner` - If you find the owner, he might reward you!\n" +
                "3. `ignore` - If you are stupid enough, choose this.")
                .queue(msg ->
                        new InteractiveChoice.Builder()
                                .setChannel(textChannel)
                                .setUser(member.getUser())
                                .expireIn(15000)
                                .setAcceptedResponses("1", "pick", "2", "find owner")
                                .onValidResonse((choice, answer) -> {
                                    switch (answer) {
                                        case "1":
                                        case "pick":
                                            boolean caught = r.nextInt(5) > 2 && data.getReputation() > -6;
                                            msg.editMessage("You quickly pick up the coins and pretend nothing happened. " + (caught ? "Unfortunately, someone saw you doing that. *-1 reputation*" : "") + " *+" + coins + " coins*").queue();
                                            if (caught)
                                                data.takeReputation();
                                            ProfileUtils.addCoins(data, coins);
                                            break;
                                        case "2":
                                        case "find owner":
                                            int i = r.nextInt(10);
                                            if (i >= 5) {
                                                boolean rewarded = i > 7;
                                                int gains = i * 10 + (coins * 2);
                                                msg.editMessage("You found the coins' owner and returned them. " + (rewarded && ProfileUtils.addCoins(data, gains) ? "The owner really liked your gesture and decided to reward you! *+" + gains + " coins* " : "") + "*+1 reputation*").queue();
                                                data.addReputation();
                                            } else {
                                                msg.editMessage("You searched the owner really hard but you couldn't find them. You decided to keep the coins! *+" + coins + " coins*").queue();
                                                ProfileUtils.addCoins(data, coins);
                                            }
                                            break;
                                    }
                                    data.saveAsync();
                                })
                                .onTimeout((choice) -> msg.editMessage("You remembered you're late for a meeting and drop the coins").queue())
                                .onInvalidResonse((choice, answer) -> msg.editMessage("You left the coins where you found them and kept walking.").queue())
                                .build()
                );
    }
}
