package br.net.brjdevs.steven.konata.core.economy.events;

import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.interactivechoice.InteractiveChoice;
import br.net.brjdevs.steven.konata.core.utils.ProfileUtils;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Random;

public class RobberyEvent extends Event {

    private static final Random r = new Random();

    public RobberyEvent(ProfileData data, TextChannel textChannel, Member member) {
        super(data, textChannel, member);
        textChannel.sendMessage(member.getAsMention() + " - You see a robbery and you decide to do something. You have three options:\n" +
                "1. `prevent` - You try to prevent the theft.\n" +
                "2. `ask for help` - You scream for help.\n" +
                "3. `ignore` - You don't do anything.")
                .queue(msg ->
                        new InteractiveChoice.Builder()
                                .setChannel(textChannel)
                                .setUser(member.getUser())
                                .expireIn(15000)
                                .setAcceptedResponses("1", "prevent", "2", "ask for help")
                                .onValidResonse((choice, answer) -> {
                                    switch (answer) {
                                        case "1":
                                        case "prevent":
                                            if (r.nextBoolean()) {
                                                boolean rewarded = r.nextInt(20) >= 15;
                                                int reward = r.nextInt(100) + 100;
                                                msg.editMessage("You successfully prevented the theft! " + (rewarded && ProfileUtils.addCoins(data, reward) ? "As a gratitude gesture, you are rewarded! *+" + reward + " coins*" : "") + "*+1 reputation*").queue();
                                                data.addReputation();
                                            } else {
                                                if (data.getCoins() < 15) {
                                                    msg.editMessage("You failed to prevent the theft. *-1 reputation*").queue();
                                                } else {
                                                    int lost = r.nextInt((int) data.getCoins()) + 10;
                                                    if (lost > data.getCoins())
                                                        lost = (int) data.getCoins();
                                                    msg.editMessage("You failed to prevent the theft and got stolen. *-" + lost + " coins -1 reputation*").queue();
                                                    ProfileUtils.takeCoins(data, lost);
                                                }
                                                data.takeReputation();
                                            }
                                            break;
                                        case "2":
                                        case "ask for help":
                                            msg.editMessage("You ask for help but don't do anything.").queue();
                                            data.takeReputation();
                                            break;
                                    }
                                    data.saveAsync();
                                })
                                .onTimeout((choice) -> msg.editMessage("You remembered you're late for a meeting, call the police and keep running.").queue())
                                .onInvalidResonse((choice, answer) -> textChannel.sendMessage("You pretend you didn't see anything.").queue())
                                .build()
                );
    }
}
