package br.net.brjdevs.steven.konata.cmds.economy;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.guild.GuildData;
import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.utils.Emojis;
import br.net.brjdevs.steven.konata.core.utils.ProfileUtils;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BetCommand {

    private static final Random r = new Random();

    @RegisterCommand
    public static ICommand bet() {
        return new ICommand.Builder()
                .setAliases("bet")
                .setName("Bet Command")
                .setDescription("Bet all your money here!")
                .setCategory(Category.ECONOMY)
                .setUsageInstruction("bet //gives you info about how to use it\n" +
                        "bet start [start amount] //starts a bet\n" +
                        "bet join [coin amount] //joins a bet with an amount of coins\n" +
                        "bet info //gives you information on a bet\n" +
                        "\\*bet end //stops a bet\n" +
                        "\n" +
                        "\\* *Requires you to be the bet creator or have MANAGE_SERVER permission*")
                .setPrivateAvailable(false)
                .setAction((event) -> {
                    String[] args = event.getArguments().split(" ");
                    switch (args[0]) {
                        case "start":
                            Bet bet = Bet.getBet(((TextChannel) event.getChannel()));
                            if (bet != null) {
                                event.sendMessage(Emojis.X + " There's a bet running is channel already!").queue();
                                return;
                            } else if (args.length < 2 || !args[1].matches("[0-9]+")) {
                                event.sendMessage(Emojis.X + " You have to provide a valid amount of coins! e.g.: konata bet start 100").queue();
                                return;
                            }
                            ProfileData data = ProfileData.of(event.getAuthor());
                            int i = Integer.parseInt(args[1]);
                            if (!ProfileUtils.takeCoins(data, i)) {
                                event.sendMessage(Emojis.X + " You don't have enough coins to start a bet with " + i + " coins!").queue();
                                return;
                            }
                            data.saveAsync();
                            new Bet(((TextChannel) event.getChannel()), event.getAuthor(), i);
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " Started a bet in " + ((TextChannel) event.getChannel()).getAsMention() + " with " + i + " coins!").queue();
                            break;
                        case "join":
                            bet = Bet.getBet(((TextChannel) event.getChannel()));
                            if (bet == null) {
                                event.sendMessage("There are no bets running in this channel! " + Emojis.SWEAT_SMILE).queue();
                                return;
                            } else if (bet.getCreator().equals(event.getAuthor())) {
                                event.sendMessage(Emojis.X + " You cannot join your own bet!").queue();
                                return;
                            } else if (args.length < 2 || !args[1].matches("[0-9]+") && !args[1].equals("all")) {
                                event.sendMessage(Emojis.X + " You have to provide a valid amount of coins or use `all` to bet all your coins!! e.g.: konata bet join " + bet.getMinimumAmount()).queue();
                                return;
                            } else if (bet.map.containsKey(event.getAuthor().getIdLong())) {
                                data = ProfileData.of(event.getAuthor());
                                ProfileUtils.addCoins(data, bet.map.get(event.getAuthor().getIdLong()));
                                data.saveAsync();
                                bet.modifyUser(event.getAuthor(), 0);
                                event.sendMessage("You quit the bet! (total coins: " + bet.getTotalAmount() + ")").queue();
                                return;
                            }
                            data = ProfileData.of(event.getAuthor());
                            long amount = args[1].equals("all") ? data.getCoins() : Long.parseLong(args[1]);
                            long min = bet.getMinimumAmount();
                            if (min > amount) {
                                event.sendMessage(Emojis.X + " You have to bet at least " + min + " coins!").queue();
                                return;
                            } else if (!ProfileUtils.takeCoins(data, amount)) {
                                event.sendMessage("You have to provide an amount of coins within your current balance!").queue();
                                return;
                            }
                            data.saveAsync();
                            bet.modifyUser(event.getAuthor(), amount);
                            event.sendMessage(Emojis.BALLOT_CHECK_MARK + " You bet " + (data.getCoins() == 0 ? "all your" : amount) + " coins! (total coins: " + bet.getTotalAmount() + ")").queue();
                            break;
                        case "info":
                            bet = Bet.getBet(((TextChannel) event.getChannel()));
                            if (bet == null) {
                                event.sendMessage("There are no bets running in this channel! " + Emojis.SWEAT_SMILE).queue();
                                return;
                            }
                            AtomicInteger counter = new AtomicInteger();
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setColor(Color.decode("#388BDF"));
                            User creator = bet.getCreator();
                            embedBuilder.setAuthor(creator.getName() + "'s bet", null, creator.getEffectiveAvatarUrl());
                            embedBuilder.setDescription("Total users participating: " + bet.map.size() + "\n\n" +
                                    Arrays.stream(bet.map.keys()).sorted().mapToObj(l -> "**#" + counter.incrementAndGet() + "** `" + StringUtils.toString(event.getJDA().getUserById(l)) + "` - " + bet.map.get(l) + " coins").collect(Collectors.joining("\n")));
                            embedBuilder.setFooter("Total coins: " +  bet.getTotalAmount(), null);

                            event.sendMessage(embedBuilder.build()).queue();
                            break;
                        case "end":
                            bet = Bet.getBet(((TextChannel) event.getChannel()));
                            if (bet == null) {
                                event.sendMessage("There are no bets running in this channel! " + Emojis.SWEAT_SMILE).queue();
                                return;
                            } else if (!bet.getCreator().equals(event.getAuthor()) && !GuildData.of(event.getGuild()).hasPermission(event.getMember(), br.net.brjdevs.steven.konata.core.permissions.Permissions.BET_MANAGE)) {
                                event.sendMessage(Emojis.X + " You don't have the `BET_MANAGE` permission!").queue();
                                return;
                            }
                            Bet.bets.remove(event.getChannel().getIdLong());
                            long prize = bet.getTotalAmount();
                            List<User> list = Arrays.stream(bet.map.keys()).mapToObj(id -> event.getJDA().getUserById(id)).filter(Objects::nonNull).collect(Collectors.toList());
                            if (list.isEmpty())
                                return;
                            User winner = list.get(r.nextInt(list.size()));
                            event.sendMessage("The bet has ended! And the lucky winner is... " + winner.getAsMention() + "! Congratulations, you won " + prize + " coins!").queue();
                            data = ProfileData.of(winner);
                            ProfileUtils.addCoins(data, prize);
                            data.saveAsync();
                            break;
                    }
                })
                .build();
    }

    public static class Bet {
        private static final TLongObjectMap<Bet> bets = new TLongObjectHashMap<>();

        public static Bet getBet(TextChannel textChannel) {
            Bet bet = bets.get(textChannel.getIdLong());
            if (bet != null && bet.getCreator() == null) {
                bets.remove(textChannel.getIdLong());
                return null;
            }
            return bet;
        }

        private TLongLongMap map = new TLongLongHashMap();
        private int shardId;

        public Bet(TextChannel tc, User user, long minimumAmount) {
            map.put(user.getIdLong(), minimumAmount);
            shardId = KonataBot.getInstance().getShardId(tc.getJDA());
            bets.put(tc.getIdLong(), this);
        }

        public User getCreator() {
            return KonataBot.getInstance().getShards()[shardId].getJDA().getUserById(map.keys()[0]);
        }

        public long getMinimumAmount() {
            return map.values()[0];
        }

        public long getTotalAmount() {
            return Arrays.stream(map.values()).sum();
        }

        public boolean modifyUser(User user, long amount) {
            if (!map.containsKey(user.getIdLong())) {
                map.put(user.getIdLong(), amount);
                return true;
            }
            map.remove(user.getIdLong());
            return false;
        }
    }
}
