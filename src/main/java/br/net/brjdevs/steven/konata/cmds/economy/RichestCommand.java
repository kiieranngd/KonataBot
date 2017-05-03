package br.net.brjdevs.steven.konata.cmds.economy;

import br.net.brjdevs.steven.konata.KonataBot;
import br.net.brjdevs.steven.konata.Shard;
import br.net.brjdevs.steven.konata.core.commands.Category;
import br.net.brjdevs.steven.konata.core.commands.ICommand;
import br.net.brjdevs.steven.konata.core.commands.RegisterCommand;
import br.net.brjdevs.steven.konata.core.data.user.ProfileData;
import br.net.brjdevs.steven.konata.core.utils.StringUtils;
import com.rethinkdb.model.OptArgs;
import com.rethinkdb.net.Cursor;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rethinkdb.RethinkDB.r;
import static br.net.brjdevs.steven.konata.core.data.DataManager.conn;

public class RichestCommand {

    @RegisterCommand
    public static ICommand richest() {
        return new ICommand.Builder()
                .setAliases("richest")
                .setName("Richest Command")
                .setDescription("Lists the 15 richest users!")
                .setCategory(Category.ECONOMY)
                .setAction((event) -> {
                    AtomicInteger i = new AtomicInteger();

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setDescription(getRichestUsers()
                            .map(pair -> "**#" + i.incrementAndGet() + "** `" + StringUtils.toString(pair.getKey()) + "` - " + pair.getValue() + " coins").collect(Collectors.joining("\n")));
                    embedBuilder.setColor(Color.decode("#388BDF"));
                    embedBuilder.setAuthor("Richest users", null, event.getJDA().getSelfUser().getEffectiveAvatarUrl());
                    embedBuilder.setFooter("Requested by " + StringUtils.toString(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl());
                    event.sendMessage(embedBuilder.build()).queue();
                })
                .build();
    }

    private static Stream<Pair<User, String>> getRichestUsers() {
        Cursor<Map> cursor = r.table("profiles")
                .orderBy()
                .optArg("index", r.desc("coins"))
                .map(profile -> profile.pluck("id", "coins"))
                .limit(15)
                .run(conn(), OptArgs.of("read_mode", "outdated"));
        List<Map> list = cursor.toList();
        return list.stream().map(map -> Pair.of(getUserById(map.get("id").toString().split(":")[0]), map.get("coins").toString()))
                .filter(p -> Objects.nonNull(p.getKey()));
    }

    private static User getUserById(String id) {
        if (id == null) return null;
        Shard shard = Arrays.stream(KonataBot.getInstance().getShards()).filter(s ->
                s.getJDA().getUserById(id) != null).findFirst().orElse(null);
        return shard == null ? null : shard.getJDA().getUserById(id);
    }
}
