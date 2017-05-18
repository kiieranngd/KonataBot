package br.net.brjdevs.steven.konata.core.poll;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.core.data.rethink.DBObject;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.net.brjdevs.steven.konata.core.data.DataManager.conn;
import static br.net.brjdevs.steven.konata.core.utils.Utils.getEntryByIndex;
import static com.rethinkdb.RethinkDB.r;

public class Poll implements DBObject {

    public static final String DB_TABLE = "polls";

    private final Map<String, List<String>> votes;
    private final String name;
    private final String creator, channel;
    private final long createdAt;

    @ConstructorProperties({"votes", "name", "creator", "channel", "createdAt"})
    public Poll(Map<String, List<String>> votes, String name, String creator, String channel, long createdAt) {
        this.votes = votes;
        this.name = name;
        this.creator = creator;
        this.channel = channel;
        this.createdAt = createdAt;
    }

    public Poll(User user, TextChannel textChannel, String name, String[] options) {
        this.votes = new LinkedHashMap<>(
                Stream.of(options).collect(Collectors.toMap(s -> s, o -> new ArrayList<>())));
        this.name = name;
        this.creator = user.getId();
        this.channel = textChannel.getId();
        this.createdAt = System.currentTimeMillis();
    }

    public Map<String, List<String>> getVotes() {
        return votes;
    }
    @JsonProperty("creator")
    public String getCreatorId() {
        return creator;
    }
    @JsonProperty("channel")
    public String getChannelId() {
        return channel;
    }
    public String getName() {
        return name;
    }
    @JsonIgnore
    public List<String> getVotes(int option) {
        return getEntryByIndex(votes, option).getValue();
    }
    public void vote(User user, int option) {
        if (option >= votes.size())
            return;
        List<String> list = getVotes(option);
        if (list.contains(user.getId())) {
            list.remove(user.getId());
            return;
        } else if (votes.values().stream().anyMatch(x -> x.contains(user.getId()))) {
            votes.values().stream().filter(v -> v.contains(user.getId())).forEach(v -> v.remove(user.getId()));
        }
        list.add(user.getId());
    }
    @JsonIgnore
    public List<Map.Entry<String, List<String>>> getLeadership() {
        int i = Collections.max(votes.values().stream().map(List::size).collect(Collectors.toList()));
        return votes.entrySet().stream().filter(entry -> entry.getValue().size() == i).collect(Collectors.toList());
    }
    public MessageEmbed toEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.decode("#388BDF"));
        embedBuilder.setAuthor(name, null, "http://www.iconsdb.com/icons/preview/caribbean-blue/poll-topic-xxl.png");
        final int totalVotes = votes.values().stream().mapToInt(List::size).sum();
        AtomicInteger i = new AtomicInteger(0);
        votes.forEach((option, votes) -> embedBuilder.addField("#" + i.incrementAndGet() + " " + option, "`" + StringUtils.getProgressBar(votes.size(), totalVotes) + "` (" + votes.size() + " votes) " + map(i.get()), false));
        User creator = KonataBot.getInstance().getUserById(getCreatorId());
        embedBuilder.setFooter("Poll created by " + StringUtils.toString(creator) + " - Creation date: ", creator.getEffectiveAvatarUrl());
        embedBuilder.setTimestamp(Instant.ofEpochMilli(createdAt));
        return embedBuilder.build();
    }
    @Override
    public void save() {
        r.table(DB_TABLE).insert(this).optArg("conflict", "replace").runNoReply(conn());
    }

    @Override
    public void delete() {
        r.table(DB_TABLE).get(channel).delete().runNoReply(conn());
    }

    private static String map(int i) {
        switch (i) {
            case 1:
                return "\uD83E\uDD47 ";
            case 2:
                return "\uD83E\uDD48 ";
            case 3:
                return "\uD83E\uDD49 ";
                default:
                    return "";
        }
    }
}
