package br.net.brjdevs.steven.konata;

import br.net.brjdevs.steven.konata.core.TaskManager;
import br.net.brjdevs.steven.konata.core.commands.CommandManager;
import br.net.brjdevs.steven.konata.core.data.Config;
import br.net.brjdevs.steven.konata.core.data.DataManager;
import br.net.brjdevs.steven.konata.core.events.EventManager;
import br.net.brjdevs.steven.konata.core.music.KonataMusicManager;
import br.net.brjdevs.steven.konata.log.DiscordLogBack;
import br.net.brjdevs.steven.konata.log.SimpleLogToSLF4JAdapter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
        return Stream.of(shards).flatMap(g -> g.getJDA().getGuilds().stream()).collect(Collectors.toList());
    }

    public List<User> getUsers() {
        return Stream.of(shards).flatMap(g -> g.getJDA().getUsers().stream()).collect(Collectors.toList());
    }

    public User getUserById(String id) {
        return Stream.of(shards).map(g -> g.getJDA().getUserById(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public User getUserById(long id) {
        return Stream.of(shards).map(g -> g.getJDA().getUserById(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }


    public List<TextChannel> getTextChannels() {
        return Stream.of(shards).flatMap(g -> g.getJDA().getTextChannels().stream()).collect(Collectors.toList());
    }

    public TextChannel getTextChannelById(String id) {
        return Stream.of(shards).map(g -> g.getJDA().getTextChannelById(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public TextChannel getTextChannelById(long id) {
        return Stream.of(shards).map(g -> g.getJDA().getTextChannelById(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public PrivateChannel getPrivateChannelById(String id) {
        return Stream.of(shards).map(g -> g.getJDA().getPrivateChannelById(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public PrivateChannel getPrivateChannelById(long id) {
        return Stream.of(shards).map(g -> g.getJDA().getPrivateChannelById(id)).filter(Objects::nonNull).findFirst().orElse(null);
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

    public Guild getGuildById(long id) {
        return shards[getShardId(id)].getJDA().getGuildById(id);
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
        return (int) ((guildId >> 22) % shards.length);
    }

    public static void main(String[] args) {
        try {

            RestAction.DEFAULT_FAILURE = (throwable) -> {}; // I don't want my log full of useless errors.
            SimpleLogToSLF4JAdapter.install();

            long l = System.currentTimeMillis();
            LOAD_STATE = LoadState.PRELOAD;

            LOGGER.info("Attempting to connect to RethinkDB...");
            DataManager.conn();
            LOGGER.info("Connected!");

            Config config = new Config(Paths.get("config.json").toAbsolutePath());

            instance = new KonataBot(config);

            HttpGet get = new HttpGet("https://discordapp.com/api/gateway/bot");
            get.addHeader("Authorization", "Bot " + config.token);
            get.addHeader("Content-type", "application/json");
            int shardTotal = new JSONObject(EntityUtils.toString(HttpClientBuilder.create().build().execute(get).getEntity())).getInt("shards");

            LOGGER.info("Starting KonataBot instance with " + shardTotal + " shards...");

            LOAD_STATE = LoadState.LOADING;

            instance.shards = new Shard[shardTotal];
            for (int i = 0; i < shardTotal; i++) {
                instance.shards[i] = new Shard(i, shardTotal);
                Thread.sleep(5000);
            }
            LOGGER.info("Finished loading all shards!");

            DiscordLogBack.enable();

            LOAD_STATE = LoadState.LOADED;

            instance.lastEvents = new AtomicLongArray(shardTotal);

            instance.musicManager = new KonataMusicManager();

            instance.commandManager = new CommandManager();

            //new SpringApplication(WebServer.class).run();

            TaskManager.startAsyncTask("DBots Updater", (service) -> {
                HttpClient client = HttpClientBuilder.create().build();
                long userId = instance.shards[0].getJDA().getSelfUser().getIdLong();
                HttpPost dbotsOrg = new HttpPost("https://discordbots.org/api/bots/" + userId + "/stats");
                HttpPost dbotsPw = new HttpPost("https://bots.discord.pw/api/bots/" + userId + "/stats");
                dbotsOrg.addHeader("Authorization", config.dbotsOrg);
                dbotsPw.addHeader("Authorization", config.dbotsPw);
                for (Shard shard : instance.shards) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("server_count", shard.getJDA().getGuilds().size());
                    if (shardTotal > 1) {
                        jsonObject.put("shard_id", shard.getId());
                        jsonObject.put("shard_count", shardTotal);
                    }
                    try {
                        StringEntity entity = new StringEntity(jsonObject.toString());
                        dbotsOrg.setEntity(entity);
                        dbotsPw.setEntity(entity);

                        try {
                            if (!config.dbotsPw.isEmpty()) {
                                int result = client.execute(dbotsPw).getStatusLine().getStatusCode();
                                if (result != 200) {
                                    LOGGER.error("Failed to update server count at discordbots.pw! Status code: " + result);
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.error("Failed to update shard_count at discordbots.pw!");
                        }

                        try {
                            if (!config.dbotsOrg.isEmpty()) {
                                int result = client.execute(dbotsOrg).getStatusLine().getStatusCode();
                                if (result != 200) {
                                    LOGGER.error("Failed to update server count at discordbots.org! Status code: " + result);
                                }
                            }
                        } catch (IOException e) {
                            LOGGER.error("Failed to update shard_count at discordbots.org!");
                        }
                    } catch (Exception ignored) {
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