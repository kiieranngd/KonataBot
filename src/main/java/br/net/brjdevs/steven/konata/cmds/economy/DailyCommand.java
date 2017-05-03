package br.net.brjdevs.steven.konata.cmds.economy;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.ProfileUtils;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;

import java.util.Random;

public class DailyCommand {

    private static final Random r = new Random();
    @RegisterCommand
    public static ICommand daily() {
        return new ICommand.Builder()
                .setAliases("daily")
                .setName("Daily Command")
                .setDescription("Gives you your daily reward.")
                .setUsageInstruction("daily //can only be used every 24 hours.")
                .setCategory(Category.ECONOMY)
                .setAction((event) -> {
                    ProfileData data = ProfileData.of(event.getAuthor());
                    if (data.getLastDaily() > System.currentTimeMillis()) {
                        event.sendMessage(Emojis.SWEAT_SMILE + " You have to wait more " + time(data.getLastDaily() - System.currentTimeMillis()) + " to claim another daily reward!").queue();
                        return;
                    }
                    data.setLastDaily(System.currentTimeMillis());
                    int coins = r.nextInt(300) + 100;
                    ProfileUtils.addCoins(data, coins);
                    event.sendMessage("\uD83D\uDCB0 Here you go: " + coins + " coins! You can claim another reward in 24 hours!").queue();
                    data.saveAsync();
                })
                .build();
    }

    public static String time(long duration) {
        final long
                years = duration / 31104000000L,
                months = duration / 2592000000L % 12,
                days = duration / 86400000L % 30,
                hours = duration / 3600000L % 24,
                minutes = duration / 60000L % 60,
                seconds = duration / 1000L % 60;
        String s = (years == 0 ? "" : years + " years, ") + (months == 0 ? "" : months + " months, ")
                + (days == 0 ? "" : days + " days, ") + (hours == 0 ? "" : hours + " hours, ")
                + (minutes == 0 ? "" : minutes + " minutes, ") + (seconds == 0 ? "" : seconds + " seconds, ");

        s = StringUtils.replaceLast(s, ", ", "");
        return StringUtils.replaceLast(s, ",", " and");

    }
}
