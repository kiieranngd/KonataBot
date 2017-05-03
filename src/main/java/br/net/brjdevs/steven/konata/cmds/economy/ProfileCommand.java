package br.net.brjdevs.steven.konata.cmds.economy;

import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.utils.ProfileUtils;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class ProfileCommand {

    @RegisterCommand
    public static ICommand profile() {
        return new ICommand.Builder()
                .setAliases("profile")
                .setName("Profile Command")
                .setDescription("Shows you your profile.")
                .setCategory(Category.ECONOMY)
                .setAction((event) -> {
                    User user = event.getMessage().getMentionedUsers().isEmpty() ? event.getAuthor() : event.getMessage().getMentionedUsers().get(0);
                    ProfileData data = ProfileData.of(user);
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setAuthor(user.getName() + "'s profile", null, user.getEffectiveAvatarUrl());
                    embedBuilder.addField("\uD83D\uDCB0 Coins", String.valueOf(data.getCoins()), true);
                    embedBuilder.addField("\u26a1 Level", data.getLevel() + " (Experience: " + data.getExperience() + ")", true);
                    embedBuilder.addField("\u2b50 Experience to next level", String.valueOf(ProfileUtils.expForNextLevel(data.getLevel())), true);
                    embedBuilder.addField("\uD83C\uDFC6 Rank", data.getRank().name(), true);
                    embedBuilder.addField("\uD83C\uDF96 Reputation", String.valueOf(data.getReputation()), true);
                    embedBuilder.setThumbnail(user.getEffectiveAvatarUrl());
                    OffsetDateTime date = OffsetDateTime.ofInstant(Instant.ofEpochMilli(data.getLastDaily()), ZoneId.of("GMT"));
                    String nextDailyReward = data.getLastDaily() < System.currentTimeMillis() ? "Available!" : format(date);
                    embedBuilder.addField("Next daily reward", nextDailyReward, true);
                    embedBuilder.setColor(Color.decode("#388BDF"));
                    embedBuilder.setFooter("Requested by " + StringUtils.toString(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl());

                    event.sendMessage(embedBuilder.build()).queue();
                })
                .build();
    }

    private static String format(OffsetDateTime date) {
        return StringUtils.capitalize(date.getDayOfWeek().name().substring(0, 3)) + ", " + date.getDayOfMonth() + " " + StringUtils.capitalize(date.getMonth().name().substring(0, 3)) + " " + date.getYear() + " " + (date.getHour() < 10 ? "0" + date.getHour() : date.getHour()) + ":" + (date.getMinute() < 10 ? "0" + date.getMinute() : date.getMinute());
    }
}
