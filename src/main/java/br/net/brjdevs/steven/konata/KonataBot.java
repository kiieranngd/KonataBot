package br.net.brjdevs.steven.konata;

import br.net.brjdevs.steven.konata.core.TaskManager;
import br.net.brjdevs.steven.konata.core.commands.CommandManager;
import br.net.brjdevs.steven.konata.core.data.Config;
import br.net.brjdevs.steven.konata.core.data.DataManager;
import br.net.brjdevs.steven.konata.core.events.EventManager;
import br.net.brjdevs.steven.konata.core.music.KonataMusicManager;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KonataBot {

    public static final String VERSION = "@version@";
    private static final Logger LOGGER = LoggerFactory.getLogger("KonataBot");
    private static LoadState LOAD_STATE;
    private static KonataBot instance;

    public static LoadState getLoadState() {
        return LOAD_STATE;
    }
    public static KonataBot getInstance() {
        return instance;
    }

    private final Config config;
    private Shard[] shards;
    private DataManager dataManager;
    private CommandManager commandManager;
    private KonataMusicManager musicManager;
    private AtomicLongArray lastEvents;

    public KonataBot(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return config;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public List<Guild> getGuilds() {
        return Stream.of(shards).map(g -> g.getJDA().getGuilds()).flatMap(List::stream).collect(Collectors.toList());
    }

    public List<User> getUsers() {
        return Stream.of(shards).map(g -> g.getJDA().getUsers()).flatMap(List::stream).collect(Collectors.toList());
    }

    public List<TextChannel> getTextChannels() {
        return Stream.of(shards).map(g -> g.getJDA().getTextChannels()).flatMap(List::stream).collect(Collectors.toList());
    }

    public AtomicLongArray getLastEvents() {
        return lastEvents;
    }
    public List<VoiceChannel> getVoiceChannels() {
        return Stream.of(shards).map(g -> g.getJDA().getVoiceChannels()).flatMap(List::stream).collect(Collectors.toList());
    }

    public Shard[] getConnectedShards() {
        return Stream.of(shards).filter(shard -> shard.getJDA().getStatus() == JDA.Status.CONNECTED).toArray(Shard[]::new);
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public boolean isOwner(User user) {
        return config.owners.contains(user.getId());
    }

    public Shard getShard(JDA jda) {
        return shards[getShardId(jda)];
    }

    public int getShardId(JDA jda) {
        return jda.getShardInfo() == null ? 0 : jda.getShardInfo().getShardId();
    }

    public Shard[] getShards() {
        return shards;
    }

    public KonataMusicManager getMusicManager() {
        return musicManager;
    }
    public int getShardId(long guildId) {
        return (int) (guildId >> 22) % shards.length;
    }

    public static void main(String[] args) {
        try {
            RestAction.LOG.setLevel(SimpleLog.Level.OFF); // I don't want my log full of useless errors.

            long l = System.currentTimeMillis();
            LOAD_STATE = LoadState.PRELOAD;

            LOGGER.info("Attempting to connect to RethinkDB...");
            DataManager.conn();
            LOGGER.info("Connected!");

            EventManager.loadListeners();

            Config config = new Config(Paths.get("config.json").toAbsolutePath());

            instance = new KonataBot(config);

            HttpResponse<JsonNode> shards = Unirest.get("https://discordapp.com/api/gateway/bot")
                    .header("Authorization", "Bot " + config.token)
                    .header("Content-Type", "application/json")
                    .asJson();

            int shardTotal = shards.getBody().getObject().getInt("shards");

            LOGGER.info("Starting KonataBot instance with " + shardTotal + " shards...");

            LOAD_STATE = LoadState.LOADING;

            instance.shards = new Shard[shardTotal];
            for (int i = 0; i < shardTotal; i++) {
                instance.shards[i] = new Shard(i, shardTotal);
            }
            LOGGER.info("Finished loading all shards!");

            LOAD_STATE = LoadState.LOADED;

            instance.lastEvents = new AtomicLongArray(shardTotal);

            instance.dataManager = new DataManager();

            instance.musicManager = new KonataMusicManager();

            instance.commandManager = new CommandManager();

            TaskManager.startAsyncTask("DBots Updater", (service) -> {
                for (Shard shard : instance.shards) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("server_count", shard.getJDA().getGuilds().size());
                    if (shardTotal > 1) {
                        jsonObject.put("shard_id", shard.getId());
                        jsonObject.put("shard_count", shardTotal);
                    }

                    try {
                        if (!config.dbotsPw.isEmpty())
                            Unirest.post("https://bots.discord.pw/api/bots/" + shard.getJDA().getSelfUser().getId() + "/stats")
                                    .header("Authorization", config.dbotsPw)
                                    .header("Content-Type", "application/json")
                                    .body(jsonObject.toString())
                                    .asJson();
                    } catch (UnirestException e) {
                        LOGGER.error("Failed to update shard_count at discordbots.pw!");
                    }

                    try {
                        if (!config.dbotsOrg.isEmpty())
                            Unirest.post("https://discordbots.org/api/bots/" + shard.getJDA().getSelfUser().getId() + "/stats")
                                    .header("Authorization", config.dbotsOrg)
                                    .header("Content-Type", "application/json")
                                    .body(jsonObject.toString())
                                    .asJson();
                    } catch (UnirestException e) {
                        LOGGER.error("Failed to update shard_count at discordbots.org!");
                    }
                }
            }, 3600);

            LOGGER.info("Started KonataBot " + VERSION + " instance in " + ((System.currentTimeMillis() - l) / 1000) + " seconds.");

            LOAD_STATE = LoadState.POSTLOAD;

        } catch (Exception e) {
            LOGGER.error("Failed to start KonataBot instance (Load State: " + getLoadState() + "). Stopping application...", e);
            System.exit(0);
        }
    }
}